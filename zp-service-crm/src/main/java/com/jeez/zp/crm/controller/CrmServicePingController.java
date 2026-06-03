package com.jeez.zp.crm.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CrmServicePingController {

    @GetMapping("/crm-service/ping")
    public Map<String, String> ping() {
        return Map.of("service", "zp-service-crm", "status", "ok");
    }
}
