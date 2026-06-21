package com.hospital.appointment.module.appointment.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("doctor_schedule")
public class DoctorSchedule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long doctorId;
    private LocalDate workDate;
    private String timeSlot;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxCount;
    private Integer bookedCount;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String doctorName;
    @TableField(exist = false)
    private String departmentName;
    @TableField(exist = false)
    private String title;
}
