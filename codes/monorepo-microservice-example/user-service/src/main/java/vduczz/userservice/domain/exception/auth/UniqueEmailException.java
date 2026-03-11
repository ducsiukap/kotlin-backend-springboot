package vduczz.userservice.domain.exception.auth;

import vduczz.userservice.domain.exception.BaseException;
import vduczz.userservice.domain.exception.code.ErrorCode;

public class UniqueEmailException extends BaseException {

    public UniqueEmailException(String email) {
        super(
                ErrorCode.AUTH_EMAIL_ALREADY_EXISTS,
                "Email %s already exists".formatted(email)
        );
    }
}
