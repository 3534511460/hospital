package com.hospital.appointment.module.appointment.controller;

import com.hospital.appointment.common.annotation.Log;
import com.hospital.appointment.common.annotation.RequireRole;
import com.hospital.appointment.common.result.R;
import com.hospital.appointment.module.appointment.model.DoctorSchedule;
import com.hospital.appointment.module.appointment.service.ScheduleService;
import com.hospital.appointment.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/available/{doctorId}")
    public R<List<DoctorSchedule>> available(@PathVariable Long doctorId,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().plusDays(7)}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
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
    @RequireRole({"DOCTOR", "DEPT_ADMIN"})
    @Log(module = "排班", operation = "新增排班")
    public R<DoctorSchedule> create(@RequestBody DoctorSchedule schedule) {
        return R.ok(scheduleService.addSchedule(schedule));
    }

    @PutMapping("/{id}")
    @RequireRole({"DOCTOR", "DEPT_ADMIN"})
    public R<DoctorSchedule> update(@PathVariable Long id, @RequestBody DoctorSchedule schedule) {
        schedule.setId(id);
        return R.ok(scheduleService.updateSchedule(schedule));
    }

    @DeleteMapping("/{id}")
    @RequireRole({"DOCTOR", "DEPT_ADMIN"})
    public R<Void> delete(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return R.ok();
    }

    @PostMapping("/batch")
    @RequireRole({"DOCTOR", "DEPT_ADMIN"})
    public R<Void> batchCreate(@RequestBody List<DoctorSchedule> schedules) {
        scheduleService.batchCreateSchedules(schedules);
        return R.okMsg("批量创建成功");
    }
}
