package com.vduczz.ksb_demo.model.demo

import jakarta.persistence.*
import org.hibernate.annotations.BatchSize
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "posts")
@EntityListeners(AuditingEntityListener::class) // enable auto-auditing
class PostDemo(
    @Id @GeneratedValue(strategy = GenerationType.UUID) var id: UUID? = null,

    @Column(nullable = false) var title: String,

    @Column(nullable = true, columnDefinition = "TEXT") var content: String? = null,


    // --------------------------
    // Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    //  X to Y: ex: ManyToOne
    //      - current entity: X (Many)
    //      - relative entity: Y (One)
    //      X,Y can be: Many/One
    //  FetchType -> khi load object, có load luôn entity liên quan?
    //      FetchType.LAZY: chỉ load khi dùng tới // nên LUÔN LUÔN sử dụng
    //      FetchType:EAGER: load cùng với entity hiện tại // TRÁNH
    //  default fetchType:
    //  - ManyToOne / OneToOne : EAGER (nên đổi sang LAZY)
    //  - OneToMany / ManyToMany : Lazy
    @JoinColumn(name = "user_id", nullable = false) // chỉ định tên cột fk trong DB
    var user: UserDemo,
) {
    // equals / hashcode
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PostDemo) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    // Auto-Auditing
    @CreatedDate
    @Column(
        name = "created_at",
        updatable = false,
        nullable = false,
    )
    var createdAt: LocalDateTime? = null

    @LastModifiedDate
    @Column(
        name = "updated_at"
    )
    var updatedAt: LocalDateTime? = null
}