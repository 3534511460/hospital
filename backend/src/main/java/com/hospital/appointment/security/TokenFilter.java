package com.hospital.appointment.security;

import com.hospital.appointment.module.user.mapper.SysUserMapper;
import com.hospital.appointment.module.user.model.SysUser;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TokenFilter implements Filter {

    private static final AntPathMatcher matcher = new AntPathMatcher();
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/send-code",
            "/api/v1/auth/reset-password",
            "/api/v1/hospital/announcements",
            "/api/v1/hospital/departments",
            "/api/v1/hospital/doctors/**",
            "/api/v1/hospital-info/public",
            "/api/v1/admin/hospital-info/public",
            "/api/v1/file/download/**",
            "/ws/**",
            "/static/**",
            "/error",
            "/favicon.ico"
    );

    private final JwtTokenProvider jwtTokenProvider;
    private final SysUserMapper sysUserMapper;

    public TokenFilter(JwtTokenProvider jwtTokenProvider, SysUserMapper sysUserMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = request.getRequestURI();

        if (isPublicPath(path)) {
            chain.doFilter(req, res);
            return;
        }

        String token = extractToken(request);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录或登录已过期\"}");
            return;
        }

        UserContext.setUserId(jwtTokenProvider.getUserId(token));
        UserContext.setUsername(jwtTokenProvider.getUsername(token));
        UserContext.setRole(jwtTokenProvider.getRole(token));
        UserContext.setToken(token);

        if ("DEPT_ADMIN".equals(UserContext.getRole())) {
            SysUser adminUser = sysUserMapper.selectById(UserContext.getUserId());
            if (adminUser != null && adminUser.getExt1() != null) {
                try { UserContext.setDepartmentId(Long.valueOf(adminUser.getExt1())); } catch (NumberFormatException ignored) {}
            }
        }

        try {
            chain.doFilter(req, res);
        } finally {
            UserContext.clear();
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(p -> matcher.match(p, path));
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return request.getHeader("token");
    }
}
