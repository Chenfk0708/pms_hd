package com.jeez.zp.crm.controller;

import com.jeez.zp.crm.api.HudsonResponse;
import com.jeez.zp.crm.api.TraceIdFactory;
import com.jeez.zp.crm.dto.request.CustomerDetailRequest;
import com.jeez.zp.crm.dto.request.CustomerPageRequest;
import com.jeez.zp.crm.dto.request.CustomerSaveRequest;
import com.jeez.zp.crm.security.LoginUserContext;
import com.jeez.zp.crm.service.CustomerService;
import com.jeez.zp.crm.vo.CustomerItemVO;
import com.jeez.zp.crm.vo.CustomerPageResponseVO;
import com.jeez.zp.crm.vo.CustomerSaveResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/customers/page/get")
    public HudsonResponse<CustomerPageResponseVO> getPage(@RequestBody CustomerPageRequest request) {
        return HudsonResponse.success(
                customerService.getPage(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("customers-page-get")
        );
    }

    @PostMapping("/customers/detail/get")
    public HudsonResponse<CustomerItemVO> getDetail(@RequestBody CustomerDetailRequest request) {
        return HudsonResponse.success(
                customerService.getDetail(parseLong(request.getCampId()), parseLong(request.getCustomerId()), LoginUserContext.requiredUserId()),
                TraceIdFactory.next("customers-detail-get")
        );
    }

    @PostMapping("/customers/save")
    public HudsonResponse<CustomerSaveResponseVO> save(@RequestBody CustomerSaveRequest request) {
        return HudsonResponse.success(
                customerService.save(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("customers-save")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
