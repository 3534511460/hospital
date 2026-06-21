package com.hospital.appointment.module.chat.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private Long senderId;
    private Long receiverId;
    private String msgType;
    private String content;
    private String rawContent;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private Integer isRead;
    private LocalDateTime readTime;
    private LocalDateTime createTime;
}
