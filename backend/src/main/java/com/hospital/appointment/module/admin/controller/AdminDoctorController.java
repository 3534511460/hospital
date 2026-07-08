package com.hospital.appointment.module.admin.controller;

import com.hospital.appointment.common.annotation.Log;
import com.hospital.appointment.common.annotation.RequireRole;
import com.hospital.appointment.common.exception.BusinessException;
import com.hospital.appointment.common.result.R;
import com.hospital.appointment.module.hospital.mapper.DoctorMapper;
import com.hospital.appointment.module.hospital.model.Doctor;
import com.hospital.appointment.module.user.mapper.SysUserMapper;
import com.hospital.appointment.module.user.model.SysUser;
import com.hospital.appointment.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/doctors")
@RequiredArgsConstructor
public class AdminDoctorController {

    private final DoctorMapper doctorMapper;
    private final SysUserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @GetMapping
    @RequireRole({"SYS_ADMIN", "DEPT_ADMIN"})
    public R<List<Doctor>> list(@RequestParam(required = false) Long departmentId,
                                 @RequestParam(required = false) Integer status) {
        if ("DEPT_ADMIN".equals(UserContext.getRole())) {
            departmentId = requireDeptId();
        }
        List<Doctor> list = doctorMapper.selectWithDetails(departmentId, null);
        if (status != null) list.removeIf(d -> !d.getStatus().equals(status));
        return R.ok(list);
    }

    @PostMapping
    @RequireRole({"SYS_ADMIN", "DEPT_ADMIN"})
    @Transactional
    @Log(module = "医生管理", operation = "新增医生")
    public R<Doctor> create(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        String realName = (String) body.get("realName");
        String phone = (String) body.get("phone");

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRealName(realName);
        user.setPhone(phone);
        user.setRole("DOCTOR");
        user.setStatus(1);
        userMapper.insert(user);

        Doctor doctor = new Doctor();
        doctor.setUserId(user.getId());
        Long deptId = body.get("departmentId") != null ? Long.valueOf(body.get("departmentId").toString()) : null;
        if ("DEPT_ADMIN".equals(UserContext.getRole())) {
            deptId = requireDeptId();
        }
        doctor.setDepartmentId(deptId);
        if (body.get("title") != null) doctor.setTitle((String) body.get("title"));
        if (body.get("specialty") != null) doctor.setSpecialty((String) body.get("specialty"));
        if (body.get("introduction") != null) doctor.setIntroduction((String) body.get("introduction"));
        doctor.setStatus(1);
        doctorMapper.insert(doctor);
        return R.ok(doctor);
    }

    private Doctor findByUserId(Long userId) {
        var doctor = doctorMapper.selectDetailById(userId);
        if (doctor == null) throw BusinessException.notFound("医生不存在");
        return doctor;
    }

    @PutMapping("/{userId}")
    @RequireRole({"SYS_ADMIN", "DEPT_ADMIN"})
    @Log(module = "医生管理", operation = "编辑医生")
    public R<Doctor> update(@PathVariable Long userId, @RequestBody Doctor updateDto) {
        Doctor doctor = requireInDept(userId);
        if (updateDto.getTitle() != null) doctor.setTitle(updateDto.getTitle());
        if (updateDto.getSpecialty() != null) doctor.setSpecialty(updateDto.getSpecialty());
        if (updateDto.getIntroduction() != null) doctor.setIntroduction(updateDto.getIntroduction());
        if (updateDto.getConsultationFee() != null) doctor.setConsultationFee(updateDto.getConsultationFee());
        if (updateDto.getDepartmentId() != null && !"DEPT_ADMIN".equals(UserContext.getRole()))
            doctor.setDepartmentId(updateDto.getDepartmentId());
        doctorMapper.updateById(doctor);
        return R.ok(doctor);
    }

    @PutMapping("/{userId}/status")
    @RequireRole({"SYS_ADMIN", "DEPT_ADMIN"})
    public R<Void> updateStatus(@PathVariable Long userId, @RequestBody Map<String, Integer> body) {
        Doctor doctor = requireInDept(userId);
        doctor.setStatus(body.get("status"));
        doctorMapper.updateById(doctor);
        return R.okMsg("状态更新成功");
    }

    @DeleteMapping("/{userId}")
    @RequireRole("SYS_ADMIN")
    @Log(module = "医生管理", operation = "删除医生")
    @Transactional
    public R<Void> delete(@PathVariable Long userId) {
        Doctor doctor = findByUserId(userId);
        doctorMapper.deleteById(doctor.getId());
        userMapper.deleteById(userId);
        return R.okMsg("删除成功");
    }

    private Long requireDeptId() {
        Long deptId = UserContext.getDepartmentId();
        if (deptId == null) throw BusinessException.forbidden("未绑定科室");
        return deptId;
    }

    private Doctor requireInDept(Long userId) {
        Doctor doctor = findByUserId(userId);
        if ("DEPT_ADMIN".equals(UserContext.getRole())) {
            Long deptId = requireDeptId();
            if (!deptId.equals(doctor.getDepartmentId()))
                throw BusinessException.forbidden("无权操作该医生");
        }
        return doctor;
    }
}
