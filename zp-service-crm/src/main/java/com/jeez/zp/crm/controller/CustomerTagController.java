package com.jeez.zp.crm.controller;

import com.jeez.zp.crm.api.HudsonResponse;
import com.jeez.zp.crm.api.TraceIdFactory;
import com.jeez.zp.crm.dto.request.CampRequest;
import com.jeez.zp.crm.dto.request.CustomerTagPageRequest;
import com.jeez.zp.crm.dto.request.CustomerTagSaveRequest;
import com.jeez.zp.crm.security.LoginUserContext;
import com.jeez.zp.crm.service.CustomerTagService;
import com.jeez.zp.crm.vo.CustomerTagExportResponseVO;
import com.jeez.zp.crm.vo.CustomerTagPageResponseVO;
import com.jeez.zp.crm.vo.CustomerTagSaveResponseVO;
import com.jeez.zp.crm.vo.WeComAccountsResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CustomerTagController {

    private final CustomerTagService customerTagService;

    @PostMapping("/memberTagGroup/page/get")
    public HudsonResponse<CustomerTagPageResponseVO> getPage(@RequestBody CustomerTagPageRequest request) {
        return HudsonResponse.success(
                customerTagService.getPage(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("member-tag-group-page-get")
        );
    }

    @PostMapping("/memberTagGroup/save")
    public HudsonResponse<CustomerTagSaveResponseVO> save(@RequestBody CustomerTagSaveRequest request) {
        return HudsonResponse.success(
                customerTagService.save(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("member-tag-group-save")
        );
    }

    @PostMapping("/memberTagGroup/export")
    public HudsonResponse<CustomerTagExportResponseVO> export(@RequestBody CustomerTagPageRequest request) {
        return HudsonResponse.success(
                customerTagService.export(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("member-tag-group-export")
        );
    }

    @PostMapping("/wxCpOpen/accounts/get")
    public HudsonResponse<WeComAccountsResponseVO> getWeComAccounts(@RequestBody CampRequest request) {
        return HudsonResponse.success(
                customerTagService.getWeComAccounts(parseLong(request == null ? null : request.getCampId()), LoginUserContext.requiredUserId()),
                TraceIdFactory.next("wxcp-open-accounts-get")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
