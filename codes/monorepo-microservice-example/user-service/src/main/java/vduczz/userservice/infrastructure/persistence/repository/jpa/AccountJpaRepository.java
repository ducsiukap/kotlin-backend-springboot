package vduczz.userservice.infrastructure.persistence.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vduczz.userservice.infrastructure.persistence.entity.AccountEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountJpaRepository extends JpaRepository<AccountEntity, UUID> {
    boolean existsByEmail(String email);

    Optional<AccountEntity> findFirstByEmail(String email);
}
