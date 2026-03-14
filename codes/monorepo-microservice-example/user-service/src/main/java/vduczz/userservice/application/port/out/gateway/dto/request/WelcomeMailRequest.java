package vduczz.userservice.application.port.out.gateway.dto.request;

import lombok.Builder;

@Builder
public record WelcomeMailRequest(
        String email,
        String name
){}