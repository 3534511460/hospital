package com.hospital.appointment.module.ai.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ai_chat_history")
public class AiChatHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String sessionId;
    private String userMessage;
    private String aiResponse;
    private String chatType;
    private Integer tokensUsed;
    private String model;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
