package com.jeez.zp.crm.service;

import com.jeez.zp.crm.dto.request.ScrmWechatQueryRequest;
import com.jeez.zp.crm.vo.ScrmSidebarDashboardResponseVO;
import com.jeez.zp.crm.vo.ScrmSidebarExportResponseVO;
import com.jeez.zp.crm.vo.WechatKfAccountPageResponseVO;
import com.jeez.zp.crm.vo.WechatKfAccountReportResponseVO;

public interface ScrmWechatService {

    WechatKfAccountPageResponseVO getKfAccounts(ScrmWechatQueryRequest request, Long userId);

    WechatKfAccountReportResponseVO getKfReport(ScrmWechatQueryRequest request, Long userId);

    ScrmSidebarDashboardResponseVO getSidebarDashboard(ScrmWechatQueryRequest request, Long userId);

    ScrmSidebarExportResponseVO exportSidebar(ScrmWechatQueryRequest request, Long userId);
}
