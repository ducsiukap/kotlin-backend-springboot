package com.vduczz.ksb_demo.model.demo

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID
import org.hibernate.annotations.BatchSize
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

// nếu muốn gắn batchsize khi load Post ->User
// phải gắn ở đầu class entity:)
// @BatchSize(1000)
@Entity // => đánh dấu Entity cho Hibernate quản lý  (mapping với DB table)
@Table(name = "users") // custom DB table, mặc định Hibernate lấy classname (User) làm table name
@EntityListeners(AuditingEntityListener::class) // enable Auto-Auditing
class UserDemo(
        // sử dụng class thường
        // không dùng data class

        @Id // Primary key
        @GeneratedValue(strategy = GenerationType.UUID) // Auto generated value
        // UUID: string, safe
        // IDENTITY: AUTO-INCREMENT -> 0, 1, 2, ...
        @Column(name = "id", updatable = false, nullable = false) // DB table column
        var id: UUID? = null, // ID phải nullable
        // Column bắt buộc là `var`

        @Column(
                name = "full_name", // column's name
                nullable = false,
                length = 150 // VARCHAR(150)
        )
        var fullName: String,
        @Column(name = "email", nullable = false, length = 20, unique = true) var email: String,
        @Enumerated(EnumType.STRING) // Enum-type column
        // EnumType.String -> string: banned, active, ...
        // default or EnumType.ORDINAL -> ordinal: 1, 2, 3 -> dễ sai nếu có thay đổi
        @Column(name = "status", nullable = false, length = 20)
        var status: UserStatus = UserStatus.ACTIVE, // default=ACTIVE
        @Transient // không lưu db
        // mặc định, không khai báo @Column vẫn lưu DB
        var age: Int? = null,

        // ----------------------------------------
        // @Batchsize cho N+1 problem
        @BatchSize(size = 200) // nên từ 100-1000
        // inversing side
        // inversing side không nhất thiết cần có references tới owning,
        // tuy nhiên theo Hibernate khuyến khích nên mapping 2 chiều
        @OneToMany(
                mappedBy = "user", // khoá ngoại được giữ bởi `user` của Post
                // mappedBy dành cho phía không sở hữu fk

                cascade = [CascadeType.ALL],
                // cascade: lan truyền hành động từ inverse sang owner
                //      - CascadeType.PERSIST: lây lan khi INSERT
                //          + tạo mới User user + thêm 3 post vào user.posts
                //              => userRepository.save(user) sẽ tự động insert uset + 3 câu insert
                // post
                //      - CascadeType.REMOVE -> lây lan khi xóa
                //          + userRepository.delete(user) -> tự sinh code xóa post của user trước
                // khi xóa user
                //      - CascadeType.ALL = PERSIST + REMOVE

                orphanRemoval = true,
                // orphan = mồ côi:)
                // ex: khi user.removePost(post):
                //  + KHÔNG CÓ: orphanRemoval=true -> post vẫn được lưu dưới DB với user_id = null
                // (nullable) -> rác
                //  + CÓ: orphanRemoval=true -> Hibernate tự sinh lệnh xóa DELETE ... để xóa post đó
                // trong DB

                fetch = FetchType.LAZY // luôn dùng LAZY
        )
        var posts: MutableList<PostDemo> = mutableListOf()
) {

    // Auto-Auditing
    //  -> auto-save time
    //      CreatedAt
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: LocalDateTime? = null

    //      UpdateAt
    @LastModifiedDate @Column(name = "updated_at") var updatedAt: LocalDateTime? = null

    // ======================================
    // utility method: đồng bộ 2 chiều
    fun addPost(post: PostDemo) {
        posts.add(post)
        post.user = this
    }

    fun removePost(post: PostDemo) {
        posts.remove(post)

        // hủy relation
        // - do nothing và bật orphanRemoval -> hibernate tự sinh sql xóa post sau khi remove
        // - cho phép null
        //      + user của post là nullable: User?
        //      => post.user=null
        //          => post vẫn được giữ trên RAM và có thể gán cho User khác
    }

    // =======================================
    // do không đuợc dùng data class
    // -> phải viết equals, hashCode dựa trên ID
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserDemo) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()
}

enum class UserStatus {
    ACTIVE,
    BANNED,
    PENDING
}
