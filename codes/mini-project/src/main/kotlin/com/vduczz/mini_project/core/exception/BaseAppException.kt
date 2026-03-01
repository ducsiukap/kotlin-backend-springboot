package com.vduczz.mini_project.core.exception

import org.springframework.http.HttpStatus
import java.util.UUID

// Base Exception
// abstract class
abstract class BaseAppException(
    val httpStatus: HttpStatus, // status: required
    message: String, // message: required
) : RuntimeException(message)
// extends RuntimeException -> no-required to try-catch / throws


// ------------------------------------------------------------
// Custom Exception
// -> nên tạo class mới cho từng loại exception / group of exceptions
class UserNotFoundException(
    id: UUID
) : BaseAppException(
    httpStatus = HttpStatus.NOT_FOUND,
    message = "User not found: id={$id}",
)

class InvalidCredentialsException : BaseAppException(
    httpStatus = HttpStatus.UNAUTHORIZED,
    message = "Invalid authenticate credentials"
)

class DuplicateUsernameException(username: String) : BaseAppException(
    httpStatus = HttpStatus.CONFLICT,
    message = "Duplicate username: $username"
)