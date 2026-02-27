# `Race Condition` & Locking

> _**Race condition** - tranh chấp dữ liệu: **2 hay nhiều request** đều **yêu cầu sửa cùng một data ** trong **cùng 1 thời điểm** có thể dẫn tới server thực hiện `đúng code logic` nhưng `sai business logic`_

## **`Optimistic Locking` - khóa lạc quan: _Rẻ_, _Nhanh_ và _Phổ biến_.**

> _Bổ sung `@Version` vào Entity => **Spring** và **Hibernate** tự động quản lý biến này -> chặn `UPDATE`_

```kotlin
@Entity
class Ticket(
    @Id var id: UUID,
    var quantity: Int,

    // version cho entity hiện tại
    // => spring và hibernate tự động quản lý
    @Version
    var version: Long=0
    // khi có update -> version thay đổi
    // => update sau => khác version
    //      => trả về ObjectOptimisticLockingFailureException
    //      -> cần try-catch và xử lý (trả về phản hồi, ...)
)
```

**Ưu điểm**:

- Server nhẹ, không làm chậm DB, không khóa dòng vật lý.
- 90% các tính năng sửa thông thường

## **`Permission Locking` - khóa bi quan: an toàn**

> _Khi có 1 request yêu cầu dữ liệu đó => **khóa cứng** và **không cho phép truy cập**, (kể cả đọc) **cho tới khi request đó dùng xong**._

> _`@Lock`: sử dụng cho `repository's method`_

```kotlin
@Repository
interface TicketRepository: JpaRepository<Ticket, UUID> {

    // Sử dụng @Lock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    // LockModeType:
    //  + OPTIMISTIC: dùng với Entity có @Version -> OptimisticLockException
    //      // thực tế thường chỉ cần @Version, không cần OPTIMISTIC
    //  + OPTIMISTIC_FORCE_INCREMENT: tương tự OPTIMISTIC nhưng tăng version kể cả không update (chỉ đọc)
    //  + PESSIMISTIC_READ: lock shared -> không update được nhưng readable
    //  + PESSIMISTIC_WRITE: lock exclusive -> unreadable / unwriteable

    @Query("...")
    fun ...
}
```
