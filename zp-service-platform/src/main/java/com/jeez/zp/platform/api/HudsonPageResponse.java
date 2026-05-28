package com.jeez.zp.platform.api;

import java.time.OffsetDateTime;

public class HudsonPageResponse<T> {

    private final Integer code;
    private final String message;
    private final T data;
    private final Long total;
    private final Long current;
    private final Long size;
    private final String traceId;
    private final String timestamp;

    private HudsonPageResponse(
            Integer code,
            String message,
            T data,
            Long total,
            Long current,
            Long size,
            String traceId,
            String timestamp
    ) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.total = total;
        this.current = current;
        this.size = size;
        this.traceId = traceId;
        this.timestamp = timestamp;
    }

    public static <T> HudsonPageResponse<T> success(T data, Long total, Long current, Long size, String traceId) {
        return new HudsonPageResponse<>(0, "success", data, total, current, size, traceId, OffsetDateTime.now().toString());
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

    public Long getTotal() {
        return total;
    }

    public Long getCurrent() {
        return current;
    }

    public Long getSize() {
        return size;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
