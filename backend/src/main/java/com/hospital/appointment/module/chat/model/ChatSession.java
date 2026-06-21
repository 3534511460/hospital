package com.hospital.appointment.module.chat.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("chat_session")
public class ChatSession {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long patientId;
    private Long doctorId;
    private Long appointmentId;
    private Integer status;
    private Integer unreadCountPatient;
    private Integer unreadCountDoctor;
    private String lastMessageContent;
    private LocalDateTime lastMessageTime;
    private String lastMessageType;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String patientName;
    @TableField(exist = false)
    private String doctorName;
}
