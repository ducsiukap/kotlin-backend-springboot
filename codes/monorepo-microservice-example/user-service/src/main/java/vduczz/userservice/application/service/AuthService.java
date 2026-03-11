package vduczz.userservice.application.service;

import vduczz.userservice.application.dto.request.AuthRequestDto;
import vduczz.userservice.application.dto.response.AuthResponseDto;

public interface AuthService {
    AuthResponseDto register(AuthRequestDto.RegisterRequest request, boolean mq, boolean kafka);
}
