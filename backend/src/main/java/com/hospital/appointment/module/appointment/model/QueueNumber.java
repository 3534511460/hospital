package com.hospital.appointment.module.appointment.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("queue_number")
public class QueueNumber {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long appointmentId;
    private Long doctorId;
    private Integer queueNumber;
    private Integer status;
    private LocalDateTime callTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String patientName;
    @TableField(exist = false)
    private String timeSlot;
    @TableField(exist = false)
    private String doctorName;
}
