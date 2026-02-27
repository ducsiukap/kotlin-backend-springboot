# `Query` / `TypedQuery` and Java Persistence Query Language (JPQL)

## **1. `Query` / `TypedQuery`**

> _Khi đưa `JPQL` cho `EntityManager` chạy, nó trả về **object để thao tác** (truyền tham số, paging, lấy kết quả, ...) -> là `Query` hoặc `TypedQuery`_

- `Query`: **type-unsafe**, cũ:
  - JPA không biết kết quả trả về là class nào -> `List<Any>` or `List<Array<Any>>`  
    => phải tự ép kiểu thủ công.
- `TypedQuery`: **type-safe**, mới:
  - Khi tạo câu lệnh, cần truyền thêm class mà kết quả mong muốn nhận về, ex: `User::class.java  
    => Không cần ép kiểu, an toàn ngay từ lúc code - compile-time

## **2. `JPQL` - Jakarta Persistence Query Language**

> _JPQL = Object-Oriented Query Language, không cần biết Table, chỉ cần biết Entity class_

|       **JPQL**       |    **SQL**     |
| :------------------: | :------------: |
| Query `entity` class | Query `table`  |
|    Query `field`     | Query `column` |
|   return `object`    |  return `row`  |

```Kotlin
// JPQL basic syntax:
SELECT ...
FROM ...
WHERE ...
GROUP BY ...
HAVING ...
ORDER BY ...
// dựa trên Entity name
```

### **2.1. Select entity**

```kotlin
// select all field
@Query("SELECT u FROM User u")
fun selectAllField(): List<User>

// select columns
@Query("SELECT u.fullName, u.email FROM User u")
fun selectNameAndEmail(): List<Array<Any>>

// User: Entity class
// u: alias

// Không có *
```

### **2.2. Select by condition**

```kotlin
// WHERE condition
// support:
//  =, <>, <, <=, >, >=, BETWEEN, LIKE, IN, IS NULL

@Query("SELECT u FROM User u WHERE u.age >= 18")
fun findValidAgeUser():List<User>

@Query("SELECT u FROM User u WHERE u.fullName LIKE %keyword%")
// ...

@Query("SELECT u FROM User u WHERE u.age BETWEEN 18 AND 36")
// ...
```

### **2.3. Argument passing**

```kotlin

// Named Parameter -> safe
// use `:paramName` for paramenter
@Query("SELECT u FROM User u WHERE u.status IN :statusList") // parameter: statusList
fun findByUSerStatus(
    // @Param("paramName") to passing parameter
    @Param("statusList") statused: List<String>
): List<User>

// Position Parameter -> unsafe
// use: `?index`
@Query("SELECT u FROM User u WHERE u.age BETWEEN ?1 AND ?2")
fun findByAgeRange(
    minAge: Int,
    maxAge: Int
) : User?
// -> vị trí param ở function phải khớp với trong Query
```

### **2.4. `JOIN`**

```kotlin
// Implicit JOIN
@Query("SELECT p FROM Post p WHERE p.user.email = :email") // tự động join với User qua p.user
fun findPostByEmail(@Param("email") email: String): List<Post>

// Explicit JOIN
@Query("SELECT DISTINCT u FROM User u JOIN u.posts p WHERE p.title LIKE %:keyword%")
fun findUserByPostIncludingKeyword(@Param("keyword") keyword: String): List<User>

// support
// + JOIN (INNER JOIN)
// + LEFT JOIN
// + CROSS JOIN
@Query("SELECT u, p FROM User u, Post p")

// JOIN FETCH / LEFT JOIN FETCH
```

### **2.5. `DTO` - Constructor Expression**

```kotlin
@Query("""
    SELECT  new com.example.dto.UserDTO(u.id, u.email)
    FROM User u
    WHERE u.status = 'ACTIVE'
""")
fun getUserByUserDTO(): List<UserDTO>
```

### **2.6. `Aggregate` Function**

```kotlin
// JPQL hỗ trợ
//  COUNT, SUM, MAX, MIN, AVG

@Query("SELECT COUNT(u) FROM User u")

