package com.hospital.appointment.module.medical.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("medical_record")
public class MedicalRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long appointmentId;
    private Long patientId;
    private Long doctorId;
    private String diagnosis;
    private String chiefComplaint;
    private String presentIllness;
    private String pastHistory;
    private String physicalExamination;
    private String advice;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String patientName;
    @TableField(exist = false)
    private String doctorName;
    @TableField(exist = false)
    private String appointmentDate;
}
