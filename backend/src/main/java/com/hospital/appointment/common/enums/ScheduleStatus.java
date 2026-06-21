package com.hospital.appointment.common.enums;

public enum ScheduleStatus {
    CLOSED(0, "停诊"),
    OPEN(1, "正常"),
    FULL(2, "满号");

    private final int code;
    private final String desc;

    ScheduleStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }
}
