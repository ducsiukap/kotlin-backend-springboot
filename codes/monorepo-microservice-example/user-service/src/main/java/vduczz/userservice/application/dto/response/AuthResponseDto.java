package vduczz.userservice.application.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record AuthResponseDto(
        String userId,
        UUID accountId
) {
}
