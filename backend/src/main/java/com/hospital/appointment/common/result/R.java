package com.hospital.appointment.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.Instant;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class R<T> {
    private int code;
    private String msg;
    private T data;
    private Long timestamp;

    private R() {}

    private R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.timestamp = Instant.now().toEpochMilli();
    }

    public static <T> R<T> ok() {
        return new R<>(200, "success", null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(200, "success", data);
    }

    public static <T> R<T> ok(String msg, T data) {
        return new R<>(200, msg, data);
    }

    public static R<Void> okMsg(String msg) {
        return new R<>(200, msg, null);
    }

    public static <T> R<T> fail(int code, String msg) {
        return new R<>(code, msg, null);
    }

    public static <T> R<T> fail(String msg) {
        return new R<>(500, msg, null);
    }

    public static <T> R<T> unauthorized(String msg) {
        return new R<>(401, msg, null);
    }

    public static <T> R<T> forbidden(String msg) {
        return new R<>(403, msg, null);
    }

    public static <T> R<T> notFound(String msg) {
        return new R<>(404, msg, null);
    }

    public static <T> R<T> badRequest(String msg) {
        return new R<>(400, msg, null);
    }
}
