package com.hospital.appointment.module.auth.controller;

import com.hospital.appointment.common.annotation.Log;
import com.hospital.appointment.common.result.R;
import com.hospital.appointment.module.auth.model.*;
import com.hospital.appointment.module.auth.service.AuthService;
import com.hospital.appointment.security.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Log(module = "认证", operation = "用户登录")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest req) {
        String ip = req.getRemoteAddr();
        String ua = req.getHeader("User-Agent");
        return R.ok(authService.login(request, ip, ua));
    }

    @PostMapping("/register")
    @Log(module = "认证", operation = "用户注册")
    public R<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return R.ok();
    }

    @PostMapping("/send-code")
    public R<Void> sendCode(@Valid @RequestBody SendCodeRequest request) {
        authService.sendCode(request);
        return R.okMsg("验证码已发送");
    }

    @PostMapping("/reset-password")
    public R<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return R.okMsg("密码重置成功");
    }

    @PostMapping("/refresh")
    public R<LoginResponse> refresh(@RequestBody Map<String, String> body) {
        String token = body.get("refreshToken");
        return R.ok(authService.refreshToken(token));
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        authService.logout(UserContext.getUserId());
        return R.ok();
    }

    @GetMapping("/ws-token")
    public R<Map<String, String>> wsToken() {
        String wsToken = authService.generateWsToken(UserContext.getUserId());
        return R.ok(Map.of("wsToken", wsToken));
    }
}
