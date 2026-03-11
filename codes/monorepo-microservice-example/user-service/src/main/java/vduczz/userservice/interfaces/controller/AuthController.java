package vduczz.userservice.interfaces.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vduczz.userservice.application.dto.request.AuthRequestDto;
import vduczz.userservice.application.dto.response.AuthResponseDto;
import vduczz.userservice.application.service.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(
            @RequestBody
            @Valid
            AuthRequestDto.RegisterRequest registerRequest,

            @RequestParam(name = "mq", defaultValue = "false") boolean mq, //  use Message Queue?
            @RequestParam(name = "kafka", defaultValue = "false") boolean kafka // Kafka event-streaming?
    ) {

        AuthResponseDto response = authService.register(registerRequest, mq, kafka);

        return ResponseEntity.ok(response);
    }
}
