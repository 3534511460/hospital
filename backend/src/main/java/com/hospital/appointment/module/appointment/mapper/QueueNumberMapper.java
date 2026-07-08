package com.hospital.appointment.module.appointment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.module.appointment.model.QueueNumber;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface QueueNumberMapper extends BaseMapper<QueueNumber> {

    @Select("SELECT q.*, u.real_name AS patient_name, a.time_slot, du.real_name AS doctor_name " +
            "FROM queue_number q " +
            "LEFT JOIN appointment a ON q.appointment_id = a.id " +
            "LEFT JOIN sys_user u ON a.patient_id = u.id " +
            "LEFT JOIN sys_user du ON q.doctor_id = du.id " +
            "WHERE q.doctor_id = #{doctorId} AND a.appointment_date = CURDATE() AND q.status != 3" +
            "ORDER BY q.queue_number ASC")
    List<QueueNumber> findTodayQueue(@Param("doctorId") Long doctorId);

    @Select("SELECT q.*, u.real_name AS patient_name, a.time_slot " +
            "FROM queue_number q LEFT JOIN appointment a ON q.appointment_id = a.id " +
            "LEFT JOIN sys_user u ON a.patient_id = u.id WHERE q.id = #{id}")
    QueueNumber findByIdWithDetails(@Param("id") Long id);

    @Select("SELECT COALESCE(MAX(queue_number), 0) FROM queue_number " +
            "WHERE doctor_id = #{doctorId} AND DATE(create_time) = CURDATE()")
    int maxQueueNumber(@Param("doctorId") Long doctorId);
}
