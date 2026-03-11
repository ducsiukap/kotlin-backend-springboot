package vduczz.userservice.domain.exception.auth;

import vduczz.userservice.domain.exception.BaseException;
import vduczz.userservice.domain.exception.code.ErrorCode;

public class PasswordContainingNameException extends BaseException {
    public PasswordContainingNameException() {
        super(
                ErrorCode.AUTH_PASSWORD_CONTAIN_NAME,
                "Password can't contain name!"
        );
    }
}
