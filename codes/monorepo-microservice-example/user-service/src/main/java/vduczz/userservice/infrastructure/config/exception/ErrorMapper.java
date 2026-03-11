package vduczz.userservice.infrastructure.config.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import vduczz.userservice.domain.exception.code.ErrorCode;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class ErrorMapper {
    private ErrorMapper() {
    }

    private static final Map<ErrorCode, HttpStatus> DOMAIN_ERROR_MAP = new EnumMap<>(ErrorCode.class);

    static {
        // Auth error
        DOMAIN_ERROR_MAP.put(ErrorCode.AUTH_PASSWORD_TOO_WEEK, HttpStatus.BAD_REQUEST);
        DOMAIN_ERROR_MAP.put(ErrorCode.AUTH_PASSWORD_CONTAIN_NAME, HttpStatus.BAD_REQUEST);
        DOMAIN_ERROR_MAP.put(ErrorCode.AUTH_SAME_EMAIL, HttpStatus.BAD_REQUEST);
        DOMAIN_ERROR_MAP.put(ErrorCode.AUTH_SAME_PASSWORD, HttpStatus.BAD_REQUEST);

        DOMAIN_ERROR_MAP.put(ErrorCode.COMMON_INVALID_ARGUMENT_EXCEPTION, HttpStatus.BAD_REQUEST);

        // Unknow error
        DOMAIN_ERROR_MAP.put(ErrorCode.UNKNOWN_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static HttpStatus toHttpStatus(ErrorCode errorCode) {
        return DOMAIN_ERROR_MAP.getOrDefault(errorCode, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
