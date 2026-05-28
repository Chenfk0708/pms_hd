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
class HudsonResponseCompatibilityIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Timeout(60)
    void roomCategoriesPageGet_shouldExposeLegacySuccessFlagForFrontendCompatibility() throws Exception {
        mockMvc.perform(post("/roomCategories/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"",
                                  "pageSize":20,
                                  "pageNum":1,
                                  "roomCategoryName":"不存在的房型",
                                  "keyword":"",
                                  "cityIds":[],
                                  "channelId":""
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.list.length()").value(0));
    }

    @Test
    @Timeout(60)
    void roomCategoriesAndRooms_shouldTreatNonNumericCampIdAsCurrentCampFallbackForFrontendCompatibility() throws Exception {
        mockMvc.perform(post("/roomCategories/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"mock-camp-main",
                                  "pageSize":20,
                                  "pageNum":1,
                                  "roomCategoryName":"不存在的房型",
                                  "keyword":"",
                                  "cityIds":[],
                                  "channelId":""
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/rooms/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"mock-camp-main",
                                  "roomCategoryIds":["22001"],
                                  "saleType":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.roomCategoryRooms.length()").value(1));
    }

    @Test
    @Timeout(60)
    void roomCategoriesPageGet_shouldExposeLegacyErrorFieldsForFrontendCompatibility() throws Exception {
        mockMvc.perform(post("/roomCategories/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "pageSize":20,
                                  "pageNum":1,
                                  "roomCategoryName":"",
                                  "keyword":"",
                                  "cityIds":[],
                                  "channelId":""
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").value("无权访问当前门店房型数据"));
    }
}
