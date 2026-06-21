package com.hospital.appointment.module.appointment.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("no_show_record")
public class NoShowRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long appointmentId;
    private LocalDate noShowDate;
    private LocalDate blockedUntil;
    private String remark;
    private LocalDateTime createTime;
}
