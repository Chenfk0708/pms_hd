package com.jeez.zp.order.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class OrderServicePingController {

    @GetMapping("/order-service/ping")
    public Map<String, String> ping() {
        return Map.of("service", "zp-service-order", "status", "ok");
    }
}
