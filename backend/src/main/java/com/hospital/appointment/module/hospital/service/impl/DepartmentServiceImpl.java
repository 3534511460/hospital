package com.hospital.appointment.module.hospital.service.impl;

import com.hospital.appointment.common.exception.BusinessException;
import com.hospital.appointment.module.hospital.mapper.DepartmentMapper;
import com.hospital.appointment.module.hospital.model.Department;
import com.hospital.appointment.module.hospital.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;

    @Override
    public List<Department> getEnabledList() {
        return departmentMapper.selectEnabled();
    }

    @Override
    public Department getById(Long id) {
        Department d = departmentMapper.selectById(id);
        if (d == null) throw BusinessException.notFound("科室不存在");
        return d;
    }

    @Override
    public List<Department> getAllList() {
        return departmentMapper.selectList(null);
    }

    @Override
    public void save(Department department) {
        departmentMapper.insert(department);
    }

    @Override
    public void update(Department department) {
        departmentMapper.updateById(department);
    }

    @Override
    public void delete(Long id) {
        departmentMapper.deleteById(id);
    }
}
