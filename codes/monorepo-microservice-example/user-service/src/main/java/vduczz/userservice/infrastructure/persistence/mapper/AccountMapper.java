package vduczz.userservice.infrastructure.persistence.mapper;

import vduczz.userservice.domain.model.Account;
import vduczz.userservice.domain.model.Email;
import vduczz.userservice.domain.model.Password;
import vduczz.userservice.infrastructure.persistence.entity.AccountEntity;

public final class AccountMapper {
    private AccountMapper() {
    }

    // Model -> Entity
    private static AccountEntity toEntity(Account account, boolean isNew) {

        return AccountEntity.builder()
                .id(account.getId())
                .name(account.getName())
                .email(account.getEmail().email())
                .password(account.getPassword().password())
                .isNewEntity(isNew)
                .build();
    }

    public static AccountEntity toNewEntity(Account account) {
        return toEntity(account, true);
    }

    public static AccountEntity toExistingEntity(Account account) {
        return toEntity(account, false);
    }

    // Entity -> Model
    public static Account toAccount(AccountEntity entity) {
        if (entity == null) return null;
        return Account.load(
                entity.getId(),
                entity.getName(),
                new Email(entity.getEmail()),
                new Password(entity.getPassword())
        );
    }
}
