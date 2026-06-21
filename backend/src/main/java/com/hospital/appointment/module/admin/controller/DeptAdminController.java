package com.hospital.appointment.module.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.appointment.common.annotation.RequireRole;
import com.hospital.appointment.common.exception.BusinessException;
import com.hospital.appointment.common.result.R;
import com.hospital.appointment.module.appointment.mapper.DoctorScheduleMapper;
import com.hospital.appointment.module.appointment.model.DoctorSchedule;
import com.hospital.appointment.module.hospital.mapper.DoctorMapper;
import com.hospital.appointment.module.hospital.model.Doctor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dept-admin")
@RequiredArgsConstructor
@RequireRole("DEPT_ADMIN")
public class DeptAdminController {

    private final DoctorMapper doctorMapper;
    private final DoctorScheduleMapper scheduleMapper;

    @GetMapping("/schedules/review")
    public R<List<DoctorSchedule>> reviewSchedules() {
        var doctors = doctorMapper.selectList(null);
        List<Long> doctorIds = doctors.stream().map(Doctor::getUserId).toList();
        if (doctorIds.isEmpty()) return R.ok(List.of());

        var w = new LambdaQueryWrapper<DoctorSchedule>()
                .in(DoctorSchedule::getDoctorId, doctorIds)
                .eq(DoctorSchedule::getStatus, 0)
                .orderByDesc(DoctorSchedule::getWorkDate);
        return R.ok(scheduleMapper.selectList(w));
    }

    @PutMapping("/schedules/{id}/approve")
    public R<Void> approveSchedule(@PathVariable Long id) {
        DoctorSchedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) throw BusinessException.notFound("排班不存在");
        schedule.setStatus(1);
        scheduleMapper.updateById(schedule);
        return R.okMsg("已审核通过");
    }

    @PutMapping("/schedules/{id}/reject")
    public R<Void> rejectSchedule(@PathVariable Long id, @RequestBody Map<String, String> body) {
        DoctorSchedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) throw BusinessException.notFound("排班不存在");
        schedule.setStatus(0);
        schedule.setRemark(body.getOrDefault("reason", "审核驳回"));
        scheduleMapper.updateById(schedule);
        return R.okMsg("已驳回");
    }
}
