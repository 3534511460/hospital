package com.hospital.appointment.module.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.module.chat.model.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    @Select("SELECT * FROM chat_message WHERE session_id = #{sessionId} ORDER BY create_time ASC")
    List<ChatMessage> findBySessionId(@Param("sessionId") Long sessionId);

    @Update("UPDATE chat_message SET is_read = 1, read_time = NOW() WHERE session_id = #{sessionId} AND receiver_id = #{userId} AND is_read = 0")
    void markAsRead(@Param("sessionId") Long sessionId, @Param("userId") Long userId);
}
