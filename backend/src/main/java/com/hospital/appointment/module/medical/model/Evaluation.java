package com.hospital.appointment.module.medical.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("evaluation")
public class Evaluation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long appointmentId;
    private Long patientId;
    private Long doctorId;
    private Integer rating;
    private String content;
    private String tags;
    private String replyContent;
    private LocalDateTime replyTime;
    private Integer isAnonymous;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String patientName;
}
