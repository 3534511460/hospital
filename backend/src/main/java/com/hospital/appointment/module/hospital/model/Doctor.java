package com.hospital.appointment.module.hospital.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("doctor")
public class Doctor {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long departmentId;
    private String title;
    private String specialty;
    private String introduction;
    private BigDecimal consultationFee;
    private BigDecimal avgRating;
    private Integer ratingCount;
    private Integer status;
    private String ext1;
    private String ext2;
    private String ext3;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String realName;
    @TableField(exist = false)
    private String avatar;
    @TableField(exist = false)
    private String departmentName;
}
