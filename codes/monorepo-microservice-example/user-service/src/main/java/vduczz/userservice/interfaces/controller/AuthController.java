package vduczz.userservice.interfaces.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vduczz.userservice.application.port.in.dto.request.AuthRequestDto;
import vduczz.userservice.application.port.in.dto.response.AuthResponseDto;
import vduczz.userservice.application.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthService authService2;

    public AuthController(
            @Qualifier("authServiceImpl")
            AuthService authService,

            @Qualifier("authServiceImpl2")
            AuthService authService2
    ) {
        this.authService = authService;
        this.authService2 = authService2;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(
            @RequestBody
            @Valid
            AuthRequestDto.RegisterRequest registerRequest,

            @RequestParam(name = "mq", defaultValue = "false") boolean mq, //  use Message Queue?
            @RequestParam(name = "kafka", defaultValue = "false") boolean kafka, // Kafka event-streaming?

            @RequestParam(name = "version", defaultValue = "1") int version
    ) {

        AuthResponseDto response;
        if (version == 1)
            response = authService.register(registerRequest, mq, kafka);
        else {
            System.out.println(2);
            response = authService2.register(registerRequest, mq, kafka);
        }

        return ResponseEntity.ok(response);
    }
}
