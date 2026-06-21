package com.hospital.appointment.module.hospital.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("department")
public class Department {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String code;
    private String description;
    private String icon;
    private String address;
    private String phone;
    private Integer sortOrder;
    private Integer status;
    private String ext1;
    private String ext2;
    private String ext3;
    private String ext4;
    private String ext5;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
