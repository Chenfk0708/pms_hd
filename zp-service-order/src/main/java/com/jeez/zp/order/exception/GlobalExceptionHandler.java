package com.jeez.zp.order.exception;

import com.jeez.zp.order.api.HudsonResponse;
import com.jeez.zp.order.api.TraceIdFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.charset.StandardCharsets;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final MediaType APPLICATION_JSON_UTF8 = new MediaType(
            MediaType.APPLICATION_JSON,
            StandardCharsets.UTF_8
    );

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<HudsonResponse<Void>> handleBusinessException(BusinessException ex) {
        return ResponseEntity.status(resolveHttpStatus(ex.getCode()))
                .contentType(APPLICATION_JSON_UTF8)
                .body(HudsonResponse.failure(ex.getCode(), ex.getMessage(), TraceIdFactory.next("business-error")));
    }

    @ExceptionHandler(com.jeez.zp.finance.exception.BusinessException.class)
    public ResponseEntity<HudsonResponse<Void>> handleFinanceBusinessException(com.jeez.zp.finance.exception.BusinessException ex) {
        return ResponseEntity.status(resolveHttpStatus(ex.getCode()))
                .contentType(APPLICATION_JSON_UTF8)
                .body(HudsonResponse.failure(ex.getCode(), ex.getMessage(), TraceIdFactory.next("business-error")));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<HudsonResponse<Void>> handleUnexpectedException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(APPLICATION_JSON_UTF8)
                .body(HudsonResponse.failure(500, ex.getMessage(), TraceIdFactory.next("unexpected-error")));
    }

    private HttpStatus resolveHttpStatus(int code) {
        HttpStatus status = HttpStatus.resolve(code);
        return status != null ? status : HttpStatus.OK;
    }
}
