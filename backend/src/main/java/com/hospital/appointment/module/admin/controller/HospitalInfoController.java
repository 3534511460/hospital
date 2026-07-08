package com.hospital.appointment.module.admin.controller;

import com.hospital.appointment.common.annotation.RequireRole;
import com.hospital.appointment.common.exception.BusinessException;
import com.hospital.appointment.common.result.R;
import com.hospital.appointment.module.hospital.mapper.HospitalInfoMapper;
import com.hospital.appointment.module.hospital.model.HospitalInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/hospital-info")
@RequiredArgsConstructor
public class HospitalInfoController {

    private final HospitalInfoMapper hospitalInfoMapper;

    @GetMapping
    @RequireRole("SYS_ADMIN")
    public R<HospitalInfo> get() {
        var list = hospitalInfoMapper.selectList(null);
        return R.ok(list.isEmpty() ? null : list.get(0));
    }

    @GetMapping("/public")
    public R<HospitalInfo> getPublic() {
        var list = hospitalInfoMapper.selectList(null);
        return R.ok(list.isEmpty() ? null : list.get(0));
    }

    @PutMapping
    @RequireRole("SYS_ADMIN")
    public R<Void> update(@RequestBody HospitalInfo info) {
        var list = hospitalInfoMapper.selectList(null);
        if (list.isEmpty()) {
            hospitalInfoMapper.insert(info);
        } else {
            info.setId(list.get(0).getId());
            hospitalInfoMapper.updateById(info);
        }
        return R.okMsg("更新成功");
    }
}
