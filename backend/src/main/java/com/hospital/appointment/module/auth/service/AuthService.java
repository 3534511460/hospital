package com.hospital.appointment.module.auth.service;

import com.hospital.appointment.module.auth.model.*;

public interface AuthService {
    LoginResponse login(LoginRequest request, String ip, String userAgent);
    void register(RegisterRequest request);
    void sendCode(SendCodeRequest request);
    void resetPassword(ResetPasswordRequest request);
    LoginResponse refreshToken(String refreshToken);
    void logout(Long userId);
}
