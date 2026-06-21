package com.hospital.appointment.module.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.module.hospital.model.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {

    @Select("SELECT * FROM department WHERE status = 1 AND deleted = 0 ORDER BY sort_order ASC")
    List<Department> selectEnabled();
}
