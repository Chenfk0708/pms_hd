package com.jeez.zp.platform.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SystemConfigMutationIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String CAMP_ID = "10001";
    private static final String ORDER_AUTO_PENDING_KEY = "hudson.basic.orderAutoPendingStrategy";
    private static final String ORDER_AUTO_SETTLE_KEY = "hudson.basic.orderAutoSettleStrategy";
    private static final String NEGOTIATE_REFUND_KEY = "hudson.basic.negotiateRefundAutomaticAcceptStrategy";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Timeout(60)
    void orderAutoPendingStrategy_shouldCreateCampConfig() throws Exception {
        String configValue = "2";

        MvcResult result = mockMvc.perform(post("/systemConfig/orderAutoPendingStrategy")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"%s","configKey":"%s","configValue":"%s"}
                                """.formatted(CAMP_ID, ORDER_AUTO_PENDING_KEY, configValue)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        assertThat(findConfigValue(readDataNode(result), ORDER_AUTO_PENDING_KEY)).isEqualTo(configValue);
        assertSystemConfigValue(ORDER_AUTO_PENDING_KEY, configValue);
    }

    @Test
    @Timeout(60)
    void orderAutoSettleStrategy_shouldCreateCampConfig() throws Exception {
        String configValue = "1";

        MvcResult result = mockMvc.perform(post("/systemConfig/orderAutoSettleStrategy")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"%s","configKey":"%s","configValue":"%s"}
                                """.formatted(CAMP_ID, ORDER_AUTO_SETTLE_KEY, configValue)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        assertThat(findConfigValue(readDataNode(result), ORDER_AUTO_SETTLE_KEY)).isEqualTo(configValue);
        assertSystemConfigValue(ORDER_AUTO_SETTLE_KEY, configValue);
    }

    @Test
    @Timeout(60)
    void negotiateRefundAutomaticAcceptStrategy_shouldCreateCampConfig() throws Exception {
        String configValue = "1";

        MvcResult result = mockMvc.perform(post("/systemConfig/negotiateRefundAutomaticAcceptStrategy")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"%s","configKey":"%s","configValue":"%s"}
                                """.formatted(CAMP_ID, NEGOTIATE_REFUND_KEY, configValue)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        assertThat(findConfigValue(readDataNode(result), NEGOTIATE_REFUND_KEY)).isEqualTo(configValue);
        assertSystemConfigValue(NEGOTIATE_REFUND_KEY, configValue);
    }

    private void assertSystemConfigValue(String configKey, String expectedValue) throws Exception {
        MvcResult result = mockMvc.perform(post("/systemConfigs/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"%s"}
                                """.formatted(CAMP_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        assertThat(findConfigValue(readDataNode(result), configKey)).isEqualTo(expectedValue);
    }

    private JsonNode readDataNode(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsByteArray()).path("data");
    }

    private String findConfigValue(JsonNode dataNode, String configKey) {
        for (JsonNode configNode : dataNode.path("configs")) {
            if (configKey.equals(configNode.path("configKey").asText())) {
                JsonNode valueNode = configNode.path("configValue");
                return valueNode.isTextual() ? valueNode.asText() : valueNode.toString();
            }
        }
        return null;
    }
}
