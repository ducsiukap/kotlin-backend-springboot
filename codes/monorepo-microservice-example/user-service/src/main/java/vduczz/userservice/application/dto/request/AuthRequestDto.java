package vduczz.userservice.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public final class AuthRequestDto {

    @AllArgsConstructor
    @Getter
    @NoArgsConstructor
    public static class RegisterRequest {
        @NotBlank(message = "Name is required!")
        private String name;

        @NotBlank(message = "Password is required!")
        private String password;

        @NotBlank(message = "Email is required!")
        @Email(message = "Email-format required!")
        private String email;

        private String address;
    }
}
