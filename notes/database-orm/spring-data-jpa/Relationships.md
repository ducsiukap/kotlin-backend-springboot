# Entity `Relationship`

## **1. Owner side vs Inverse side**

> _`User` có nhiều `Post`, ngược lại, `Post` chỉ thuộc về 1 `User` -> **`User-Post (1-N)`**_

Do **User 1-N Post** -> `posts` phải có fk - `foreign-key` - trỏ tới `users`: **`user_id`**

- **`Post`** - Owning Side: Post là class giữ khóa ngoại vì trong DB, `posts` phải chứa cột `user_id`
- **`User`** - Inverse Side: bảng `users` không cần thông tin gì của `posts`, nó đóng vai trò làm tham chiếu cho `posts`. _**Tuy nhiên, theo Hibernate, cả 2 class đều phải trỏ vào nhau**_

## **2. Implementations**

- Owning Side: [PostDemo.kt](/codes/ksb-demo/src/main/kotlin/com/vduczz/ksb_demo/model/demo/PostDemo.kt)
- Inverse Side: [UserDemo.kt](/codes/ksb-demo/src/main/kotlin/com/vduczz/ksb_demo/model/demo/UserDemo.kt)

## **3. Repositories**

details: [PostDemoRepository.kt](/codes/ksb-demo/src/main/kotlin/com/vduczz/ksb_demo/repository/demo/PostRepositoryDemo.kt)
