package com.vduczz.mini_project.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(name = "reactions")
@EntityListeners(AuditingEntityListener::class)
class Reaction(
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        @field:Column(name = "id", nullable = false)
        val id: UUID? = null,

        // User 1 - N Reaction
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        var user: User,

        // Post 1 - N Reaction
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "post_id", nullable = false)
        var post: Post,
) {
    // ------------------------------------------------------------
    // data class method
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Reaction) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    // ------------------------------------------------------------
    // auto-auditing
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: LocalDateTime? = null

    @LastModifiedDate @Column(name = "updated_at") var updatedAt: LocalDateTime? = null
}
