package com.hospital.appointment.module.hospital.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.appointment.module.hospital.model.Doctor;

public interface DoctorService {
    Page<Doctor> getDoctorList(Long departmentId, String name, int page, int size);
    Doctor getDetail(Long userId);
}
