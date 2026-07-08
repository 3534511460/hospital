package com.hospital.appointment.common.aspect;

import com.hospital.appointment.common.annotation.RequireRole;
import com.hospital.appointment.common.exception.BusinessException;
import com.hospital.appointment.security.UserContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RequireRoleAspect {

    @Around("@annotation(requireRole)")
    public Object checkMethodRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        checkPermission(requireRole);
        return joinPoint.proceed();
    }

    @Around("@within(requireRole)")
    public Object checkClassRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        checkPermission(requireRole);
        return joinPoint.proceed();
    }

    private void checkPermission(RequireRole requireRole) {
        String currentRole = UserContext.getRole();
        if (currentRole == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        String[] required = requireRole.value();
        if (required.length == 0) return;

        for (String role : required) {
            if (role.equals(currentRole)) return;
        }
        throw BusinessException.forbidden("权限不足，需要角色: " + String.join(",", required));
    }
}
