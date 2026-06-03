package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.CampIdRequest;
import com.jeez.zp.platform.dto.request.WorkspaceAccommodationAnalysisRequest;
import com.jeez.zp.platform.dto.request.WorkspaceMemoCreateRequest;
import com.jeez.zp.platform.dto.request.WorkspaceMemoHandleRequest;
import com.jeez.zp.platform.dto.request.WorkspaceMemoPageRequest;
import com.jeez.zp.platform.dto.request.WorkspaceOrdersRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.WorkspaceDashboardService;
import com.jeez.zp.platform.vo.WorkspaceAccommodationAnalysisVO;
import com.jeez.zp.platform.vo.WorkspaceBacklogItemVO;
import com.jeez.zp.platform.vo.WorkspaceCampFlowVO;
import com.jeez.zp.platform.vo.WorkspaceHomePageVO;
import com.jeez.zp.platform.vo.WorkspaceMemoItemVO;
import com.jeez.zp.platform.vo.WorkspaceMemoPageResponseVO;
import com.jeez.zp.platform.vo.WorkspaceOrdersResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class WorkspaceDashboardController {

    private final WorkspaceDashboardService workspaceDashboardService;

    @PostMapping("/report/homePage/v2")
    public HudsonResponse<WorkspaceHomePageVO> getHomePage(@RequestBody CampIdRequest request) {
        return HudsonResponse.success(
                workspaceDashboardService.getHomePage(parseLong(request.getCampId()), LoginUserContext.requiredUserId()),
                TraceIdFactory.next("report-home-page-v2")
        );
    }

    @PostMapping("/report/accommodation/management/analysis/get")
    public HudsonResponse<WorkspaceAccommodationAnalysisVO> getAccommodationAnalysis(@RequestBody WorkspaceAccommodationAnalysisRequest request) {
        return HudsonResponse.success(
                workspaceDashboardService.getAccommodationAnalysis(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getStartDate(),
                        request.getEndDate()
                ),
                TraceIdFactory.next("report-accommodation-management-analysis-get")
        );
    }

    @PostMapping("/campFlow/get")
    public HudsonResponse<WorkspaceCampFlowVO> getCampFlow(@RequestBody CampIdRequest request) {
        return HudsonResponse.success(
                workspaceDashboardService.getCampFlow(parseLong(request.getCampId()), LoginUserContext.requiredUserId()),
                TraceIdFactory.next("camp-flow-get")
        );
    }

    @PostMapping("/orders/get")
    public HudsonResponse<WorkspaceOrdersResponseVO> getOrders(@RequestBody WorkspaceOrdersRequest request) {
        return HudsonResponse.success(
                workspaceDashboardService.getOrders(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getOrderType(),
                        request.getPage(),
                        request.getPageNum(),
                        request.getCurrent(),
                        request.getPageSize(),
                        request.getKeyword()
                ),
                TraceIdFactory.next("orders-get")
        );
    }

    @PostMapping("/memo/page/get")
    public HudsonResponse<WorkspaceMemoPageResponseVO> getMemoPage(@RequestBody WorkspaceMemoPageRequest request) {
        return HudsonResponse.success(
                workspaceDashboardService.getMemoPage(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getPage(),
                        request.getPageNum(),
                        request.getCurrent(),
                        request.getPageSize(),
                        request.getIsHandle()
                ),
                TraceIdFactory.next("memo-page-get")
        );
    }

    @PostMapping("/memo/add")
    public HudsonResponse<WorkspaceMemoItemVO> addMemo(@RequestBody WorkspaceMemoCreateRequest request) {
        return HudsonResponse.success(
                workspaceDashboardService.addMemo(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getContent()
                ),
                TraceIdFactory.next("memo-add")
        );
    }

    @PostMapping("/memo/handle")
    public HudsonResponse<WorkspaceMemoItemVO> handleMemo(@RequestBody WorkspaceMemoHandleRequest request) {
        return HudsonResponse.success(
                workspaceDashboardService.handleMemo(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        parseLong(request.getMemoId()),
                        request.getIsHandle()
                ),
                TraceIdFactory.next("memo-handle")
        );
    }

    @PostMapping("/backlogs/get")
    public HudsonResponse<List<WorkspaceBacklogItemVO>> getBacklogs(@RequestBody CampIdRequest request) {
        return HudsonResponse.success(
                workspaceDashboardService.getBacklogs(parseLong(request.getCampId()), LoginUserContext.requiredUserId()),
                TraceIdFactory.next("backlogs-get")
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
}
