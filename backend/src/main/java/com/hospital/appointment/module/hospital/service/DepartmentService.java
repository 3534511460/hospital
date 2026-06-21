package com.hospital.appointment.module.hospital.service;

import com.hospital.appointment.module.hospital.model.Department;
import java.util.List;

public interface DepartmentService {
    List<Department> getEnabledList();
    Department getById(Long id);
    List<Department> getAllList();
    void save(Department department);
    void update(Department department);
    void delete(Long id);
}
