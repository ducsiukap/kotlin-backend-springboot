package vduczz.userservice.domain.exception.auth;

import vduczz.userservice.domain.exception.BaseException;
import vduczz.userservice.domain.exception.code.ErrorCode;

public class PasswordTooWeekException extends BaseException {
    public PasswordTooWeekException() {
        super(
                ErrorCode.AUTH_PASSWORD_TOO_WEEK,
                "Password must have equal or more than 6 characters!"
        );
    }
}
