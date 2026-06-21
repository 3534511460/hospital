package com.hospital.appointment.module.medical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.module.medical.model.Prescription;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PrescriptionMapper extends BaseMapper<Prescription> {

    @Select("SELECT * FROM prescription WHERE medical_record_id = #{recordId}")
    List<Prescription> findByMedicalRecordId(@Param("recordId") Long recordId);
}
