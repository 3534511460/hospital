package com.hospital.appointment.module.admin.controller;

import com.hospital.appointment.common.annotation.Log;
import com.hospital.appointment.common.annotation.RequireRole;
import com.hospital.appointment.common.result.R;
import com.hospital.appointment.module.hospital.model.Department;
import com.hospital.appointment.module.hospital.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/departments")
@RequiredArgsConstructor
@RequireRole("SYS_ADMIN")
public class AdminDepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public R<?> list() {
        return R.ok(departmentService.getAllList());
    }

    @PostMapping
    @Log(module = "科室管理", operation = "新增科室")
    public R<Void> create(@RequestBody Department department) {
        departmentService.save(department);
        return R.okMsg("创建成功");
    }

    @PutMapping("/{id}")
    @Log(module = "科室管理", operation = "编辑科室")
    public R<Void> update(@PathVariable Long id, @RequestBody Department department) {
        department.setId(id);
        departmentService.update(department);
        return R.okMsg("更新成功");
    }

    @DeleteMapping("/{id}")
    @Log(module = "科室管理", operation = "删除科室")
    public R<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return R.okMsg("删除成功");
    }
}
