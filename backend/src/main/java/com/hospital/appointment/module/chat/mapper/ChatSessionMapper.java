package com.hospital.appointment.module.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.module.chat.model.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {

    @Select("SELECT cs.*, p.real_name AS patient_name, d.real_name AS doctor_name " +
            "FROM chat_session cs " +
            "LEFT JOIN sys_user p ON cs.patient_id = p.id " +
            "LEFT JOIN sys_user d ON cs.doctor_id = d.id " +
            "WHERE cs.patient_id = #{userId} AND cs.status != 0 ORDER BY cs.last_message_time DESC")
    List<ChatSession> findByPatient(@Param("userId") Long userId);

    @Select("SELECT cs.*, p.real_name AS patient_name, d.real_name AS doctor_name " +
            "FROM chat_session cs " +
            "LEFT JOIN sys_user p ON cs.patient_id = p.id " +
            "LEFT JOIN sys_user d ON cs.doctor_id = d.id " +
            "WHERE cs.doctor_id = #{userId} AND cs.status != 0 ORDER BY cs.last_message_time DESC")
    List<ChatSession> findByDoctor(@Param("userId") Long userId);
}
