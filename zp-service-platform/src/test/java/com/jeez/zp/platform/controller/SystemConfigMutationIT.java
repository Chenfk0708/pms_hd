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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    private static final String CHECK_IN_GUIDE_SHOW_KEY = "hudson.basic.checkInGuideShowStrategy";
    private static final String CHECK_WIFI_SHOW_KEY = "hudson.basic.checkWifiShowStrategy";
    private static final String NIGHT_AUDIT_ENABLED_KEY = "hudson.finance.isNightAudit";
    private static final String NIGHT_AUDIT_TIME_KEY = "hudson.finance.autoNightAuditTime";
    private static final String FINANCE_STRATEGY_KEY = "hudson.finance.orderAmortizeStrategy";
    private static final String VENDIBLE_TYPES_KEY = "hudson.finance.vendibleTypes";

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

    @Test
    @Timeout(60)
    void checkInGuideShowStrategy_shouldPersistRealtimeTogglePayload() throws Exception {
        MvcResult result = mockMvc.perform(put("/systemConfig/checkInGuideShowStrategy")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "isCheckInGuideIdentityRegCompleted":1,
                                  "isCheckInGuideVerifyPayDeposit":0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        JsonNode configValue = findConfigNode(readDataNode(result), CHECK_IN_GUIDE_SHOW_KEY);
        assertThat(configValue.path("isCheckInGuideIdentityRegCompleted").asInt()).isEqualTo(1);
        assertThat(configValue.path("isCheckInGuideVerifyPayDeposit").asInt()).isEqualTo(0);

        MvcResult getResult = mockMvc.perform(post("/systemConfig/checkInGuideShowStrategy/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        JsonNode detailValue = readDataNode(getResult).path("configValue");
        assertThat(detailValue.path("isCheckInGuideIdentityRegCompleted").asInt()).isEqualTo(1);
        assertThat(detailValue.path("isCheckInGuideVerifyPayDeposit").asInt()).isEqualTo(0);
    }

    @Test
    @Timeout(60)
    void checkWifiShowStrategy_shouldPersistRealtimeTogglePayload() throws Exception {
        MvcResult result = mockMvc.perform(put("/systemConfig/checkWifiShowStrategy")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "isWifiDisplayEnabled":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        JsonNode configValue = findConfigNode(readDataNode(result), CHECK_WIFI_SHOW_KEY);
        assertThat(configValue.path("isWifiDisplayEnabled").asInt()).isEqualTo(1);

        MvcResult getResult = mockMvc.perform(post("/systemConfig/checkWifiShowStrategy/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        JsonNode detailValue = readDataNode(getResult).path("configValue");
        assertThat(detailValue.path("isWifiDisplayEnabled").asInt()).isEqualTo(1);
    }

    @Test
    @Timeout(60)
    void financeSettingMutations_shouldPersistNightAuditStrategyAndVendibleTypes() throws Exception {
        MvcResult nightAuditResult = mockMvc.perform(post("/systemConfigs/nightAudit/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","isNightAudit":1,"autoNightAuditTime":3}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        assertThat(findConfigNode(readDataNode(nightAuditResult), NIGHT_AUDIT_ENABLED_KEY).asInt()).isEqualTo(1);
        assertThat(findConfigNode(readDataNode(nightAuditResult), NIGHT_AUDIT_TIME_KEY).asInt()).isEqualTo(3);

        MvcResult strategyResult = mockMvc.perform(post("/systemConfigs/financeStrategy/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","orderAmortizeStrategy":2}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        assertThat(findConfigNode(readDataNode(strategyResult), FINANCE_STRATEGY_KEY).asInt()).isEqualTo(2);

        MvcResult vendibleResult = mockMvc.perform(post("/systemConfigs/vendibleTypes/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","vendibleTypes":[1,3,5]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        JsonNode vendibleTypes = findConfigNode(readDataNode(vendibleResult), VENDIBLE_TYPES_KEY);
        assertThat(vendibleTypes.isArray()).isTrue();
        assertThat(vendibleTypes.get(0).asInt()).isEqualTo(1);
        assertThat(vendibleTypes.get(1).asInt()).isEqualTo(3);
        assertThat(vendibleTypes.get(2).asInt()).isEqualTo(5);

        MvcResult getResult = mockMvc.perform(post("/systemConfigs/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        JsonNode dataNode = readDataNode(getResult);
        assertThat(findConfigNode(dataNode, NIGHT_AUDIT_ENABLED_KEY).asInt()).isEqualTo(1);
        assertThat(findConfigNode(dataNode, NIGHT_AUDIT_TIME_KEY).asInt()).isEqualTo(3);
        assertThat(findConfigNode(dataNode, FINANCE_STRATEGY_KEY).asInt()).isEqualTo(2);
        assertThat(findConfigNode(dataNode, VENDIBLE_TYPES_KEY).get(2).asInt()).isEqualTo(5);
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
        JsonNode valueNode = findConfigNode(dataNode, configKey);
        return valueNode.isMissingNode() ? null : valueNode.isTextual() ? valueNode.asText() : valueNode.toString();
    }

    private JsonNode findConfigNode(JsonNode dataNode, String configKey) {
        for (JsonNode configNode : dataNode.path("configs")) {
            if (configKey.equals(configNode.path("configKey").asText())) {
                return configNode.path("configValue");
            }
        }
        return objectMapper.missingNode();
    }
}
