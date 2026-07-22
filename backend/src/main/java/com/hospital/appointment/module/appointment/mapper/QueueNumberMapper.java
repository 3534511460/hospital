package com.hospital.appointment.module.appointment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.module.appointment.model.QueueNumber;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface QueueNumberMapper extends BaseMapper<QueueNumber> {

    @Select("SELECT q.*, u.real_name AS patient_name, a.time_slot, du.real_name AS doctor_name " +
            "FROM queue_number q " +
            "LEFT JOIN appointment a ON q.appointment_id = a.id " +
            "LEFT JOIN sys_user u ON a.patient_id = u.id " +
            "LEFT JOIN sys_user du ON q.doctor_id = du.id " +
            "WHERE q.doctor_id = #{doctorId} AND a.appointment_date = CURDATE() AND q.status != 3 " +
            "ORDER BY q.queue_number ASC")
    List<QueueNumber> findTodayQueue(@Param("doctorId") Long doctorId);

    @Select("SELECT q.*, u.real_name AS patient_name, a.time_slot, du.real_name AS doctor_name " +
            "FROM queue_number q " +
            "LEFT JOIN appointment a ON q.appointment_id = a.id " +
            "LEFT JOIN sys_user u ON a.patient_id = u.id " +
            "LEFT JOIN sys_user du ON q.doctor_id = du.id " +
            "WHERE a.appointment_date = CURDATE() AND q.status != 3 " +
            "ORDER BY q.doctor_id, q.queue_number ASC")
    List<QueueNumber> findAllTodayQueue();

    @Select("SELECT q.*, u.real_name AS patient_name, a.time_slot " +
            "FROM queue_number q LEFT JOIN appointment a ON q.appointment_id = a.id " +
            "LEFT JOIN sys_user u ON a.patient_id = u.id WHERE q.id = #{id}")
    QueueNumber findByIdWithDetails(@Param("id") Long id);

    /**
     * 原子生成排队号：单条 INSERT...SELECT 内计算 max+1，避免并发重号。
     * 号码按该医生「就诊日期」维度计数，与 findTodayQueue 的口径一致。
     */
    @Insert("INSERT INTO queue_number (appointment_id, doctor_id, queue_number, status, create_time, update_time) " +
            "SELECT #{appointmentId}, #{doctorId}, " +
            "COALESCE((SELECT MAX(q.queue_number) FROM queue_number q " +
            "LEFT JOIN appointment a ON q.appointment_id = a.id " +
            "WHERE q.doctor_id = #{doctorId} AND a.appointment_date = #{workDate}), 0) + 1, " +
            "0, NOW(), NOW()")
    int insertWithNextNumber(@Param("appointmentId") Long appointmentId,
                             @Param("doctorId") Long doctorId,
                             @Param("workDate") LocalDate workDate);
}
