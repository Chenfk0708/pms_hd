package com.jeez.common;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * API统一响应结果
 *
 * @param <T> 数据类型
 * @author Jeez
 */
@Schema(description = "API统一响应结果")
public class Result<T> {

    @Schema(description = "响应状态码", example = "200")
    @JsonSerialize(using = IntegerSerializer.class)
    private Integer code;

    @Schema(description = "响应消息", example = "操作成功")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    public Result() {
    }

    public Result(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功");
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    public static <T> Result<T> error() {
        return new Result<>(500, "操作失败");
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(500, message);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message);
    }

    public static <T> Result<T> error(Integer code, String message, T data) {
        return new Result<>(code, message, data);
    }

    public static <T> Result<T> paramError(String message) {
        return new Result<>(400, message);
    }

    // Getters and Setters
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }

    /**
     * 自定义Integer序列化器，保持Integer不转换为String
     * 这样可以在全局配置将Long转换为String的同时，保持code字段为数字类型
     */
    public static class IntegerSerializer extends StdSerializer<Integer> {
        public IntegerSerializer() {
            super(Integer.class);
        }

        @Override
        public void serialize(Integer value, com.fasterxml.jackson.core.JsonGenerator gen,
                              com.fasterxml.jackson.databind.SerializerProvider provider) throws java.io.IOException {
            if (value == null) {
                gen.writeNull();
            } else {
                gen.writeNumber(value);
            }
        }
    }
}