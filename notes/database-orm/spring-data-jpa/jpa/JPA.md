# Spring Data JPA

## **1. `JPA` - Java/Jakarta Persistence API**

> _JPA là **chuẩn** (`specification`) của `Java` cho việc mapping `object` <-> `database`_

Cụ thể, **JPA** bao gồm:

- **Specification** - Bộ quy chuẩn cho việc mapping.
- **Interfaces**
- **Annotations**

**JPA trong Spring Boot**:

- Trước đây, với `Spring Boot 2.x` sử dụng: **_`javax.persistence.*`_**
- Hiện nay, `Spring Boot 3.x` sử dụng: **_`jakarta.persistence.*`_**

### **Kiến trúc bên trong JPA**

#### **1.1. `EntityManagerFactory`**

- Bản chất: `EntityManagerFactory` là super-factory, chứa toàn bộ configuration về:
  - DB connection: URL, username, password, driver, ..
  - Mapping metadata: bản đồ ánh xạ của toàn bộ `@Entity` trong hệ thống
- **Nguyên tắc**: khởi tạo `EntityManagerFactory` **CỰC KÌ NẶNG**, do vậy **_chỉ nên có `only-one` EntityManagerFactory được sinh ra lúc khởi động server, và sống trong toàn bộ vòng đời của server_**

#### **1.2. `EntityManager`**

> _Là object được sinh ra bởi `EntityManagerFactory`, mỗi khi có Request từ user, hệ thống sẽ sinh ra một `EntityManager`._

- Bản chất: là `main-interface` tương tác với Database, có quyền:
  - `persist()` -> lưu mới
  - `merge()` -> cập nhật
  - `remove()` -> xóa
  - `find()` -> tìm kiếm
- Đặc điểm: khởi tạo **nhanh** và **nhẹ**, tuy nhiên **KHÔNG** thread-safe:
  - mỗi Request **PHẢI** được dùng một instance `EntityManager` riêng biệt, không được dùng chung.
  - dùng xong là **`Close`** ngay instance đó

#### **1.3. `Persistence Context`**

- Giữa `Object` và `Relational`, tồn tại `Persistence Context` (hay First-level Cache - cache cấp 1):
  - `EntityManager`: là lớp quản lý `Persistence Context`, mọi thao tác với DB phải thực hiện thông qua `EntityManager`.
  - Cách hoạt động: Khi yêu cầu `EntityManager` lưu User, nó không **INSERT** ngày lập tức xuống DB. Thay vào đó, nó gom User vào `sandbox` (`Persistence Context`) trước, chờ tới khi kết thúc phiên - `transaction commit` - _**EntityManager mới truy vấn sandbox, gom lại thành SQL tối ưu nhất và thao tác với DB**_ -> giảm tải cho DB.
  - Khi có nhiều lệnh thao tác DB giống nhau, First-level Cache thực hiện 1 lần (nếu chỉ là find..), sau đó cache và sử dụng lại cache thay vì query thêm.

#### **1.4. `@Entity`**

> _`@Entity` annotation giúp class map được với table_

```kotlin
import jakarta.persistence.*
import java.util.UUID

// ex: User table
@Entity
@Table(
  name="users",
  indexes=[ Index(
              name="idx_user_email",
              columnList="email",
              unique="true"
          )])
class User(
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  var id: UUID? = null

  @Column(name="full_name", nullable=false, length=100)
  var fullName: String,

  @Column(nullable=false, unique=true, length=150)
  var email: String

  @Enumerated(EnumType.STRING)
  @Column(nullable=false, length=20)
  var status: UserStatu = UserStatus.ACTIVE

  @Transient
  var age: Int? = null
)

enum class UserStatus {
  ACTIVE, INACTIVE, BANNED
}
```

**Annotations**:

- `@Entity`: **BẮT BUỘC**, giúp Hibernate mark đây là Entity có thể map tới DB
- `@Table`: optional
  - `name="users"` -> ép tên bảng trong DB. Nếu không có, Hibernate lấy tên class làm tên bảng.
  - `indexes`: tạo index cho table -> giúp `find` trên `indexed column` chạy nhanh hơn, không cần **Full Table Scan**.
