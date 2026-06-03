package com.jeez.zp.platform.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PrintSettingControllerIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Timeout(60)
    void getAndSave_shouldReturnPrintableSections() throws Exception {
        mockMvc.perform(post("/printSettings/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.sections[0].key").value("stay"))
                .andExpect(jsonPath("$.data.sections[0].paperType").value("80mm"))
                .andExpect(jsonPath("$.data.sections[1].key").value("receipt"));

        mockMvc.perform(post("/printSettings/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "section":"stay",
                                  "paperType":"58mm",
                                  "selectedDocument":"stay-register-short",
                                  "customText":"print setting integration"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.sections[0].key").value("stay"))
                .andExpect(jsonPath("$.data.sections[0].paperType").value("58mm"))
                .andExpect(jsonPath("$.data.sections[0].selectedDocument").value("stay-register-short"))
                .andExpect(jsonPath("$.data.sections[0].customText").value("print setting integration"));
    }
}
