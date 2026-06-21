package com.hospital.appointment.module.admin.controller;

import com.hospital.appointment.common.annotation.RequireRole;
import com.hospital.appointment.common.result.R;
import com.hospital.appointment.module.admin.mapper.StatisticsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsMapper statisticsMapper;

    @GetMapping("/dashboard")
    @RequireRole({"SYS_ADMIN", "DEPT_ADMIN"})
    public R<Map<String, Object>> dashboard() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String weekAgo = LocalDate.now().minusDays(7).format(DateTimeFormatter.ISO_DATE);
        String monthAgo = LocalDate.now().minusDays(30).format(DateTimeFormatter.ISO_DATE);

        Map<String, Object> data = new HashMap<>();
        data.put("todayAppointments", statisticsMapper.countTodayAppointments());
        data.put("totalPatients", statisticsMapper.countUsersByRole("PATIENT"));
        data.put("totalDoctors", statisticsMapper.countUsersByRole("DOCTOR"));
        data.put("monthAppointments", statisticsMapper.countAppointmentsBetween(monthAgo, today));
        data.put("departmentStats", statisticsMapper.getDepartmentStats());
        data.put("topDoctors", statisticsMapper.getTopDoctors(monthAgo, today, 5));
        data.put("trend", statisticsMapper.getAppointmentTrend(weekAgo, today));
        return R.ok(data);
    }

    @GetMapping("/appointments")
    @RequireRole({"SYS_ADMIN", "DEPT_ADMIN"})
    public R<?> appointmentStats(@RequestParam(defaultValue = "week") String period) {
        String end = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String start = switch (period) {
            case "month" -> LocalDate.now().minusDays(30).format(DateTimeFormatter.ISO_DATE);
            case "year" -> LocalDate.now().minusDays(365).format(DateTimeFormatter.ISO_DATE);
            default -> LocalDate.now().minusDays(7).format(DateTimeFormatter.ISO_DATE);
        };
        return R.ok(statisticsMapper.getAppointmentTrend(start, end));
    }

    @GetMapping("/doctors")
    @RequireRole({"SYS_ADMIN", "DEPT_ADMIN"})
    public R<?> topDoctors() {
        String start = LocalDate.now().minusDays(30).format(DateTimeFormatter.ISO_DATE);
        String end = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        return R.ok(statisticsMapper.getTopDoctors(start, end, 10));
    }

    @GetMapping("/departments")
    @RequireRole({"SYS_ADMIN", "DEPT_ADMIN"})
    public R<?> departmentStats() {
        return R.ok(statisticsMapper.getDepartmentStats());
    }

    @GetMapping("/revenue")
    @RequireRole("SYS_ADMIN")
    public R<Map<String, Object>> revenue(@RequestParam(defaultValue = "") String month) {
        if (month.isEmpty()) month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        Map<String, Object> result = new HashMap<>();
        result.put("month", month);
        result.put("total", statisticsMapper.getRevenueByMonth(month));
        return R.ok(result);
    }
}
