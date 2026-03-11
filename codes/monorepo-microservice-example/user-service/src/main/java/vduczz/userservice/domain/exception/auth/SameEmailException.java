package vduczz.userservice.domain.exception.auth;

import vduczz.userservice.domain.exception.BaseException;
import vduczz.userservice.domain.exception.code.ErrorCode;

public class SameEmailException extends BaseException {

    public SameEmailException() {
        super(
                ErrorCode.AUTH_SAME_EMAIL,
                "Same email!"
        );
    }
}
