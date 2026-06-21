package com.hospital.appointment.module.user.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("refresh_token")
public class RefreshToken {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String token;
    private String jti;
    private LocalDateTime expiresAt;
    private Integer revoked;
    private LocalDateTime createTime;
}
