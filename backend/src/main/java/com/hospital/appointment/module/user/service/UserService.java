package com.hospital.appointment.module.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.appointment.module.user.model.PatientCompanion;
import com.hospital.appointment.module.user.model.SysUser;
import com.hospital.appointment.module.user.model.UserLoginLog;

import java.util.List;

public interface UserService {
    SysUser getUserProfile(Long userId);
    void updateProfile(Long userId, SysUser updateDto);
    void updateAvatar(Long userId, String avatarUrl);
    void changePassword(Long userId, String oldPassword, String newPassword);
    Page<UserLoginLog> getLoginLogs(Long userId, int page, int size);
    List<PatientCompanion> getCompanions(Long userId);
    PatientCompanion addCompanion(Long userId, PatientCompanion companion);
    PatientCompanion updateCompanion(Long userId, Long companionId, PatientCompanion companion);
    void deleteCompanion(Long userId, Long companionId);
}
