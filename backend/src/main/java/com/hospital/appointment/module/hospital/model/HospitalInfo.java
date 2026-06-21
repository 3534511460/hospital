package com.hospital.appointment.module.hospital.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("hospital_info")
public class HospitalInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String hospitalName;
    private String shortName;
    private String logoUrl;
    private String address;
    private String phone;
    private String emergencyPhone;
    private String workHours;
    private String weekendHours;
    private String introduction;
    private String transportation;
    private String parkingInfo;
    private String website;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
