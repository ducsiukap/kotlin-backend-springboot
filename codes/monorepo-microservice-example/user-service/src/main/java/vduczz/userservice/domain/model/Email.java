package vduczz.userservice.domain.model;

// record:
//      private final + all-args constructor + getter + equals() + hashCode() + toString()
// data class (kotlin)
public record Email(String email) {
    public Email {
        // compact constructor
        // + không khai báo tham số nhưng vẫn truy cập được
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Invalid email address");
        }
        // + tự thêm set field vào sau
        // this.value = valueX
    }
    // compact constructor dùng để
    //  + normalize dữ liệu bằng cách sửa trực tiếp biến được khai báo
    //      value = value.toUpperCase()
    //  + simple validation (simple business rule)
}
