package vduczz.userservice.application.service;

import vduczz.userservice.application.port.in.dto.request.AuthRequestDto;
import vduczz.userservice.application.port.in.dto.response.AuthResponseDto;

public interface AuthService {
    AuthResponseDto register(AuthRequestDto.RegisterRequest request, boolean mq, boolean kafka);
}
