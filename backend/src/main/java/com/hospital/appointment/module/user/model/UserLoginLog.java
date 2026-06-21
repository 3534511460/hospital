package com.hospital.appointment.module.user.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_login_log")
public class UserLoginLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String username;
    private String loginIp;
    private String userAgent;
    private Integer loginStatus;
    private String failReason;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
