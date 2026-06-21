package com.hospital.appointment.module.hospital.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.appointment.common.exception.BusinessException;
import com.hospital.appointment.module.hospital.mapper.DoctorMapper;
import com.hospital.appointment.module.hospital.model.Doctor;
import com.hospital.appointment.module.hospital.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorMapper doctorMapper;

    @Override
    public Page<Doctor> getDoctorList(Long departmentId, String name, int page, int size) {
        List<Doctor> all = doctorMapper.selectWithDetails(departmentId, name);
        int total = all.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        Page<Doctor> result = new Page<>(page, size, total);
        if (start < total) {
            result.setRecords(all.subList(start, end));
        }
        return result;
    }

    @Override
    public Doctor getDetail(Long userId) {
        Doctor d = doctorMapper.selectDetailById(userId);
        if (d == null) throw BusinessException.notFound("医生不存在");
        return d;
    }
}
