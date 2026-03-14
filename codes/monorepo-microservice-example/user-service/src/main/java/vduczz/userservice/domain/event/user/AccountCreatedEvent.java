package vduczz.userservice.domain.event.user;

import lombok.Getter;

import java.util.UUID;

@Getter
public class AccountCreatedEvent extends BaseEvent {
    private final String name;
    private final String email;

    private AccountCreatedEvent(UUID id, String name, String email) {
        super(id);
        this.name = name;
        this.email = email;
    }

    public static AccountCreatedEvent create(UUID id, String name, String email) {
        return new AccountCreatedEvent(id, name, email);
    }
}
