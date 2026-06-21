package com.hospital.appointment.module.medical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.module.medical.model.MedicalRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MedicalRecordMapper extends BaseMapper<MedicalRecord> {

    @Select("SELECT mr.*, p.real_name AS patient_name, d.real_name AS doctor_name, a.appointment_date " +
            "FROM medical_record mr " +
            "LEFT JOIN sys_user p ON mr.patient_id = p.id " +
            "LEFT JOIN sys_user d ON mr.doctor_id = d.id " +
            "LEFT JOIN appointment a ON mr.appointment_id = a.id " +
            "WHERE mr.appointment_id = #{appointmentId}")
    MedicalRecord findByAppointmentId(@Param("appointmentId") Long appointmentId);

    @Select("SELECT mr.*, p.real_name AS patient_name, d.real_name AS doctor_name, a.appointment_date " +
            "FROM medical_record mr " +
            "LEFT JOIN sys_user p ON mr.patient_id = p.id " +
            "LEFT JOIN sys_user d ON mr.doctor_id = d.id " +
            "LEFT JOIN appointment a ON mr.appointment_id = a.id " +
            "WHERE mr.patient_id = #{patientId} ORDER BY mr.create_time DESC")
    List<MedicalRecord> findByPatientId(@Param("patientId") Long patientId);
}
