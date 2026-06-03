package com.jeez.zp.finance.api;

import java.time.OffsetDateTime;

public class HudsonResponse<T> {

    private final Integer code;
    private final String message;
    private final T data;
    private final String traceId;
    private final String timestamp;

    private HudsonResponse(Integer code, String message, T data, String traceId, String timestamp) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.traceId = traceId;
        this.timestamp = timestamp;
    }

    public static <T> HudsonResponse<T> success(T data, String traceId) {
        return new HudsonResponse<>(0, "success", data, traceId, OffsetDateTime.now().toString());
    }

    public static <T> HudsonResponse<T> failure(Integer code, String message, String traceId) {
        return new HudsonResponse<>(code, message, null, traceId, OffsetDateTime.now().toString());
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Boolean getSuccess() {
        return code != null && code == 0;
    }

    public String getErrorMsg() {
        return getSuccess() ? null : message;
    }
}
