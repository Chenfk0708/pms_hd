package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.StatementOrderRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.StatementOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatementOrderController {

    private final StatementOrderService statementOrderService;

    @PostMapping("/report/storer/statement/get")
    public HudsonResponse<Object> getStatement(@RequestBody StatementOrderRequest request) {
        return HudsonResponse.success(
                statementOrderService.getStatement(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        parseLongList(request.getPoiIds()),
                        request.getBookingStartDate(),
                        request.getBookingEndDate(),
                        request.getBreakTemp(),
                        request.getPageNum(),
                        request.getCurrent(),
                        request.getPageSize(),
                        request.getExportExcelMenuId()
                ),
                TraceIdFactory.next("report-storer-statement-get")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private List<Long> parseLongList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        List<Long> result = new ArrayList<>();
        for (String value : values) {
            Long parsed = parseLong(value);
            if (parsed != null) {
                result.add(parsed);
            }
        }
        return result;
    }
}
