package com.hospital.appointment.module.appointment.controller;

import com.hospital.appointment.common.annotation.Log;
import com.hospital.appointment.common.annotation.RequireRole;
import com.hospital.appointment.common.result.R;
import com.hospital.appointment.module.appointment.model.Appointment;
import com.hospital.appointment.module.appointment.service.AppointmentService;
import com.hospital.appointment.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/appointment")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/book")
    @RequireRole("PATIENT")
    @Log(module = "预约", operation = "预约挂号")
    public R<Appointment> book(@RequestBody Map<String, Object> body) {
        Long doctorId = toLong(body.get("doctorId"));
        Long scheduleId = toLong(body.get("scheduleId"));
        String symptomDesc = (String) body.get("symptomDesc");
        Long companionId = toLong(body.get("companionId"));
        return R.ok(appointmentService.bookAppointment(UserContext.getUserId(), doctorId, scheduleId, symptomDesc, companionId));
    }

    @PutMapping("/{id}/cancel")
    @RequireRole("PATIENT")
    @Log(module = "预约", operation = "取消预约")
    public R<Void> cancel(@PathVariable Long id) {
        appointmentService.cancelAppointment(id, UserContext.getUserId());
        return R.okMsg("取消成功");
    }

    @GetMapping("/my")
    public R<?> myAppointments(@RequestParam(required = false) Integer status) {
        return R.ok(appointmentService.getPatientAppointments(UserContext.getUserId(), status));
    }

    @GetMapping("/doctor")
    @RequireRole("DOCTOR")
    public R<?> doctorAppointments(@RequestParam(required = false) Integer status,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return R.ok(appointmentService.getDoctorAppointments(UserContext.getUserId(), status, date));
    }

    @PutMapping("/{id}/confirm")
    @RequireRole("DOCTOR")
    public R<Void> confirm(@PathVariable Long id) {
        appointmentService.confirmAppointment(id, UserContext.getUserId());
        return R.okMsg("已确认");
    }

    @PutMapping("/{id}/complete")
    @RequireRole("DOCTOR")
    public R<Void> complete(@PathVariable Long id) {
        appointmentService.completeAppointment(id, UserContext.getUserId());
        return R.okMsg("就诊完成");
    }

    @PutMapping("/{id}/noshow")
    @RequireRole("DOCTOR")
    public R<Void> noShow(@PathVariable Long id) {
        appointmentService.markNoShow(id);
        return R.okMsg("已标记爽约");
    }

    @GetMapping("/check-block")
    public R<Map<String, Boolean>> checkBlock() {
        boolean blocked = appointmentService.checkNoShowBlocked(UserContext.getUserId());
        return R.ok(Map.of("blocked", blocked));
    }

    @GetMapping("/{id}")
    public R<Appointment> detail(@PathVariable Long id) {
        return R.ok(appointmentService.getAppointmentDetail(id));
    }

    private Long toLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number n) return n.longValue();
        return Long.valueOf(obj.toString());
    }
}
