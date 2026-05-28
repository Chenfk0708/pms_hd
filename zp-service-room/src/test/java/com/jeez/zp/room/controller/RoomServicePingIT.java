package com.jeez.zp.room.controller;

import com.jeez.zp.room.ZpServiceRoomApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ZpServiceRoomApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RoomServicePingIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Timeout(60)
    void roomServicePing_shouldExposeServiceIdentity() throws Exception {
        mockMvc.perform(get("/room-service/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.service").value("zp-service-room"))
                .andExpect(jsonPath("$.data.module").value("room-inventory"))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
