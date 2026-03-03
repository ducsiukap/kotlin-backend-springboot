package com.vduczz.mini_project.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.util.UUID
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    var id: UUID? = null,

    @Column(name = "first_name", nullable = false, length = 150)
    var firstName: String,

    @Column(name = "last_name", nullable = false, length = 50)
    var lastName: String,

    @Column(length = 50)
    var email: String,

    @Column(name = "day_of_birth", nullable = false)
    var dayOfBirth: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: UserStatus = UserStatus.INACTIVED,

    // ============================================================
    // Spring security
    // do username -> kotlin sinh hàm getUsername() trong java
    // -> trung với getUsername() của UserDetails -> không thể override
    //      + giải pháp 1: private field -> không có get..
    //      + giải pháp 2: @get:JvmName("jvmName")
    //              để kotlin gen hàm get trong java lấy tên là "jvmName"
    @Column(nullable = false, length = 50)
    @get:JvmName("getDbUsername")
    var username: String,

    @Column(nullable = false, length = 100)
    @get:JvmName("getDbPassword") // tương tự username
    var password: String,

    // bổ sung role cho authorization
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var role: UserRoles = UserRoles.USER,

    // ------------------------------------------------------------
    // references to user's posts
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var posts: MutableList<Post> = mutableListOf(),

// ------------------------------------------------------------
// user's reactions
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var reactions: MutableList<Reaction> = mutableListOf(),

// ------------------------------------------------------------
// user's comment
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var comments: MutableList<Comment> = mutableListOf(),

// ============================================================
// Spring Security
// implementations UserDetails
) : UserDetails {
    // ============================================================
    // @Transient -> ẩn field khỏi db
    @get:Transient // transient
    val fullName: String // derived field
        get() = "$firstName $lastName"

    @get:Transient
    val age: Int
        get() = Period.between(dayOfBirth, LocalDate.now()).years

    // ============================================================
    // auto-auditing
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: LocalDateTime? = null

    @LastModifiedDate
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null

    // ============================================================
    // data class methods
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    // ============================================================
    // update related-entity
    // User's posts -> Post
    fun addPost(post: Post) {
        post.user = this
        this.posts.add(post)
    }

    fun removePost(post: Post) {
        this.posts.remove(post)
    }

    // ============================================================
    // Spring Security
    // override UserDetails's methods

    // getUsername + getPassword -> credentials
    override fun getUsername(): String {
        // getUsername trả về thứ được làm 'username' trong auth
        // ex: username / email, ...
        return this.username
    }

    override fun getPassword(): String = this.password

    // authorization
    // -> trả về danh sách quyền của user (roles)
    // phải có tiền tố ROLE_, ex: ROLE_ADMIN
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_${this.role.name}"))
    }

    // account status
    // spring security sinh ra cho banking / army
    // -> cần quản lí trạng thái tài khoản chặt chẽ
    // 4 cấp độ:
    //      + isAccountNonExpired -> account chưa hết hạn ?
    //      + isAccountNonLocked -> account không bị tạm khóa ? // ex: acc bị tạm khóa do sai pw 5 lần, ...
    //      + isCredentialsNonExpired -> thông tin đăng nhập chưa hết hạn ? // ex: pw hết hạn sau 30days, ...
    //      + isEnabled -> account được cấp phép?
    // tuy nhiên, đa số dự án không cần tất cả:
    //      + isAccountNonExpired / isAccountNonLocked / isCredentialsNonExpired -> always true -> pass
    //      + isEnabled: đưa toàn bộ logic giám sát tài khoản vào hàm này
    //          => true -> được vào / false -> reject
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true

    // đưa logic về trạng thái tài khoản vào isEnabled
    override fun isEnabled(): Boolean {
        // INACTIVED -> active later

        return this.status != UserStatus.BANNED
    }
}

enum class UserStatus {
    BANNED,
    ACTIVED,
    INACTIVED
}

enum class UserRoles {
    ADMIN,
    USER
}