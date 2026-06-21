package com.hospital.appointment.module.hospital.controller;

import com.hospital.appointment.common.result.R;
import com.hospital.appointment.module.hospital.service.AnnouncementService;
import com.hospital.appointment.module.hospital.service.DepartmentService;
import com.hospital.appointment.module.hospital.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/hospital")
@RequiredArgsConstructor
public class HospitalController {

    private final DepartmentService departmentService;
    private final DoctorService doctorService;
    private final AnnouncementService announcementService;

    @GetMapping("/departments")
    public R<?> departments() {
        return R.ok(departmentService.getEnabledList());
    }

    @GetMapping("/departments/{id}")
    public R<?> departmentDetail(@PathVariable Long id) {
        return R.ok(departmentService.getById(id));
    }

    @GetMapping("/doctors")
    public R<?> doctors(@RequestParam(required = false) Long departmentId,
                        @RequestParam(required = false) String name,
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "10") int size) {
        return R.ok(doctorService.getDoctorList(departmentId, name, page, size));
    }

    @GetMapping("/doctors/{id}")
    public R<?> doctorDetail(@PathVariable Long id) {
        return R.ok(doctorService.getDetail(id));
    }

    @GetMapping("/announcements")
    public R<?> announcements() {
        return R.ok(announcementService.getPublishedList());
    }

    @GetMapping("/announcements/{id}")
    public R<?> announcementDetail(@PathVariable Long id) {
        return R.ok(announcementService.getDetail(id));
    }
}
