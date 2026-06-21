package com.hospital.appointment.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.appointment.common.exception.BusinessException;
import com.hospital.appointment.module.user.mapper.PatientCompanionMapper;
import com.hospital.appointment.module.user.mapper.SysUserMapper;
import com.hospital.appointment.module.user.mapper.UserLoginLogMapper;
import com.hospital.appointment.module.user.model.PatientCompanion;
import com.hospital.appointment.module.user.model.SysUser;
import com.hospital.appointment.module.user.model.UserLoginLog;
import com.hospital.appointment.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final PatientCompanionMapper companionMapper;
    private final UserLoginLogMapper loginLogMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public SysUser getUserProfile(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw BusinessException.notFound("用户不存在");
        user.setPassword(null);
        return user;
    }

    @Override
    public void updateProfile(Long userId, SysUser updateDto) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw BusinessException.notFound("用户不存在");
        if (updateDto.getRealName() != null) user.setRealName(updateDto.getRealName());
        if (updateDto.getGender() != null) user.setGender(updateDto.getGender());
        if (updateDto.getAge() != null) user.setAge(updateDto.getAge());
        if (updateDto.getPhone() != null) user.setPhone(updateDto.getPhone());
        if (updateDto.getEmail() != null) user.setEmail(updateDto.getEmail());
        userMapper.updateById(user);
    }

    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw BusinessException.notFound("用户不存在");
        user.setAvatar(avatarUrl);
        userMapper.updateById(user);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw BusinessException.notFound("用户不存在");
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw BusinessException.badRequest("原密码不正确");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    @Override
    public Page<UserLoginLog> getLoginLogs(Long userId, int page, int size) {
        LambdaQueryWrapper<UserLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserLoginLog::getUserId, userId).orderByDesc(UserLoginLog::getCreateTime);
        return loginLogMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public List<PatientCompanion> getCompanions(Long userId) {
        return companionMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public PatientCompanion addCompanion(Long userId, PatientCompanion companion) {
        int count = companionMapper.countByUserId(userId);
        if (count >= 5) throw BusinessException.badRequest("最多只能添加5个就诊人");
        companion.setUserId(userId);
        companionMapper.insert(companion);
        return companion;
    }

    @Override
    public PatientCompanion updateCompanion(Long userId, Long companionId, PatientCompanion updateDto) {
        PatientCompanion companion = companionMapper.selectById(companionId);
        if (companion == null || !companion.getUserId().equals(userId)) {
            throw BusinessException.notFound("就诊人不存在");
        }
        if (updateDto.getRealName() != null) companion.setRealName(updateDto.getRealName());
        if (updateDto.getPhone() != null) companion.setPhone(updateDto.getPhone());
        if (updateDto.getGender() != null) companion.setGender(updateDto.getGender());
        if (updateDto.getAge() != null) companion.setAge(updateDto.getAge());
        if (updateDto.getRelationship() != null) companion.setRelationship(updateDto.getRelationship());
        companionMapper.updateById(companion);
        return companion;
    }

    @Override
    public void deleteCompanion(Long userId, Long companionId) {
        PatientCompanion companion = companionMapper.selectById(companionId);
        if (companion == null || !companion.getUserId().equals(userId)) {
            throw BusinessException.notFound("就诊人不存在");
        }
        companionMapper.deleteById(companionId);
    }
}
