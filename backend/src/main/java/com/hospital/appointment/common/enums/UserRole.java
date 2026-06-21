package com.hospital.appointment.common.enums;

public enum UserRole {
    PATIENT("PATIENT", "患者"),
    DOCTOR("DOCTOR", "医生"),
    DEPT_ADMIN("DEPT_ADMIN", "科室管理员"),
    SYS_ADMIN("SYS_ADMIN", "系统管理员");

    private final String code;
    private final String desc;

    UserRole(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }
}
