package vduczz.userservice.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
public record WelcomeMailRequest(
        String email,
        String name
){}