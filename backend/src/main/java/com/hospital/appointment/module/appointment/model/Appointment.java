package com.hospital.appointment.module.appointment.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("appointment")
public class Appointment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String appointmentNo;
    private Long patientId;
    private Long companionId;
    private Long doctorId;
    private Long departmentId;
    private Long scheduleId;
    private LocalDate appointmentDate;
    private String timeSlot;
    private Integer queueNumber;
    private String symptomDesc;
    private Integer status;
    private String cancelReason;
    private LocalDateTime cancelTime;
    private BigDecimal fee;
    private String ext1;
    private String ext2;
    private String ext3;
    private String ext4;
    private String ext5;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String patientName;
    @TableField(exist = false)
    private String doctorName;
    @TableField(exist = false)
    private String departmentName;
    @TableField(exist = false)
    private String timeSlotDisplay;
}