@Query("""
    SELECT u.age, COUNT(u)
    FROM User u
    GROUP BY u.age
""")
```

### **2.7. `Subquery`**

> _Subquery chỉ được dùng trong WHERE / HAVING_

### **2.8.`UPDATE` / `DELETE`**

> _Cần `@Modifying`_

## **3. Custom query**

### **3.1. Sử dụng `@Query` cuả Spring Data JPA**

> _Hầu hết các trường hợp sử dụng `@Query`_

- Bản chất: `static query` -> JPQL được xác định ngay khi compile code
- Advantage: Code nhanh, ngắn, sử dụng trực tiếp trong interface
  - hỗ trợ phân trang -`Pageable`, sắp xếp - `Sort`
  - tự chống `SQL Injection`
- Disadvantage: không linh hoạt

```kotlin
interface UserRepository: JpaRepository <User, UUID> {
    // query command bị fix cứng
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.status = :status")
    fun findUserByNameAndEmail(
        @Param("email") email: String,
        @Param("status") status: UserStatus
    ): List<User>

    // JPQL không có LIMIT 1
    // Để giải quyết khi sử dụng Spring Data JPA:
    //  - cần thêm First vào tên hàm
    //  - hoặc truyền PageRequest.of(0, 1) vào tham số hàm

    // Pageable - phân trang
    // example code:
    val pageNumber = 0 // first-page
    val pageSize = 10 // limit
    val pageable: Pageable = PageRequest.of(pageNumber, pageSize)
    // Sort
    // simple sort
    val sort = Sort.by(Sort.Direction.DESC."fullName")
    // complex sort
    val compSort = Sort.by(Sort.Direction.ASC, "fullName").and(Sort.by(Sort.Direction.ASC, "email"))
    // Pageable + Sort
    val pageableAndSort: Pageable = PageRequest.of(
        pageNumber, pageSize, sort
    )

    // sử dụng Pageable / Sort trong Repository
    interface UserRepository : JpaRepository<User, UUID> {
        @Query("SELECT u FROM User u WHERE u.fullName LIKE %:keyword%")
        fun findAndPaging(
            @Param("keyword") keyword: String,
            pageable: Pageable
        ) : Page<User>
    }
}
```

### **3.2. Sử dụng `EntityManager` - JPA thuần**

> _Sử dụng `EntityManager` (hoặc các công cụ như JPA Specification / Criteria API / QueryDSL) khi filter/search phức tạp, nhiều điều kiện_

- Bản chất: cho phép `dynamic query`
- Advantage: giải quyết vấn đề linh hoạt - dynamic filter
- Disadvantage:
  - Phải tự viết logic phân trang
  - Dễ dính `ClassCastException` nếu dùng `Query` thay vì `TypedQuery`

```kotlin
@Service
class UserCustomSearchService(
    // yêu cầu inject EntityManager
    @PersistenceContext
    private val entityManager: EntityManager
) {
    //
    fun findByDynamicFilter(
        name: String?,
        email: String?
    ) : List<User> {
        // JPQL
        val jpql = StringBuilder("SELECT u FROM User u WHERE 1=1")

        // params map
        val params = mutableMapOf<String, Any>()

        // dynamic logic
        if (!name.isNullOrBlank()) {
            jpql.append(" AND u.fullName LIKE :name")
            params["name"] = "%$name%"
        }
        if (!email.isNullOrBlank()) {
            jpql.append(" AND u.email = :email")
            params["email"] = email
        }

        // createQuery
        val query = entityManager.createQuery(jpql.toString(), User::class.java) // TypedQuery
        // val query = entityManager.createQuery(jpql.toString()) // Query

        // params passing
        params.forEach{ (key, value) ->
            query.setParameter(key, value)
        }

        // do query
        return query.resultList
    }
}
```

### **3.3. Native SQL**

```kotlin
// sử dụng nativeQuery=true trong @Query
@Query(value="...", nativeQuery=true)
fun ...

// khi paging, phải tự viết count:)
@Query(
    nativeQuery=true,
    value="SELECT * FROM users WHERE status = :status",
    // countQuery phục vụ paging
    countQuery = "SELECT count(*) FROM users WHERE status= :status"
)
fun ... (
    @Param("status") status: String,
    pageable: Pageable
): Page<User>
```
