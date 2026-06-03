package com.jeez.zp.crm.controller;

import com.jeez.zp.crm.api.HudsonResponse;
import com.jeez.zp.crm.api.TraceIdFactory;
import com.jeez.zp.crm.dto.request.ScrmWechatQueryRequest;
import com.jeez.zp.crm.security.LoginUserContext;
import com.jeez.zp.crm.service.ScrmWechatService;
import com.jeez.zp.crm.vo.ScrmSidebarDashboardResponseVO;
import com.jeez.zp.crm.vo.ScrmSidebarExportResponseVO;
import com.jeez.zp.crm.vo.WechatKfAccountPageResponseVO;
import com.jeez.zp.crm.vo.WechatKfAccountReportResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ScrmWechatController {

    private final ScrmWechatService scrmWechatService;

    @PostMapping("/wxcp/kfAccount/page/get")
    public HudsonResponse<WechatKfAccountPageResponseVO> getKfAccounts(@RequestBody ScrmWechatQueryRequest request) {
        return HudsonResponse.success(
                scrmWechatService.getKfAccounts(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("wxcp-kf-account-page-get")
        );
    }

    @PostMapping("/wxcp/kfAccount/report/get")
    public HudsonResponse<WechatKfAccountReportResponseVO> getKfReport(@RequestBody ScrmWechatQueryRequest request) {
        return HudsonResponse.success(
                scrmWechatService.getKfReport(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("wxcp-kf-account-report-get")
        );
    }

    @PostMapping("/scrm/sidebarPreview/dashboard")
    public HudsonResponse<ScrmSidebarDashboardResponseVO> getSidebarDashboard(@RequestBody ScrmWechatQueryRequest request) {
        return HudsonResponse.success(
                scrmWechatService.getSidebarDashboard(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("scrm-sidebar-preview-dashboard")
        );
    }

    @PostMapping("/scrm/sidebarPreview/export")
    public HudsonResponse<ScrmSidebarExportResponseVO> exportSidebar(@RequestBody ScrmWechatQueryRequest request) {
        return HudsonResponse.success(
                scrmWechatService.exportSidebar(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("scrm-sidebar-preview-export")
        );
    }
}
