package vduczz.userservice.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
public record ErrorResponse(int statusCode, String errorCode, String message) {
}
