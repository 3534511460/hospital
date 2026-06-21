package com.hospital.appointment.module.medical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.module.medical.model.Evaluation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EvaluationMapper extends BaseMapper<Evaluation> {

    @Select("SELECT e.*, u.real_name AS patient_name FROM evaluation e " +
            "LEFT JOIN sys_user u ON e.patient_id = u.id " +
            "WHERE e.doctor_id = #{doctorId} ORDER BY e.create_time DESC")
    List<Evaluation> findByDoctorId(@Param("doctorId") Long doctorId);

    @Select("SELECT AVG(rating) FROM evaluation WHERE doctor_id = #{doctorId}")
    Double avgRatingByDoctorId(@Param("doctorId") Long doctorId);
}
