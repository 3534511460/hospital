package com.hospital.appointment.module.admin.controller;

import com.hospital.appointment.common.annotation.Log;
import com.hospital.appointment.common.annotation.RequireRole;
import com.hospital.appointment.common.result.R;
import com.hospital.appointment.module.hospital.model.Announcement;
import com.hospital.appointment.module.hospital.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/announcements")
@RequiredArgsConstructor
@RequireRole({"SYS_ADMIN", "DEPT_ADMIN"})
public class AdminAnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public R<?> list(@RequestParam(defaultValue = "1") int page,
                     @RequestParam(defaultValue = "10") int size) {
        return R.ok(announcementService.getAllList(page, size));
    }

    @PostMapping
    @Log(module = "公告管理", operation = "发布公告")
    public R<Announcement> create(@RequestBody Announcement announcement) {
        return R.ok(announcementService.save(announcement));
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody Announcement announcement) {
        announcement.setId(id);
        announcementService.update(announcement);
        return R.okMsg("更新成功");
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return R.okMsg("删除成功");
    }
}
