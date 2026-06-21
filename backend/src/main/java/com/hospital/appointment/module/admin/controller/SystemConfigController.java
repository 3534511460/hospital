package com.hospital.appointment.module.admin.controller;

import com.hospital.appointment.common.annotation.RequireRole;
import com.hospital.appointment.common.exception.BusinessException;
import com.hospital.appointment.common.result.R;
import com.hospital.appointment.module.admin.mapper.SystemConfigMapper;
import com.hospital.appointment.module.admin.model.SystemConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/configs")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigMapper configMapper;

    @GetMapping
    @RequireRole("SYS_ADMIN")
    public R<List<SystemConfig>> list() {
        return R.ok(configMapper.selectList(null));
    }

    @PutMapping("/{id}")
    @RequireRole("SYS_ADMIN")
    public R<Void> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        SystemConfig config = configMapper.selectById(id);
        if (config == null) throw BusinessException.notFound("配置不存在");
        config.setConfigValue(body.get("configValue"));
        configMapper.updateById(config);
        return R.okMsg("更新成功");
    }

    @GetMapping("/public/{key}")
    public R<String> getPublicConfig(@PathVariable String key) {
        return R.ok(configMapper.getValueByKey(key));
    }
}
