package com.jeez.zp.finance.controller;

import com.jeez.zp.finance.api.HudsonResponse;
import com.jeez.zp.finance.api.TraceIdFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FinanceServicePingController {

    @GetMapping("/finance-service/ping")
    public HudsonResponse<String> ping() {
        return HudsonResponse.success("finance-service-ok", TraceIdFactory.next("finance-service-ping"));
    }
}
