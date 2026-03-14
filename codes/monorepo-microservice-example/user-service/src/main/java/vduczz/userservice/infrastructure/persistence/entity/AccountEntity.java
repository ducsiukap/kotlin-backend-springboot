package vduczz.userservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;
import vduczz.userservice.domain.model.Account;

import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountEntity extends BaseEntity implements Persistable<UUID> {
    @Id
    // không @GeneratedValue
    @Column(columnDefinition = "CHAR(36)", updatable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id; // UUID chuẩn có 36 kí tự

    @Column(nullable = false)
    private String name;
    @Column(nullable = false, length = 100)
    private String email;
    @Column(nullable = false)
    private String password;

    // ============================================================
    // Persistable
    @Transient
    boolean isNewEntity;

    @Override
    public UUID getId() {
        return this.id;
    }

    @Override
    public boolean isNew() {
        return this.isNewEntity;
    }

    @PostLoad
    @PostPersist
    void makeNotNew() {
        this.isNewEntity = false;
    }
    // ============================================================
}
