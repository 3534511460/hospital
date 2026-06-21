package com.hospital.appointment.module.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.appointment.common.annotation.Log;
import com.hospital.appointment.common.annotation.RequireRole;
import com.hospital.appointment.common.exception.BusinessException;
import com.hospital.appointment.common.result.R;
import com.hospital.appointment.module.user.mapper.SysUserMapper;
import com.hospital.appointment.module.user.model.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@RequireRole("SYS_ADMIN")
public class AdminUserController {

    private final SysUserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @GetMapping
    public R<Page<SysUser>> list(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) String role,
                                  @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SysUser> w = new LambdaQueryWrapper<>();
        if (role != null) w.eq(SysUser::getRole, role);
        if (keyword != null) w.and(q -> q.like(SysUser::getUsername, keyword).or().like(SysUser::getRealName, keyword));
        w.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> result = userMapper.selectPage(new Page<>(page, size), w);
        result.getRecords().forEach(u -> u.setPassword(null));
        return R.ok(result);
    }

    @GetMapping("/{id}")
    public R<SysUser> detail(@PathVariable Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw BusinessException.notFound("用户不存在");
        user.setPassword(null);
        return R.ok(user);
    }

    @PutMapping("/{id}/status")
    @Log(module = "用户管理", operation = "修改状态")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw BusinessException.notFound("用户不存在");
        user.setStatus(body.get("status"));
        userMapper.updateById(user);
        return R.okMsg("状态更新成功");
    }

    @PutMapping("/{id}/role")
    @Log(module = "用户管理", operation = "修改角色")
    public R<Void> updateRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw BusinessException.notFound("用户不存在");
        user.setRole(body.get("role"));
        userMapper.updateById(user);
        return R.okMsg("角色更新成功");
    }

    @PutMapping("/{id}/reset-pwd")
    @Log(module = "用户管理", operation = "重置密码")
    public R<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw BusinessException.notFound("用户不存在");
        user.setPassword(passwordEncoder.encode(body.get("password")));
        userMapper.updateById(user);
        return R.okMsg("密码已重置");
    }

    @DeleteMapping("/{id}")
    @Log(module = "用户管理", operation = "删除用户")
    public R<Void> delete(@PathVariable Long id) {
        userMapper.deleteById(id);
        return R.okMsg("删除成功");
    }
}
