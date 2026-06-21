package com.hospital.appointment.module.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.module.ai.model.AiChatHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AiChatHistoryMapper extends BaseMapper<AiChatHistory> {

    @Select("SELECT * FROM ai_chat_history WHERE session_id = #{sessionId} ORDER BY create_time ASC LIMIT 20")
    List<AiChatHistory> findBySessionId(@Param("sessionId") String sessionId);

    @Select("SELECT * FROM ai_chat_history WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<AiChatHistory> findByUserId(@Param("userId") Long userId);
}
