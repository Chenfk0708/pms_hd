package com.jeez.zp.room.controller;

import com.jeez.zp.room.api.HudsonResponse;
import com.jeez.zp.room.api.TraceIdFactory;
import com.jeez.zp.room.dto.request.CleanTaskActionRequest;
import com.jeez.zp.room.dto.request.CleanTaskCreateRequest;
import com.jeez.zp.room.dto.request.CleanTaskNotifyRequest;
import com.jeez.zp.room.dto.request.CleanTaskPageRequest;
import com.jeez.zp.room.dto.request.CleanTaskStatisticsRequest;
import com.jeez.zp.room.security.LoginUserContext;
import com.jeez.zp.room.service.CleanTaskService;
import com.jeez.zp.room.vo.CleanTaskActionResponseVO;
import com.jeez.zp.room.vo.CleanTaskDashboardResponseVO;
import com.jeez.zp.room.vo.CleanTaskExportResponseVO;
import com.jeez.zp.room.vo.CleanTaskStatisticsResponseVO;
import com.jeez.zp.room.vo.CleanStatisticsDashboardResponseVO;
import com.jeez.zp.room.vo.CleanStatisticsDetailRowVO;
import com.jeez.zp.room.vo.CleanStatisticsExportResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CleanTaskController {

    private final CleanTaskService cleanTaskService;

    @PostMapping("/cleanTask/page/get")
    public HudsonResponse<CleanTaskDashboardResponseVO> getPage(@RequestBody CleanTaskPageRequest request) {
        return HudsonResponse.success(
                cleanTaskService.getPage(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        parseLong(request.getPoiId()),
                        request.getCleanTime(),
                        parseLong(request.getRoomId()),
                        request.getCleanType(),
                        request.getCleanStatus(),
                        request.getCleanerIds(),
                        request.getPageNum(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("clean-task-page-get")
        );
    }

    @PostMapping("/cleanTask/create")
    public HudsonResponse<CleanTaskActionResponseVO> create(@RequestBody CleanTaskCreateRequest request) {
        return HudsonResponse.success(
                cleanTaskService.create(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("clean-task-create")
        );
    }

    @PostMapping("/cleanTask/notify")
    public HudsonResponse<CleanTaskActionResponseVO> notify(@RequestBody CleanTaskNotifyRequest request) {
        return HudsonResponse.success(
                cleanTaskService.notify(parseLong(request.getCampId()), LoginUserContext.requiredUserId(), request.getTaskIds()),
                TraceIdFactory.next("clean-task-notify")
        );
    }

    @PostMapping("/cleanTask/assign")
    public HudsonResponse<CleanTaskActionResponseVO> assign(@RequestBody CleanTaskActionRequest request) {
        return HudsonResponse.success(
                cleanTaskService.assign(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getTaskId(),
                        request.getCleanerId(),
                        request.getRemark()
                ),
                TraceIdFactory.next("clean-task-assign")
        );
    }

    @PostMapping("/cleanTask/start")
    public HudsonResponse<CleanTaskActionResponseVO> start(@RequestBody CleanTaskActionRequest request) {
        return HudsonResponse.success(
                cleanTaskService.start(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getTaskId(),
                        request.getRemark()
                ),
                TraceIdFactory.next("clean-task-start")
        );
    }

    @PostMapping("/cleanTask/complete")
    public HudsonResponse<CleanTaskActionResponseVO> complete(@RequestBody CleanTaskActionRequest request) {
        return HudsonResponse.success(
                cleanTaskService.complete(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getTaskId(),
                        request.getRemark()
                ),
                TraceIdFactory.next("clean-task-complete")
        );
    }

    @PostMapping("/cleanTask/cancel")
    public HudsonResponse<CleanTaskActionResponseVO> cancel(@RequestBody CleanTaskActionRequest request) {
        return HudsonResponse.success(
                cleanTaskService.cancel(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getTaskId(),
                        request.getRemark()
                ),
                TraceIdFactory.next("clean-task-cancel")
        );
    }

    @PostMapping("/cleanTask/export")
    public HudsonResponse<CleanTaskExportResponseVO> export(@RequestBody CleanTaskPageRequest request) {
        return HudsonResponse.success(
                cleanTaskService.export(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        parseLong(request.getPoiId()),
                        request.getCleanTime(),
                        parseLong(request.getRoomId()),
                        request.getCleanType(),
                        request.getCleanStatus(),
                        request.getCleanerIds()
                ),
                TraceIdFactory.next("clean-task-export")
        );
    }

    @PostMapping("/cleanTask/statistics")
    public HudsonResponse<CleanTaskStatisticsResponseVO> getStatistics(@RequestBody CleanTaskStatisticsRequest request) {
        return HudsonResponse.success(
                cleanTaskService.getStatistics(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        parseLong(request.getStoreId()),
                        request.getRoomIds(),
                        request.getCleanerIds(),
                        request.getCleanStartTime(),
                        request.getCleanEndTime(),
                        request.getPageNum(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("clean-task-statistics")
        );
    }

    @PostMapping("/clean/statistics/dashboard")
    public HudsonResponse<CleanStatisticsDashboardResponseVO> getStatisticsDashboard(@RequestBody CleanTaskStatisticsRequest request) {
        return HudsonResponse.success(
                cleanTaskService.getStatisticsDashboard(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        parseLong(request.getStoreId()),
                        request.getRoomIds(),
                        request.getCleanerIds(),
                        request.getCleanStartTime(),
                        request.getCleanEndTime(),
                        request.getPageNum(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("clean-statistics-dashboard")
        );
    }

    @PostMapping("/clean/statistics/export")
    public HudsonResponse<CleanStatisticsExportResponseVO> exportStatistics(@RequestBody CleanTaskStatisticsRequest request) {
        return HudsonResponse.success(
                cleanTaskService.exportStatistics(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        parseLong(request.getStoreId()),
                        request.getRoomIds(),
                        request.getCleanerIds(),
                        request.getCleanStartTime(),
                        request.getCleanEndTime()
                ),
                TraceIdFactory.next("clean-statistics-export")
        );
    }

    @GetMapping(value = "/clean/statistics/export/download", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> downloadStatistics(
            @RequestParam(required = false) String campId,
            @RequestParam(required = false) String storeId,
            @RequestParam(required = false) List<String> roomIds,
            @RequestParam(required = false) List<String> cleanerIds,
            @RequestParam(required = false) Long cleanStartTime,
            @RequestParam(required = false) Long cleanEndTime
    ) {
        CleanStatisticsExportResponseVO export = cleanTaskService.exportStatistics(
                parseLong(campId),
                LoginUserContext.requiredUserId(),
                parseLong(storeId),
                roomIds,
                cleanerIds,
                cleanStartTime,
                cleanEndTime
        );
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + export.getFileName())
                .body(toCleanStatisticsCsv(export));
    }

    private String toCleanStatisticsCsv(CleanStatisticsExportResponseVO export) {
        StringBuilder csv = new StringBuilder("id,cleanDate,roomName,cleanerName,cleanType,fee,status\n");
        if (export.getRows() == null) {
            return csv.toString();
        }
        for (CleanStatisticsDetailRowVO row : export.getRows()) {
            csv.append(escapeCsv(row.getId())).append(',')
                    .append(escapeCsv(row.getCleanDate())).append(',')
                    .append(escapeCsv(row.getRoomName())).append(',')
                    .append(escapeCsv(row.getCleanerName())).append(',')
                    .append(escapeCsv(row.getCleanType())).append(',')
                    .append(row.getFee() == null ? "" : row.getFee()).append(',')
                    .append(escapeCsv(row.getStatus()))
                    .append('\n');
        }
        return csv.toString();
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
        if (value == null || value.isBlank() || "ALL".equals(value) || "all".equals(value)) {
            return null;
        }
        return Long.valueOf(value);
    }
}
