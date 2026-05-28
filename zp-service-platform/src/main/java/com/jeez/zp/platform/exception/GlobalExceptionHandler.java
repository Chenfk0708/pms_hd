package com.jeez.zp.platform.exception;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<HudsonResponse<Void>> handleBusinessException(BusinessException ex) {
        return ResponseEntity.status(resolveHttpStatus(ex.getCode()))
                .body(HudsonResponse.failure(ex.getCode(), ex.getMessage(), TraceIdFactory.next("business-error")));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<HudsonResponse<Void>> handleUnexpectedException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(HudsonResponse.failure(500, ex.getMessage(), TraceIdFactory.next("unexpected-error")));
    }

    private HttpStatus resolveHttpStatus(int code) {
        HttpStatus status = HttpStatus.resolve(code);
        return status != null ? status : HttpStatus.OK;
    }
}
