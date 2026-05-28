package com.jeez.zp.platform.api;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CleanLogLegacyResponse<T> {

    private final Boolean success;
    private final String errorMsg;
    private final String errorDetail;
    private final T data;

    private CleanLogLegacyResponse(Boolean success, String errorMsg, String errorDetail, T data) {
        this.success = success;
        this.errorMsg = errorMsg;
        this.errorDetail = errorDetail;
        this.data = data;
    }

    public static <T> CleanLogLegacyResponse<T> success(T data) {
        return new CleanLogLegacyResponse<>(true, null, null, data);
    }

    public static <T> CleanLogLegacyResponse<T> failure(String errorMsg) {
        return new CleanLogLegacyResponse<>(false, errorMsg, null, null);
    }

    public static <T> CleanLogLegacyResponse<T> failure(String errorMsg, String errorDetail) {
        return new CleanLogLegacyResponse<>(false, errorMsg, errorDetail, null);
    }

    public Boolean getSuccess() {
        return success;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public String getErrorDetail() {
        return errorDetail;
    }

    public T getData() {
        return data;
    }
}
