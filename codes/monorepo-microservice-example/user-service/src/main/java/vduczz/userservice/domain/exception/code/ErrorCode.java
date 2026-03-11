package vduczz.userservice.domain.exception.code;

public enum ErrorCode {
    AUTH_PASSWORD_TOO_WEEK,
    AUTH_PASSWORD_CONTAIN_NAME,
    AUTH_SAME_EMAIL,
    AUTH_SAME_PASSWORD,
    AUTH_EMAIL_ALREADY_EXISTS,

    COMMON_INVALID_ARGUMENT_EXCEPTION,

    // Default Error Code
    UNKNOWN_ERROR
}
