package vduczz.userservice.domain.model;

import lombok.Getter;
import vduczz.userservice.domain.exception.auth.PasswordContainingNameException;
import vduczz.userservice.domain.exception.auth.SameEmailException;
import vduczz.userservice.domain.exception.auth.SamePasswordException;
import vduczz.userservice.domain.exception.common.InvalidArgumentException;

import java.util.Arrays;
import java.util.UUID;

import static vduczz.userservice.domain.utils.Utilities.normalizeString;

// Model
@Getter
public class Account {
    private final UUID id;
    private String name;
    private Password password;
    private Email email;

    protected Account(UUID id, String name, Email email, Password password) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.email = email;
    }

    // ------------------------------------------------------------
    // validate
    private static void validatePasswordContainsName(String name, Password password) {
        String normalizeName = normalizeString(name);
        String normalizePassword = normalizeString(password.password());

        String[] nameTokens = normalizeName.split("\\s+");
        if (Arrays.stream(nameTokens)
                .filter(token -> token.length() > 2)
                .anyMatch(normalizePassword::contains)
        )
            throw new PasswordContainingNameException();
    }

    private static void validateName(String name) {
        //...
        if (name == null || name.isBlank())
            throw new InvalidArgumentException("name");
    }

    private static void validateNameAndPassword(String name, Password password) {
        validateName(name);
        if (password == null)
            throw new InvalidArgumentException("password");
        validatePasswordContainsName(name, password);
    }

    // ============================================================
    // Business core
    // Factory method
    public static Account load(UUID id, String name, Email email, Password password) {
        // load từ db -> không cần checking
        return new Account(id, name.trim(), email, password);
    }

    public static Account create(String name, Email email, Password password) {
        // tạo mới account -> cần check
        validateNameAndPassword(name, password);

        if (email == null) throw new InvalidArgumentException("email");

        return new Account(
                UUID.randomUUID(), // Quyền sinh ID ở domain, bỏ GeneratedValue ở DB entity
                name, email, password);
    }

    // getter

    // setter
    public void changeEmail(Email email) {
        if (email == null)
            throw new InvalidArgumentException("email");
        if (this.email.equals(email))
            throw new SameEmailException();
        this.email = email;
    }

    public void changeName(String name) {
        validateNameAndPassword(name, this.password);
        this.name = name.trim();
    }

    public void changePassword(Password password) {
        if (password == null) throw new InvalidArgumentException("password");
        if (this.password.equals(password)) throw new SamePasswordException();
        validatePasswordContainsName(this.name, password);
        this.password = password;
    }
}