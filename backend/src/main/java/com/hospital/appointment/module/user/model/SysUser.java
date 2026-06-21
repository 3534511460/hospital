package com.hospital.appointment.module.user.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    private Integer gender;
    private Integer age;
    private String avatar;
    private String role;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private String ext1;
    private String ext2;
    private String ext3;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
