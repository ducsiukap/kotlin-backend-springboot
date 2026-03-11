package vduczz.userservice.domain.exception;

import lombok.Getter;
import vduczz.userservice.domain.exception.code.ErrorCode;

// Nguyên tắc
// + với Technical Errors / Programming Bugs: thiếu validate từ controller, ....
//      => dùng exception có sẵn
// + với Business Rule Violations: Dữ liệu đầu vào hoàn toàn hợp lệ về mặt kỹ thuật
//      (không null, đúng kiểu dữ liệu), nhưng nó lại vi phạm
//      các quy tắc kinh doanh, quy tắc vận hành của hệ thống
//      => custom exception

// Đa số custom excetion là DomainException, được sử dụng ở Domain
// Tuy nhiên, vẫn có Application Exceptions
//      Xảy ra trong quá trình điều phối luồng chạy (Use Case),
//      thường liên quan đến sự tương tác với bên ngoài (Database, API khác)

@Getter
public abstract class BaseException extends RuntimeException {
    // Tự định nghĩa Domain Error Code  => không phụ thuộc bên ngoài
    // Và mapping sang framework error code ở sau
    private final ErrorCode errorCode;

    protected BaseException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected BaseException(String message) {
        super(message);
        this.errorCode = ErrorCode.UNKNOWN_ERROR;
    }
}
