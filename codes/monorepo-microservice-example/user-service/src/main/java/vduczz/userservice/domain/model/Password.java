package vduczz.userservice.domain.model;

import vduczz.userservice.domain.exception.auth.PasswordTooWeekException;

// Value object
public record Password(String password) {
    public Password {
        if (password == null || password.length() < 6) {
            throw new PasswordTooWeekException();
        }
    }
}
