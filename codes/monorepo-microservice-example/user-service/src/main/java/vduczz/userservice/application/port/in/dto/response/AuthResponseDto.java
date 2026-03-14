package vduczz.userservice.application.port.in.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record AuthResponseDto(
        UUID userId,
        UUID accountId
) {
}
