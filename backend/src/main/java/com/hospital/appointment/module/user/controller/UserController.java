package com.hospital.appointment.module.user.controller;

import com.hospital.appointment.common.annotation.RequireRole;
import com.hospital.appointment.common.result.R;
import com.hospital.appointment.module.user.model.PatientCompanion;
import com.hospital.appointment.module.user.model.SysUser;
import com.hospital.appointment.module.user.service.UserService;
import com.hospital.appointment.security.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public R<SysUser> profile() {
        return R.ok(userService.getUserProfile(UserContext.getUserId()));
    }

    @PutMapping("/profile")
    public R<Void> updateProfile(@RequestBody SysUser updateDto) {
        userService.updateProfile(UserContext.getUserId(), updateDto);
        return R.ok();
    }

    @PutMapping("/avatar")
    public R<Void> updateAvatar(@RequestBody Map<String, String> body) {
        userService.updateAvatar(UserContext.getUserId(), body.get("avatarUrl"));
        return R.ok();
    }

    @PutMapping("/password")
    public R<Void> changePassword(@RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            return R.badRequest("原密码和新密码不能为空");
        }
        if (newPassword.length() < 6) {
            return R.badRequest("新密码至少6位");
        }
        userService.changePassword(UserContext.getUserId(), oldPassword, newPassword);
        return R.okMsg("密码修改成功");
    }

    @GetMapping("/login-logs")
    public R<?> loginLogs(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size) {
        return R.ok(userService.getLoginLogs(UserContext.getUserId(), page, size));
    }

    @GetMapping("/companions")
    @RequireRole("PATIENT")
    public R<?> companions() {
        return R.ok(userService.getCompanions(UserContext.getUserId()));
    }

    @PostMapping("/companions")
    @RequireRole("PATIENT")
    public R<PatientCompanion> addCompanion(@Valid @RequestBody PatientCompanion companion) {
        return R.ok(userService.addCompanion(UserContext.getUserId(), companion));
    }

    @PutMapping("/companions/{id}")
    @RequireRole("PATIENT")
    public R<PatientCompanion> updateCompanion(@PathVariable Long id, @RequestBody PatientCompanion companion) {
        return R.ok(userService.updateCompanion(UserContext.getUserId(), id, companion));
    }

    @DeleteMapping("/companions/{id}")
    @RequireRole("PATIENT")
    public R<Void> deleteCompanion(@PathVariable Long id) {
        userService.deleteCompanion(UserContext.getUserId(), id);
        return R.ok();
    }
}
