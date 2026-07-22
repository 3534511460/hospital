package com.hospital.appointment.module.appointment.controller;

import com.hospital.appointment.common.annotation.RequireRole;
import com.hospital.appointment.common.exception.BusinessException;
import com.hospital.appointment.common.result.R;
import com.hospital.appointment.module.appointment.model.QueueNumber;
import com.hospital.appointment.module.appointment.service.QueueService;
import com.hospital.appointment.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    @GetMapping("/today")
    @RequireRole({"DOCTOR", "DEPT_ADMIN", "SYS_ADMIN"})
    public R<?> todayQueue() {
        String role = UserContext.getRole();
        if ("DEPT_ADMIN".equals(role)) {
            Long deptId = UserContext.getDepartmentId();
            if (deptId == null) return R.ok(java.util.List.of());
            return R.ok(queueService.getDepartmentQueue(deptId));
        }
        if ("DOCTOR".equals(role)) {
            return R.ok(queueService.getTodayQueue(UserContext.getUserId()));
        }
        // SYS_ADMIN: no personal queue; use /doctor/{id}/today or /queue/board
        return R.ok(java.util.List.of());
    }

    @GetMapping("/doctor/{doctorId}/today")
    public R<?> doctorQueue(@PathVariable Long doctorId) {
        return R.ok(queueService.getTodayQueue(doctorId));
    }

    @GetMapping("/board/today")
    public R<?> boardQueue() {
        return R.ok(queueService.getAllTodayQueue());
    }

    @GetMapping("/doctor/{doctorId}/current")
    public R<Map<String, Integer>> currentNumber(@PathVariable Long doctorId) {
        return R.ok(Map.of("currentNumber", queueService.getCurrentQueueNumber(doctorId)));
    }

    @PostMapping("/call-next")
    @RequireRole("DOCTOR")
    public R<QueueNumber> callNext() {
        return R.ok(queueService.callNext(UserContext.getUserId()));
    }

    @PutMapping("/{appointmentId}/complete")
    @RequireRole({"DOCTOR", "DEPT_ADMIN"})
    public R<Void> complete(@PathVariable Long appointmentId) {
        queueService.markCompleted(appointmentId);
        return R.okMsg("就诊完成");
    }

    @PutMapping("/{appointmentId}/miss")
    @RequireRole({"DOCTOR", "DEPT_ADMIN"})
    public R<Void> miss(@PathVariable Long appointmentId) {
        queueService.markMissed(appointmentId);
        return R.okMsg("已标记过号");
    }

    @PutMapping("/{appointmentId}/rewait")
    @RequireRole({"DOCTOR", "DEPT_ADMIN"})
    public R<Void> rewait(@PathVariable Long appointmentId) {
        queueService.markWaitAgain(appointmentId);
        return R.okMsg("已重新排队");
    }
}
