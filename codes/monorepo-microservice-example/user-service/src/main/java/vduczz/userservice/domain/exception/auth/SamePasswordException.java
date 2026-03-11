package vduczz.userservice.domain.exception.auth;

import vduczz.userservice.domain.exception.BaseException;
import vduczz.userservice.domain.exception.code.ErrorCode;

public class SamePasswordException extends BaseException {
    public SamePasswordException() {
        super(
                ErrorCode.AUTH_SAME_PASSWORD,
                "Same password!"
        );
    }
}
