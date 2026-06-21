package com.hospital.appointment.module.appointment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.module.appointment.model.DoctorSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DoctorScheduleMapper extends BaseMapper<DoctorSchedule> {

    @Select("SELECT ds.*, u.real_name AS doctor_name, d.name AS department_name, dc.title " +
            "FROM doctor_schedule ds " +
            "LEFT JOIN doctor dc ON ds.doctor_id = dc.user_id " +
            "LEFT JOIN sys_user u ON ds.doctor_id = u.id " +
            "LEFT JOIN department d ON dc.department_id = d.id " +
            "WHERE ds.doctor_id = #{doctorId} AND ds.work_date BETWEEN #{startDate} AND #{endDate} " +
            "AND ds.status = 1 AND ds.booked_count < ds.max_count " +
            "ORDER BY ds.work_date, ds.start_time")
    List<DoctorSchedule> getAvailableSchedules(@Param("doctorId") Long doctorId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    @Select("SELECT ds.*, u.real_name AS doctor_name, d.name AS department_name, dc.title " +
            "FROM doctor_schedule ds " +
            "LEFT JOIN doctor dc ON ds.doctor_id = dc.user_id " +
            "LEFT JOIN sys_user u ON ds.doctor_id = u.id " +
            "LEFT JOIN department d ON dc.department_id = d.id " +
            "WHERE ds.doctor_id = #{doctorId} ORDER BY ds.work_date DESC, ds.start_time")
    List<DoctorSchedule> getDoctorScheduleWithDetails(@Param("doctorId") Long doctorId);

    @Select("SELECT ds.*, u.real_name AS doctor_name, d.name AS department_name, dc.title " +
            "FROM doctor_schedule ds " +
            "LEFT JOIN doctor dc ON ds.doctor_id = dc.user_id " +
            "LEFT JOIN sys_user u ON ds.doctor_id = u.id " +
            "LEFT JOIN department d ON dc.department_id = d.id " +
            "WHERE ds.doctor_id = #{doctorId} AND ds.work_date BETWEEN #{start} AND #{end} " +
            "ORDER BY ds.work_date, ds.start_time")
    List<DoctorSchedule> getWeekSchedules(@Param("doctorId") Long doctorId,
                                           @Param("start") LocalDate start,
                                           @Param("end") LocalDate end);

    @Select("SELECT DISTINCT ds.*, u.real_name AS doctor_name, d.name AS department_name, dc.title " +
            "FROM doctor_schedule ds " +
            "LEFT JOIN doctor dc ON ds.doctor_id = dc.user_id " +
            "LEFT JOIN sys_user u ON ds.doctor_id = u.id " +
            "LEFT JOIN department d ON dc.department_id = d.id " +
            "ORDER BY ds.work_date DESC, ds.start_time")
    List<DoctorSchedule> getAllWithDetails();
}
