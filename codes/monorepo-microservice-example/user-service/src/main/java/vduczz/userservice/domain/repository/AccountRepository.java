package vduczz.userservice.domain.repository;

import vduczz.userservice.domain.model.Account;
import vduczz.userservice.domain.model.Email;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {

    Optional<Account> findByEmail(Email email);

    Account save(Account account);

    Optional<Account> findById(UUID id);

    boolean existsByEmail(Email email);
}
