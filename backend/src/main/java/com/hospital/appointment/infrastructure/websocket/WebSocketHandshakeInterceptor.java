package com.hospital.appointment.infrastructure.websocket;

import com.hospital.appointment.module.user.mapper.SysUserMapper;
import com.hospital.appointment.module.user.model.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SysUserMapper sysUserMapper;

    public WebSocketHandshakeInterceptor(RedisTemplate<String, Object> redisTemplate, SysUserMapper sysUserMapper) {
        this.redisTemplate = redisTemplate;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String query = request.getURI().getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=", 2);
                if (kv.length == 2 && "token".equals(kv[0])) {
                    try {
                        String wsToken = kv[1];
                        String redisKey = "ws-token:" + wsToken;
                        String userIdStr = (String) redisTemplate.opsForValue().get(redisKey);
                        if (userIdStr != null) {
                            Long userId = Long.valueOf(userIdStr);
                            redisTemplate.delete(redisKey);
                            SysUser user = sysUserMapper.selectById(userId);
                            if (user != null && user.getStatus() == 1) {
                                attributes.put("userId", userId);
                                attributes.put("username", user.getUsername());
                                attributes.put("role", user.getRole());
                                return true;
                            }
                        }
                    } catch (Exception e) {
                        log.warn("WebSocket auth failed: {}", e.getMessage());
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {}
}
