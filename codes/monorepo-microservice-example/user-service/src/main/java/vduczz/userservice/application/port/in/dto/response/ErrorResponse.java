package vduczz.userservice.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
public record ErrorResponse(int statusCode, String errorCode, String message) {
}
