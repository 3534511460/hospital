package com.hospital.appointment.module.medical.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("prescription")
public class Prescription {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long medicalRecordId;
    private Long patientId;
    private Long doctorId;
    private String medicationName;
    private String specification;
    private String dosage;
    private String frequency;
    private String duration;
    private BigDecimal totalAmount;
    private String remark;
    private LocalDateTime createTime;
}
