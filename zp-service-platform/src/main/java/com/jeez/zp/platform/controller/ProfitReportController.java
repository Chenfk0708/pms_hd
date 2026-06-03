package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.ProfitReportRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.ProfitReportService;
import com.jeez.zp.platform.vo.ProfitReportExportResponseVO;
import com.jeez.zp.platform.vo.ProfitReportPageResponseVO;
import com.jeez.zp.platform.vo.ProfitReportRowVO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
public class ProfitReportController {

    private final ProfitReportService profitReportService;

    @PostMapping("/report/profit/get/v2")
    public HudsonResponse<ProfitReportPageResponseVO> getProfitReport(@RequestBody ProfitReportRequest request) {
        return HudsonResponse.success(
                profitReportService.getProfitReport(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getStartDate(),
                        request.getEndDate(),
                        parseLong(request.getPoiId()),
                        parseLong(request.getRoomCategoryId()),
                        parseLong(request.getRoomCategoryGroupId()),
                        parseLong(request.getChannelId()),
                        parseLong(request.getRoomId()),
                        request.getIsCleanCost(),
                        request.getPageNum(),
                        request.getCurrent(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("report-profit-get-v2")
        );
    }

    @PostMapping("/statistics/profit-report/export")
    public HudsonResponse<ProfitReportExportResponseVO> exportProfitReport(@RequestBody ProfitReportRequest request) {
        return HudsonResponse.success(
                profitReportService.exportProfitReport(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getStartDate(),
                        request.getEndDate(),
                        parseLong(request.getPoiId()),
                        parseLong(request.getRoomCategoryId()),
                        parseLong(request.getRoomCategoryGroupId()),
                        parseLong(request.getChannelId()),
                        parseLong(request.getRoomId()),
                        request.getIsCleanCost()
                ),
                TraceIdFactory.next("statistics-profit-report-export")
        );
    }

    @GetMapping(value = "/statistics/profit-report/export/download", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> downloadProfitReport(
            @RequestParam(required = false) String campId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String poiId,
            @RequestParam(required = false) String roomCategoryId,
            @RequestParam(required = false) String roomCategoryGroupId,
            @RequestParam(required = false) String channelId,
            @RequestParam(required = false) String roomId,
            @RequestParam(required = false) String isCleanCost
    ) {
        ProfitReportExportResponseVO export = profitReportService.exportProfitReport(
                parseLong(campId),
                LoginUserContext.requiredUserId(),
                startDate,
                endDate,
                parseLong(poiId),
                parseLong(roomCategoryId),
                parseLong(roomCategoryGroupId),
                parseLong(channelId),
                parseLong(roomId),
                parseInteger(isCleanCost)
        );
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + export.getFileName())
                .body(toProfitReportCsv(export));
    }

    private String toProfitReportCsv(ProfitReportExportResponseVO export) {
        StringBuilder csv = new StringBuilder("date,roomFeeMinusCommission,totalIncome,cleanCost,profitPrice,profitRate\n");
        if (export.getRows() == null) {
            return csv.toString();
        }
        for (ProfitReportRowVO row : export.getRows()) {
            csv.append(escapeCsv(row.getDate())).append(',')
                    .append(formatAmount(row.getRoomFeeMinusCommission())).append(',')
                    .append(formatAmount(row.getTotalIncome())).append(',')
                    .append(formatAmount(row.getCleanCost())).append(',')
                    .append(formatAmount(row.getProfitPrice())).append(',')
                    .append(escapeCsv(row.getProfitRate()))
                    .append('\n');
        }
        return csv.toString();
    }

    private String formatAmount(BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
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

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
