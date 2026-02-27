package com.vduczz.mini_project.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.util.UUID
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
class User(
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        @Column(name = "id", updatable = false, nullable = false)
        var id: UUID? = null,
        @Column(name = "first_name", nullable = false, length = 150) var firstName: String,
        @Column(name = "last_name", nullable = false, length = 50) var lastName: String,
        @Column(length = 50) var email: String,
        @Column(name = "day_of_birth", nullable = false) var dayOfBirth: LocalDate,
        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 20)
        var status: UserStatus = UserStatus.INACTIVE,
        @Column(nullable = false, length = 50) var username: String,
        @Column(nullable = false, length = 50) var password: String,

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
) {
    // ------------------------------------------------------------
    @get:Transient // transient
    val fullName: String // derived field
        get() = "$firstName $lastName"

    @get:Transient
    val age: Int
        get() = Period.between(LocalDate.now(), dayOfBirth).years

    // ------------------------------------------------------------
    // auto-auditing
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: LocalDateTime? = null

    @LastModifiedDate @Column(name = "updated_at") var updatedAt: LocalDateTime? = null

    // ------------------------------------------------------------
    // data class methods
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    // ------------------------------------------------------------
    // update User's posts
    fun addPost(post: Post) {
        post.user = this
        this.posts.add(post)
    }

    fun removePost(post: Post) {
        this.posts.remove(post)
    }
}

enum class UserStatus {
    BANNED,
    ACTIVED,
    INACTIVE
}
