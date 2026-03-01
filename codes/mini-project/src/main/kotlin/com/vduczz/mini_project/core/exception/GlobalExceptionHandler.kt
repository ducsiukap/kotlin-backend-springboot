package com.vduczz.mini_project.core.exception

import com.vduczz.mini_project.dto.response.ErrorResponse
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice // @RestControllerAdvice
// kích hoạt cho phép nó tự động bắt các exception
class GlobalExceptionHandler {

    val defaultErrorMessage: String
        get() = "Unknown error"
    val defaultInternalErrorMessage: String
        get() = "Server too busy, try again later!"


    // ------------------------------------------------------------
    // Chiến lược bắt exception:
    // - bắt đích danh exception
    //      => chỉ định chi tiết exception trong @ExceptionHandler
    // - Gom chung N exceptions -> các exception xử lý giống nhau
    //      + Các exception có chung cha (super-exception)
    //          => bắt SuperException
    //      + Các exception không liên quan tới nhau
    //          => liệt kê toàn bộ
    //              ExceptionHandler(Exception1::class, Exception2::class, ...)
    //              fun exceptionHandler(ex: Exception) // params phải chứa được toàn bộ listed-exception
    //                              // Exception hoặc RuntimeException

    // ------------------------------------------------------------
    // handler specified exception
    // ex:  MethodArgumentNotValidException -> JSON parse failed (exception nếu @Valid thất bại)
    //      ConstraintViolationException -> url params (PathVariable, RequestParams, ..) validation failed
    @ExceptionHandler(MethodArgumentNotValidException::class)
    // @ExceptionHandler -> cho phép bắt exception nào?
    fun handleValidationExceptions(
        ex: MethodArgumentNotValidException
    ): ResponseEntity<ErrorResponse> {
        // errors
        val errors = mutableMapOf<String, String>()
        ex.bindingResult.fieldErrors.forEach { fieldError ->
            errors[fieldError.field] = fieldError.defaultMessage ?: defaultErrorMessage
        }

        // error -> ExceptionResponseDto
        val response = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(), // 400
            message = "Validation error, try again!",
            errors = errors
        )

        // return to client
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleValidationExceptions(
        ex: ConstraintViolationException
    ): ResponseEntity<ErrorResponse> {

        val errors = mutableMapOf<String, MutableList<String>>()
        ex.constraintViolations.forEach { violation ->
            val key = violation.propertyPath.toString().substringAfterLast(".")
            val message =
                "${violation.message ?: defaultErrorMessage} (rejected: ${violation.invalidValue})"

            errors.computeIfAbsent(key) { mutableListOf() }.add(message)
        }

        val response = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = "Invalid query parameters!",
            errors = errors
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    // ------------------------------------------------------------
    // default handler for business logic exception
    // -> RuntimeError
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = ex.message ?: defaultErrorMessage,
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    // Khi có custom exception
    // => bắt BaseAppException
    @ExceptionHandler(BaseAppException::class)
    fun handleBusinessException(ex: BaseAppException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            status = ex.httpStatus.value(),
            message = ex.message ?: defaultErrorMessage,
            errors = null
        )

        // return
        return ResponseEntity.status(ex.httpStatus).body(response)
    }


    // ------------------------------------------------------------
    // catch-all
    // -> default handler for all exception -> chống crash server
    @ExceptionHandler(Exception::class)
    fun defaultExceptionHandler(ex: Exception): ResponseEntity<ErrorResponse> {

        // log
        ex.printStackTrace()

        // response
        val response = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(), // 500
            message = defaultInternalErrorMessage,
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
}