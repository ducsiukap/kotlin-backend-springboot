package vduczz.userservice.domain.exception.common;

import vduczz.userservice.domain.exception.BaseException;
import vduczz.userservice.domain.exception.code.ErrorCode;

public class InvalidArgumentException extends BaseException {

    public InvalidArgumentException(String... args) {
        super(
                ErrorCode.COMMON_INVALID_ARGUMENT_EXCEPTION,
                "Invalid arguments: %s".formatted(String.join(", ", args))
        );
    }

}
