package com.hospital.appointment.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.module.user.model.PatientCompanion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PatientCompanionMapper extends BaseMapper<PatientCompanion> {

    @Select("SELECT * FROM patient_companion WHERE user_id = #{userId} ORDER BY is_default DESC, create_time ASC")
    List<PatientCompanion> selectByUserId(Long userId);

    @Select("SELECT COUNT(*) FROM patient_companion WHERE user_id = #{userId}")
    int countByUserId(Long userId);
}
