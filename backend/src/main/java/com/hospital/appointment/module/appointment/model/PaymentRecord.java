package com.hospital.appointment.module.appointment.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment_record")
public class PaymentRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long appointmentId;
    private String paymentNo;
    private Long userId;
    private BigDecimal amount;
    private String payMethod;
    private String tradeNo;
    private Integer status;
    private LocalDateTime paidAt;
    private LocalDateTime refundAt;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
