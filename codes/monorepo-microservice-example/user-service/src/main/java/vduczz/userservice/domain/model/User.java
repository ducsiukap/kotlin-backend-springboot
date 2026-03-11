package vduczz.userservice.domain.model;

import lombok.Getter;
import lombok.Setter;
import vduczz.userservice.domain.exception.common.InvalidArgumentException;

import java.util.UUID;

@Getter
public class User {
    private final UUID id;
    private Account account;
    @Setter // nullable field
    private String address;

    private User(UUID id, Account account, String address) {
        this.id = id;
        this.account = account;
        this.address = address;
    }

    // validation
    private static void validateAccount(Account account) {
        if (account == null) throw new InvalidArgumentException("account:null");
    }

    public static User load(UUID id, Account account, String address) {
        return new User(id, account, address);
    }

    public static User create(Account account, String address) {
        validateAccount(account);

        return new User(UUID.randomUUID(), account, address);
    }

    public void setAccount(Account account) {
        validateAccount(account);
        this.account = account;
    }

}
