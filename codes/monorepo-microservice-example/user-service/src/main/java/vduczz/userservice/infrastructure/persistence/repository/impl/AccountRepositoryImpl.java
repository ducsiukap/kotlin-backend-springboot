package vduczz.userservice.infrastructure.persistence.repository.impl;

import org.springframework.stereotype.Repository;
import vduczz.userservice.domain.model.Account;
import vduczz.userservice.domain.model.Email;
import vduczz.userservice.domain.repository.AccountRepository;
import vduczz.userservice.infrastructure.persistence.entity.AccountEntity;
import vduczz.userservice.infrastructure.persistence.mapper.AccountMapper;
import vduczz.userservice.infrastructure.persistence.repository.jpa.AccountJpaRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class AccountRepositoryImpl implements AccountRepository {

    private final AccountJpaRepository jpaRepository;

    public AccountRepositoryImpl(AccountJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Account save(Account account) {
        // check
        boolean isNew = !jpaRepository.existsByEmail(account.getEmail().email());

        AccountEntity accountEntity;
        // map
        if (isNew)
            accountEntity = AccountMapper.toNewEntity(account);
        else
            accountEntity = AccountMapper.toExistingEntity(account);

        AccountEntity entity = jpaRepository.save(accountEntity);
        return AccountMapper.toAccount(entity);
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return jpaRepository.findById(id).map(AccountMapper::toAccount);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email.email());
    }

    @Override
    public Optional<Account> findByEmail(Email email) {
        return jpaRepository.findFirstByEmail(email.email()).map(AccountMapper::toAccount);
    }
}
