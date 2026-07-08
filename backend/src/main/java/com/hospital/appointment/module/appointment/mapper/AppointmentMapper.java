package com.hospital.appointment.module.appointment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.module.appointment.model.Appointment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {

    @Select("SELECT a.*, p.real_name AS patient_name, d.real_name AS doctor_name, dept.name AS department_name " +
            "FROM appointment a " +
            "LEFT JOIN sys_user p ON a.patient_id = p.id " +
            "LEFT JOIN sys_user d ON a.doctor_id = d.id " +
            "LEFT JOIN department dept ON a.department_id = dept.id " +
            "WHERE a.patient_id = #{patientId} ORDER BY a.create_time DESC")
    List<Appointment> findByPatientId(@Param("patientId") Long patientId);

    @Select("SELECT a.*, p.real_name AS patient_name, d.real_name AS doctor_name, dept.name AS department_name " +
            "FROM appointment a " +
            "LEFT JOIN sys_user p ON a.patient_id = p.id " +
            "LEFT JOIN sys_user d ON a.doctor_id = d.id " +
            "LEFT JOIN department dept ON a.department_id = dept.id " +
            "WHERE a.doctor_id = #{doctorId} ORDER BY a.appointment_date DESC, a.create_time DESC")
    List<Appointment> findByDoctorId(@Param("doctorId") Long doctorId);

    @Select("SELECT a.*, p.real_name AS patient_name, d.real_name AS doctor_name, dept.name AS department_name " +
            "FROM appointment a " +
            "LEFT JOIN sys_user p ON a.patient_id = p.id " +
            "LEFT JOIN sys_user d ON a.doctor_id = d.id " +
            "LEFT JOIN department dept ON a.department_id = dept.id " +
            "WHERE a.appointment_no = #{appointmentNo}")
    Appointment findByAppointmentNo(@Param("appointmentNo") String appointmentNo);

    @Update("UPDATE appointment SET status = 3, cancel_time = NOW() " +
            "WHERE id = #{id} AND status = 1 AND appointment_date > CURDATE()")
    int cancelAppointmentAtomic(@Param("id") Long id);
}
