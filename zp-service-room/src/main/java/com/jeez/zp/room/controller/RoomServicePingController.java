package com.jeez.zp.room.controller;

import com.jeez.zp.room.api.HudsonResponse;
import com.jeez.zp.room.api.TraceIdFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RoomServicePingController {

    @GetMapping("/room-service/ping")
    public HudsonResponse<Map<String, String>> ping() {
        return HudsonResponse.success(
                Map.of(
                        "service", "zp-service-room",
                        "module", "room-inventory"
                ),
                TraceIdFactory.next("room-service-ping")
        );
    }
}
