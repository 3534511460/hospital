package com.hospital.appointment.module.auth.service.impl;

import com.hospital.appointment.common.exception.BusinessException;
import com.hospital.appointment.module.auth.model.*;
import com.hospital.appointment.module.auth.service.AuthService;
import com.hospital.appointment.module.user.mapper.SysUserMapper;
import com.hospital.appointment.module.user.mapper.UserLoginLogMapper;
import com.hospital.appointment.module.user.model.SysUser;
import com.hospital.appointment.module.user.model.UserLoginLog;
import com.hospital.appointment.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final UserLoginLogMapper loginLogMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public LoginResponse login(LoginRequest request, String ip, String userAgent) {
        checkRateLimit("login:" + ip, 10, 60, "登录过于频繁，请稍后再试");

        SysUser user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            recordLogin(null, request.getUsername(), ip, userAgent, 0, "用户不存在");
            throw BusinessException.badRequest("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            recordLogin(user.getId(), user.getUsername(), ip, userAgent, 0, "账号已禁用");
            throw BusinessException.forbidden("账号已被禁用，请联系管理员");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            recordLogin(user.getId(), user.getUsername(), ip, userAgent, 0, "密码错误");
            throw BusinessException.badRequest("用户名或密码错误");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        String redisKey = "refresh:" + user.getId() + ":" + jwtTokenProvider.getJti(refreshToken);
        redisTemplate.opsForValue().set(redisKey, refreshToken, 7, TimeUnit.DAYS);

        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        userMapper.updateById(user);

        recordLogin(user.getId(), user.getUsername(), ip, userAgent, 1, null);

        return LoginResponse.builder()
                .accessToken(accessToken).refreshToken(refreshToken)
                .userId(user.getId()).username(user.getUsername())
                .realName(user.getRealName()).role(user.getRole()).avatar(user.getAvatar())
                .build();
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        checkRateLimit("register:" + request.getUsername(), 3, 3600, "注册过于频繁，请稍后再试");


        SysUser exist = userMapper.selectByUsername(request.getUsername());
        if (exist != null) throw BusinessException.badRequest("用户名已存在");
        if (request.getPhone() != null) {
            SysUser phoneUser = userMapper.selectByPhone(request.getPhone());
            if (phoneUser != null) throw BusinessException.badRequest("手机号已被注册");
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setRole("PATIENT");
        user.setStatus(1);
        userMapper.insert(user);
    }

    @Override
    public void sendCode(SendCodeRequest request) {
        checkRateLimit("sms:" + request.getPhone(), 1, 60, "验证码发送过于频繁，请60秒后再试");

        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        String redisKey = "sms:code:" + request.getBizType() + ":" + request.getPhone();
        redisTemplate.opsForValue().set(redisKey, code, 5, TimeUnit.MINUTES);
        log.info("验证码发送到 {}: {}", request.getPhone(), code);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        checkRateLimit("reset-pwd:" + request.getPhone(), 5, 300, "密码重置尝试过于频繁，请5分钟后再试");


        String redisKey = "sms:code:RESET_PWD:" + request.getPhone();
        String cachedCode = (String) redisTemplate.opsForValue().get(redisKey);
        if (cachedCode == null) throw BusinessException.badRequest("验证码已过期，请重新获取");
        if (!cachedCode.equals(request.getCode())) throw BusinessException.badRequest("验证码不正确");

        SysUser user = userMapper.selectByPhone(request.getPhone());
        if (user == null) throw BusinessException.notFound("该手机号未注册");
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
        redisTemplate.delete(redisKey);
    }

    @Override
    public LoginResponse refreshToken(String refreshTokenStr) {
        if (!jwtTokenProvider.validateToken(refreshTokenStr) || !jwtTokenProvider.isRefreshToken(refreshTokenStr))
            throw BusinessException.unauthorized("刷新令牌无效或已过期");

        Long userId = jwtTokenProvider.getUserId(refreshTokenStr);
        String jti = jwtTokenProvider.getJti(refreshTokenStr);
        String redisKey = "refresh:" + userId + ":" + jti;

        if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey)))
            throw BusinessException.unauthorized("刷新令牌已失效");

        redisTemplate.delete(redisKey);

        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == 0) throw BusinessException.forbidden("账号不可用");

        String newAccess = jwtTokenProvider.generateAccessToken(userId, user.getUsername(), user.getRole());
        String newRefresh = jwtTokenProvider.generateRefreshToken(userId, user.getUsername());
        String newKey = "refresh:" + userId + ":" + jwtTokenProvider.getJti(newRefresh);
        redisTemplate.opsForValue().set(newKey, newRefresh, 7, TimeUnit.DAYS);

        return LoginResponse.builder()
                .accessToken(newAccess).refreshToken(newRefresh)
                .userId(user.getId()).username(user.getUsername())
                .realName(user.getRealName()).role(user.getRole()).avatar(user.getAvatar())
                .build();
    }

    @Override
    public void logout(Long userId) {
        redisTemplate.delete(redisTemplate.keys("refresh:" + userId + ":*"));
    }

    @Override
    public String generateWsToken(Long userId) {
        String wsToken = java.util.UUID.randomUUID().toString().replace("-", "");
        String redisKey = "ws-token:" + wsToken;
        redisTemplate.opsForValue().set(redisKey, userId.toString(), 60, TimeUnit.SECONDS);
        return wsToken;
    }

    private void checkRateLimit(String key, int maxAttempts, int windowSeconds, String errorMsg) {
        String redisKey = "rate:" + key;
        Long count = redisTemplate.opsForValue().increment(redisKey);
        if (count != null && count == 1) {
            redisTemplate.expire(redisKey, windowSeconds, TimeUnit.SECONDS);
        }
        if (count != null && count > maxAttempts) {
            throw BusinessException.badRequest(errorMsg);
        }
    }

    private void recordLogin(Long userId, String username, String ip, String ua, int status, String failReason) {
        UserLoginLog l = new UserLoginLog();
        l.setUserId(userId); l.setUsername(username); l.setLoginIp(ip);
        l.setUserAgent(ua); l.setLoginStatus(status); l.setFailReason(failReason);
        loginLogMapper.insert(l);
    }
}