- `@Id` & `@GeneratedValue`:
  - `@Id`: table's pk (primary key)
  - `@GeneratedValue`: auto-generate id:
    - `GeneratedType.IDENTITY`: `AUTO-INCREMENT` trong MySQL, tự động tăng 1, 2, ...
    - `GeneratedType.UUID`: sinh random string, ex: _`550e8400-e29b-41d4-a716-446655440000`_ -> an toàn
- `@Column`: optional, custom table's column
  - `name="full_name"`: custom column's name. By default, class field -> column's name (camelCase -> snake_case)
  - `nullable=false`: by default, `nullable=true`
  - `unique=true`: by default, `unique=false`
  - `length=150`: string length, by default, `length=255`
- `@Enumerated` - enum column
  > _nếu không sử dụng `Enumerated(EnumType.STRING)`, Hibernate lấy **số thứ tự** như 0, 1, 2, .. của Enum để lưu DB => dễ bị sai khi sử Enum class, cần **BẮT BUỘC DÙNG** `EnumType.STRING` để lưu dạng chuỗi thay vì số thứ tự_
- `@Transient`: tương tự `Virtuals` của Moongoose
  > _`@Transient` được gắn lên field không muốn lưu vào DB, thường là trường dẫn xuất. Field sẽ được tính toán nội bộ trong RAM thay vì lưu vào DB_

**Vòng đời của `@Entity` trong `Persistence Context`:**

- `Transient` / `New`: object entity mới được khởi tạo, nằm trong bộ nhớ, EntityManager không biết sự tồn tại của object.
- `Managed` / `Persistent`: đang được quản lí. Khi này, object được đưa vào `Persistence Context` để quản lý.
  > _**`Dirty checking`**: khi object đang ở trạng thái `Managed`, nếu gán lại field của object, không cần gọi `.save()` / `.update()`. Khi kết thúc transaction, JPA tự soi chiếu - Dirty Checking - và sinh lệnh `UPDATE` tới DB_
- `Detected`: sandbox bị close / object bị detect khỏi `EntityManager` -> mọi thay đổi với object không còn được lưu tới DB.
- `Removed`: object vẫn nằm trong sandbox nhưng chờ tới lúc commit để thực hiện câu lệnh `DELETE`.

#### **1.5. `EntityTransaction`**

> _Làm việc với `SQL` -> tính `ACID` đảm bảo toàn vẹn dữ liệu là **TÔN CHỈ**_

- `EntityTransaction` giúp đảm bảo nguyên tắc "**All or Nothing**" => gắn chặt `1-1` với `Entity-Manager`
- Nhiệm vụ: khi thực nhiện nhóm task liên quan tới nhau (ex: thanh toán đơn hàng + trừ tiền) -> `EntityTransaction` thực hiện `group` 2 hành động này lại bằng lệnh `begin()`:
  - nếu không có lỗi xảy ra -> `EntityTransaction` thực hiện `commit()` xuống DB.
  - nếu có lỗi -> thực hiện `rollback()` để hủy transaction

#### **1.6. `Query`/`TypedQuery ` and `JPQL` - Java Persistence Query Language**

> _Thay vì viết SQL, Java sinh ra ngôn ngữ truy vấn trực tiếp trên Class và Class's fields_

Thay vì:

```sql
SELECT * FROM users WHERE email = "abc@gmail.com";
```

JPQL:

```kotlin
SELECT u FROM User u WHERE u.email = :email
```

for more details: [JPQL example code](./JPQL.md)

## **2. `Hibernate`**

> _Bản chất **JPA không thể kết nối DB**, nó cung cấp bộ chỉ thị - `specification`. **Hibernate** là `ORM Framework` thực thụ, đảm nhiệm việc thực thi các chỉ thị từ JPA_

**Hibernate** là ORM, đảm nhiệm:

- Dịch các specification `@Entity` của JPA thành các câu lệnh `CREATE TABLE`, `INSERT INTO`
- Mở kết nối và thao tác với DB.

Hibernate là ORM Framework mạnh mẽ và phức tạp, giúp:

- Quản lí `catching`
- Quản lí `transaction`
- Tối ưu hóa `SQL commands` ngầm

## **3. Spring Data JPA**

view details: [Spring Data JPA](../spring-data-jpa/README.md)
