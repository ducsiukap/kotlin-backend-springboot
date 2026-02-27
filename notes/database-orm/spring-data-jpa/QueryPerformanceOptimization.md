# `N+1` Problem & Solutions

> _Để tối ưu hiệu năng, thường sử dụng `fetch = FetchType.LAZY`: khi lấy Entity hiện tại, thay vì lấy các `related-entities` thì trả về Proxy => **Khi nào thực sự dùng tới thì mới trả dữ liệu thật của `related-entity`**_

### **`N+1` problem**:

```kotlin
// UserDemo
// @OneToMany(
//     mappedBy = "user",
//     cascade = [CascadeType.ALL],
//     orphanRemoval = true,
//     fetch = FetchType.LAZY // luôn dùng LAZY
// )
// var posts: MutableList<PostDemo> = mutableListOf()

// lấy user
val users = userRepository.findByStatus(UserStatus.ACTIVE)
// do FetchType.LAZY -> posts trong mỗi user là 1 Proxy thay vì lấy ngay => hiệu suất cao
// FetchType.EAGER thì lấy ngay nhưng hiệu xuất thấp

// truy cập posts trong mỗi user
for (user in users) {
    println("User ${user.fullName} has ${user.posts.size} posts!")
    // khi này, mỗi lần lặp sẽ sinh lệnh SQL tới DB: SELECT * FROM posts WHERE user_id = ?
    // => N+1 Problems:
    //      + Lấy User -> mất 1 lệnh SQL
    //      + Lặp qua N user -> mất N lệnh SQL
    //      => tổng phải thực hiện N+1 Query
    // => cạn Connection Pool => nghẽn => giảm hiệu suất DB
}
```

### **Solutions**

> _Gom tất cả data cần thiết vào càng ít SQL commands càng tốt_

details: [UserRepositoryDemo.kt](/codes/ksb-demo/src/main/kotlin/com/vduczz/ksb_demo/repository/demo/UserRepositoryDemo.kt)

_`@Batchsize` giải quyết **JOIN FETCH + Pageable**:_

> _JOIN FETCH với quan hệ 1-N sẽ trả về N row với cùng 1 entity -> với M entity, mỗi entity có trung bình N row của related-entity -> trả về `M*N` row._

> _Pagination trên row: **load full result** + pagination trong memory => Out-of-memory_

- gắn `@Batchsize` vào phía nào có `FetchType.Lazy`: [PostDemo.kt](/codes/ksb-demo/src/main/kotlin/com/vduczz/ksb_demo/model/demo/PostDemo.kt)
  > hoặc có thể dùng auto-batchsize cho mọi Lazy: [application.properties](/codes/ksb-demo/src/main/resources/application.properties) => không cần set `@Batchsize`
- sử repository: [UserRepositoryDemo.kt](/codes/ksb-demo/src/main/kotlin/com/vduczz/ksb_demo/repository/demo/UserRepositoryDemo.kt)

> _hoặc 2-step query loại bỏ lazy loading_

```kotlin
// bỏ qua sử dụng user.posts để load

// 1. lấy user theo pageable
val pageUsers: Page<User> = userRepository.findByStatus(status, pageable)

// 2. lấy id -> nhóm lại
val userIds: List<UUID> = pageUsers.content.map { it.id }

// 3. query lần 2:
val danhSachPost: List<Post> = postRepository.findByUserIdIn(userIds)

// 4. mapping user-post bằng code vào DTO
```

**Tóm lại:**

- Khi load `main-entity`, cần data của `related-entity`:
  - **Non-Pagination**: `JOIN FETCH` / `@EntityGraph` -> sử dụng khi cần data của related-entity ngay và **KHÔNG CÓ PHÂN TRANG** or lấy đúng 1 object.
  - **Pagination**: `@Batchsize` ( or `2-step query` ) -> sử dụng khi cần thông tin của related-entity, cần sử dụng entity và **BẮT BUỘC PHÂN TRANG** (gom bằng `IN`)
    > _sử dụng `@Batchsize` hoặc cấu hình **default_batch_fetch_size**_
    - Quan hệ 1-N, kéo từ 1 sang N (đứng ở inverse lấy N owner) -> gắn `@Batchsize` ở inverse (field danh sách owner)
    - Kéo từ N sang 1 (đứng ở Owner lấy inverse) -> gắn ở **đầu class** inverse, không gắn ở biến tham chiếu trong owner
      > _-> luôn gắn ở inverse_
  - Chỉ thể hiện dữ liệu đơn giản / thống kê -> `DTO projection` -> `JOIN (+ GROUPBY)`
    ```kotlin
    @Query("""
        SELECT new com.example.PostDto(
            p.id,
            p.title,
            u.fullName,
            COUNT(c)
        )
        FROM Post p
        JOIN p.user u
        LEFT JOIN p.comments c
        GROUP BY p.id, p.title, u.fullName
    """)
    Page<PostDto> findPostSummary(Pageable pageable);
    ```
- Ngược lại, nếu chỉ cần `main-entity`, không cần thông tin của related entity -> giữ nguyên `Proxy`, không làm gì.
