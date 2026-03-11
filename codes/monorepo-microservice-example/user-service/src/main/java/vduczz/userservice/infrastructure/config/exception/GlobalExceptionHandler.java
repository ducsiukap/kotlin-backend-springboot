package vduczz.userservice.infrastructure.config.exception;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vduczz.userservice.application.dto.response.ErrorResponse;
import vduczz.userservice.domain.exception.BaseException;
import vduczz.userservice.domain.exception.code.ErrorCode;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Domain Exception
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException e) {

        HttpStatus httpStatus = ErrorMapper.toHttpStatus(e.getErrorCode());
        return buildResponse(httpStatus, e.getErrorCode().name(), e.getMessage());
    }

    // @Valid exception
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("Invalid input data!");
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", msg);
    }

    // Catch mọi thứ còn lại (NPE, SQL Error, v.v.)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleRemainingException(Exception e) {
        // Ghi log lỗi để Dev vào sửa (CỰC KỲ QUAN TRỌNG)
        // log.error("Unexpected error occurred: ", e);

        // Trả về 500 cho client, không bao giờ để lộ stack trace
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.UNKNOWN_ERROR.name(),
                "Something went wrong, please try again later!"
        );
    }


    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String code, String msg) {
        ErrorResponse response = ErrorResponse.builder()
                .statusCode(status.value())
                .errorCode(code)
                .message(msg)
                .build();
        return ResponseEntity.status(status).body(response);
    }
}
