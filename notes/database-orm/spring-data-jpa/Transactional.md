# `@Transactional`

## **All or nothing**

> _Principle: hoặc thành công tất cả task (commit), hoặc khôi phục lại ban đầu (rollback) nếu có bất kì task nào gặp lỗi_

**Sử dụng `@Transactional`**:

```kotlin
@Service
class BankService(private val accountRepo: AccoutRepository) {

    // @Transactional -> mở transaction
    @Transactinal(rollbackFor = [Exception::class])
    //  rollbackFor = [Exception::class] -> gặp bất cứ exception nào cũng rollback // LUÔN VIẾT ĐẦY ĐỦ
    //  default, chỉ rollback với RuntimeException
    fun moneyTransfer(fromId: UUID, toID: UUID, amount: Long) {
        // find account
        val srcAccount = accountRepo.findById(fromId)
        val dstAccount = accountRepo.findById(toId)

        // tru tien
        srcAccount.balance -= amount;
        accountRepo.save(srcAccount) // RAM / Cache of Hibernate
        // cong tien
        dstAccount.balance += amount;
        accountRepo.save(dstAccount) // RAM / Cache of Hibernate

        // khi hàm kết thúc an toàn, spring mới chính thức COMMIT cả 2 lệnh xuống DB
        // ngược lại, nếu dính lỗi, nó hủy toàn bộ các lệnh và rollback lại như trước khi gọi moneyTransfer()
    }
}
```

**Note**: `@Transactional` chạy bằng `Proxy` => nếu gọi transaction từ 1 hàm khác **TRONG CÙNG CLASS** -> `@Transactional` không có tác dụng. => `@Transactional` fun phải được gọi từ class khác.

## **`transaction` Propagation**

> _Khi `method A` có `transaction` gọi `method B` cũng có `transaction`_

Mặc định, **Spring** dùng cơ chế `Propagation.REQUIRED`:

> _Khi `B` chạy, nó thấy `A` đã mở sẵn `transaction` => **B sẽ KHÔNG tạo mới transaction mà DÙNG CHUNG transaction được mở bởi A**_

=> nếu B gặp `exception`, nó hủy luôn toàn bộ transaction (kể cả của A).

**`REQUIRES_NEW`**: để B tự chịu exception của nó, không ảnh hưởng tới A, sử dụng `Propagation.REQUIRES_NEW`

```kotlin
class Service1 ( //...
) {
    @Transactional(rollbackFor=[Exception::class])
    fun A() {
        // ....
        try {
            B() // call other transaction
            // Propagation.REQUIRES_NEW vẫn bắn exception lên cha -> cần try-catch
        } catch (e: Exception) {}
        // ...
    }
}

class Service2 ( //...
) {
    @Transactional(
        rollbackFor=[Exception::class],
        propagation = Progatation.REQUIRES_NEW // yêu cầu transaction mới, không ảnh hưởng tới transaction A
    )fun B() {}

// propagation:
//  + propagation = Propagation.REQUIRED (default)
    // -> dùng chung transaction với nơi gọi / nếu chưa có thì tạo mới
//  + propagation = Progatation.REQUIRES_NEW
    // -> mở transaction mới
    // *note: dù vậy, Exception vẫn được bắn lên transaction cha -> cần try-catch để tránh hủy transaction cha
//  + propagation = Progatation.MANDATORY
    // -> bắt buộc cha mở transaction trước
//  + propagation = Progatation.SUPPORTS
    // -> dùng chung với cha (nếu cha có transaction thì dùng, không thì không cần transaction)
//  + propagation = Progatation.NOT_SUPPORTED
    // -> suspend transaction cha cho tới khi nó chạy xong
    // chạy không dùng transaction
    // => for SUPER-HEAVY TASK (call 3rd API, ...)
//  + propagation = Progatation.NEVER
    // -> không chạy trong cha có transaction
//  + propagation = Progatation.NESTED
    // -> tạo savepoint trong transaction cha, nếu nó lỗi sẽ rollback lại savepoint đó
}
```

## **Isolation**

> _Isolation quyết định việc transaction thấy dữ liệu của nhau như nào_

Mặc định, **Spring** sử dụng `READ_COMMITTED` (hoặc `REPEATABLE_READ` với `MySQL`) => Chỉ cho phép đọc những đa ta nào **ĐÃ ĐƯỢC COMMIT**.

```kotlin
@Transactional(
    isolation=Isolation.READ_COMMITTED // default
)
fun ...

// isolation:
//      + Isolation.READ_COMMITTED: chỉ đọc dữ liệu đã commit => ngăn DIRTY_READ // mặc định của nhiều DBMS
//          *note: vẫn có non-repeatable read => đọc dữ liệu mới nhất của 1 row dù trước đó đã đọc
//      + Isolation.REPEATABLE_READ: đảm bảo nếu đọc 1 row rồi thì khi đọc lại vẫn là dữ liệu cũ
//          *note: mặc định của MySQL InnoDB
//      + Isolation.READ_UNCOMMITTED: cho phép đọc dữ liệu uncommitted của transaction khác => performance cao nhất
//          *note: MySQL không thật sự hỗ trợ mức này
//      + Isolation.SERIALIZABLE -> mọi transaction chạy tuần tự => performance giảm mạnh
```

## **`@Transaction(readOnly = true)`: tăng tốc truy vấn**

> _nếu chỉ đọc thì cần gì transaction? => **performance**_

Bình thường, khi lấy data bằng `Hibernate`, nó lưu 2 bản:

- 1 bản để sử dụng
- 1 bản làm `snapshot`

Đến cuối hàm, `Hibernate` thực hiện **Dirty Checking** để so sánh với `snapshot` và sinh lệnh cập nhật (`UPDATE`, nếu có) tới DB.

> _Quá trình này yêu cầu RAM/CPU phải hoạt động nhiều_

**Với `@Transactional(readOnly=true)`**:

> _`@Transaction` với thuộc tính `readOnly=true` đánh dấu với **Hibernate** rằng đó là hàm chỉ đọc (`SELECT`)_

Khi này, `Hibernate` tự động bỏ qua cơ chế `snapshot` và **Dirty Checking** => `RAM` / `CPU` được giải phóng giúp **tốc độ API tăng lên** đáng kể
