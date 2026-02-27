# Database Migration - Schema version management

### **1. Tại sao chỉ bật `spring.jpa.hibernate.ddl-auto: update` lúc Dev?**

Khi sử dụng `spring.jpa.hibernate.ddl-auto: update`, mọi thao tác tạo/sửa/xóa table đều được **Hibernate** tự động sinh code để cập nhật DB.

Nhưng, khi làm việc nhóm hoặc thao tác trên **Production** dễ dẫn tới DB không khớp.

**Giải pháp**: Database Migration - mọi thay đổi về cấu trúc table phải **BẮT BUỘC** được ghi lại thành từng file lịch sử.

### **2. **Flyway** vs **Liquibase**:**

- `Flyway`: mọi thứ được viết bằng SQL thuần => Đơn giản, dễ hiểu, tốc độ.
- `Liquibase`: không viết SQL, sử dụng **XML**, **YAML** hoặc **JSON** -> **Liquibase** tự đọc file đó và dịch ra DB tương ứng như MySQL, Oracle, PostgreSQL, ...
  > _`Liquibase`: xịn, hỗ trợ `rollback` - lùi version db._

### **3. Triển khai `Flyway` vào Spring Boot**

#### _3.1. Dependencies_

- details: [build.gradle.kts](/codes/ksb-demo/build.gradle.kts)

- hủy `ddl-auto: update` và config `flyway`: [application.properties](/codes/ksb-demo/src/main/resources/application.properties)

#### _3.2. implementations_

- create `/resources/db/migration` directory để chứa toàn bộ file `.sql`
- Quy luật đặt tên:

  ```text
  V<version_number>__<description>.sql

  - V: required, uppercase
  - version_number: can be 1, 2, 3, ... or 1.1, 1.2, ... or using date likely 20260227, ...
  - description: summary, lowercase, separate by underscore(_), ex: init_database, add_dob_to_user
  ```

examle: [V1\_\_init_database.sql](/codes/ksb-demo/src/main/resources/db.migration/V1__init_database.sql)

> _khi có bất cứ thay đổi nào với DB/table -> cần tạo file `.sql` mới_

#### _3.3. Hoạt động của Flyway_

Khi chạy server, `Flyway` thực hiện:

- **step 1**: quét directory: `resources/db/migration`
- **step 2**: so sánh với bảng lưu lịch sử: `flyway_schema_history` trong DB, chứa:
  - `version`
  - `description`
  - `checksum`
  - `installed_on`
  - `success`
- **step 3**: kiếm tra version:
  - đọc toàn bộ file migration
  - đọc `flyway_schema_history`
  - so sánh version chưa chạy -> chạy theo thứ tự tăng dần
