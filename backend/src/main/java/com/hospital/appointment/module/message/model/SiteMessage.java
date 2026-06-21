package com.hospital.appointment.module.message.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("site_message")
public class SiteMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String title;
    private String content;
    private String msgType;
    private Integer isRead;
    private LocalDateTime readTime;
    private String ext1;
    private LocalDateTime createTime;
}
