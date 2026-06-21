package com.hospital.appointment.common.enums;

public enum AppointmentStatus {
    PENDING_PAY(0, "待支付"),
    CONFIRMED(1, "已预约"),
    COMPLETED(2, "已就诊"),
    CANCELLED(3, "已取消"),
    NO_SHOW(4, "已爽约");

    private final int code;
    private final String desc;

    AppointmentStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }
}
