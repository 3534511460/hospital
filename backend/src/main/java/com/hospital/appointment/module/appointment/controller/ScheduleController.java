package com.hospital.appointment.module.appointment.controller;

import com.hospital.appointment.common.annotation.Log;
import com.hospital.appointment.common.annotation.RequireRole;
import com.hospital.appointment.common.exception.BusinessException;
import com.hospital.appointment.common.result.R;
import com.hospital.appointment.module.appointment.model.DoctorSchedule;
import com.hospital.appointment.module.appointment.service.ScheduleService;
import com.hospital.appointment.module.hospital.mapper.DoctorMapper;
import com.hospital.appointment.module.hospital.model.Doctor;
import com.hospital.appointment.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final DoctorMapper doctorMapper;

    @GetMapping("/available/{doctorId}")
    public R<List<DoctorSchedule>> available(@PathVariable Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now();
        if (endDate == null) endDate = LocalDate.now().plusDays(7);
        return R.ok(scheduleService.getAvailableSchedules(doctorId, startDate, endDate));
    }

    @GetMapping("/doctor")
    @RequireRole("DOCTOR")
    public R<List<DoctorSchedule>> mySchedules() {
        return R.ok(scheduleService.getDoctorScheduleDetail(UserContext.getUserId()));
    }

    @PostMapping
    @RequireRole({"DOCTOR", "DEPT_ADMIN", "SYS_ADMIN"})
    @Log(module = "排班", operation = "新增排班")
    public R<DoctorSchedule> create(@RequestBody DoctorSchedule schedule) {
        enforceScheduleWrite(schedule.getDoctorId());
        return R.ok(scheduleService.addSchedule(schedule));
    }

    @GetMapping("/admin/{doctorId}")
    @RequireRole({"SYS_ADMIN", "DEPT_ADMIN"})
    public R<List<DoctorSchedule>> adminGetDoctorSchedules(@PathVariable Long doctorId) {
        enforceScheduleRead(doctorId);
        return R.ok(scheduleService.getDoctorScheduleDetail(doctorId));
    }

    @GetMapping("/all")
    @RequireRole({"SYS_ADMIN", "DEPT_ADMIN"})
    public R<List<DoctorSchedule>> all() {
        if ("DEPT_ADMIN".equals(UserContext.getRole())) {
            Long deptId = requireDeptId();
            return R.ok(scheduleService.getSchedulesByDepartment(deptId));
        }
        return R.ok(scheduleService.getAllWithDetails());
    }

    @PutMapping("/{id}")
    @RequireRole({"DOCTOR", "DEPT_ADMIN", "SYS_ADMIN"})
    public R<DoctorSchedule> update(@PathVariable Long id, @RequestBody DoctorSchedule schedule) {
        DoctorSchedule exist = scheduleService.getScheduleById(id);
        enforceScheduleWrite(exist.getDoctorId());
        schedule.setId(id);
        // Keep original doctorId
        schedule.setDoctorId(exist.getDoctorId());
        return R.ok(scheduleService.updateSchedule(schedule));
    }

    @GetMapping("/week")
    @RequireRole({"SYS_ADMIN", "DEPT_ADMIN"})
    public R<List<DoctorSchedule>> weekSchedules(@RequestParam(required = false) Long doctorId,
            @RequestParam String start, @RequestParam String end) {
        if (doctorId != null) {
            enforceScheduleRead(doctorId);
            return R.ok(scheduleService.getWeekSchedules(doctorId, LocalDate.parse(start), LocalDate.parse(end)));
        }
        if ("DEPT_ADMIN".equals(UserContext.getRole())) {
            Long deptId = requireDeptId();
            return R.ok(scheduleService.getSchedulesByDepartment(deptId));
        }
        return R.ok(scheduleService.getAllWithDetails());
    }

    @PostMapping("/copy-last-week/{doctorId}")
    @RequireRole({"SYS_ADMIN", "DEPT_ADMIN", "DOCTOR"})
    @Log(module = "排班", operation = "复制上周排班")
    public R<Map<String, Integer>> copyLastWeek(@PathVariable Long doctorId,
            @RequestParam String targetMonday) {
        enforceScheduleWrite(doctorId);
        int count = scheduleService.copyLastWeekSchedules(doctorId, LocalDate.parse(targetMonday));
        return R.ok(Map.of("copied", count));
    }

    @DeleteMapping("/{id}")
    @RequireRole({"DOCTOR", "DEPT_ADMIN", "SYS_ADMIN"})
    public R<Void> delete(@PathVariable Long id) {
        DoctorSchedule exist = scheduleService.getScheduleById(id);
        enforceScheduleWrite(exist.getDoctorId());
        scheduleService.deleteSchedule(id);
        return R.ok();
    }

    @PostMapping("/batch")
    @RequireRole({"DOCTOR", "DEPT_ADMIN", "SYS_ADMIN"})
    public R<Void> batchCreate(@RequestBody List<DoctorSchedule> schedules) {
        for (var s : schedules) enforceScheduleWrite(s.getDoctorId());
        scheduleService.batchCreateSchedules(schedules);
        return R.okMsg("批量创建成功");
    }

    // --- helpers ---

    /**
     * For write operations (create/update/delete):
     * - DOCTOR: only their own schedules
     * - DEPT_ADMIN: only schedules of doctors in their department
     * - SYS_ADMIN: all
     */
    private void enforceScheduleWrite(Long scheduleDoctorId) {
        if (scheduleDoctorId == null)
            throw BusinessException.badRequest("缺少医生信息");
        String role = UserContext.getRole();
        if ("DOCTOR".equals(role)) {
            if (!UserContext.getUserId().equals(scheduleDoctorId))
                throw BusinessException.forbidden("只能操作自己的排班");
        } else if ("DEPT_ADMIN".equals(role)) {
            requireDoctorInDept(scheduleDoctorId);
        }
        // SYS_ADMIN: no restriction
    }

    private void enforceScheduleRead(Long doctorId) {
        if (doctorId == null) return;
        if ("DEPT_ADMIN".equals(UserContext.getRole()))
            requireDoctorInDept(doctorId);
    }

    private void requireDoctorInDept(Long doctorUserId) {
        Long deptId = requireDeptId();
        Doctor doctor = doctorMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Doctor>()
                        .eq(Doctor::getUserId, doctorUserId));
        if (doctor == null || !deptId.equals(doctor.getDepartmentId()))
            throw BusinessException.forbidden("无权操作该医生排班");
    }

    private Long requireDeptId() {
        Long deptId = UserContext.getDepartmentId();
        if (deptId == null) throw BusinessException.forbidden("未绑定科室");
        return deptId;
    }
}
