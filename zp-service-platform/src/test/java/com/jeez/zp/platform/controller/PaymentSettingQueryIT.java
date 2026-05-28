package com.jeez.zp.platform.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PaymentSettingQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CURRENT_USER_ID = 12001L;
    private static final long ISOLATED_CAMP_ID = 29961L;
    private static final long ENABLED_PAYMENT_WAY_ID = 29971L;
    private static final long DISABLED_PAYMENT_WAY_ID = 29972L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void paymentSettingsList_shouldReturnMethodsForCurrentCampAndKeepFrontendContract() throws Exception {
        seedPaymentSettingScene();

        mockMvc.perform(post("/paymentSettings/list")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"1796067693589061634",
                                  "includeDisabled":true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.notice", containsString("支付方式")))
                .andExpect(jsonPath("$.data.methods.length()").value(2))
                .andExpect(jsonPath("$.data.methods[0].id").value("29971"))
                .andExpect(jsonPath("$.data.methods[0].code").value("wechat"))
                .andExpect(jsonPath("$.data.methods[0].name").value("微信支付联调"))
                .andExpect(jsonPath("$.data.methods[0].status").value("enabled"))
                .andExpect(jsonPath("$.data.methods[0].isSystemDefault").value(true))
                .andExpect(jsonPath("$.data.methods[0].isPreferred").value(true))
                .andExpect(jsonPath("$.data.methods[0].description", containsString("线上")))
                .andExpect(jsonPath("$.data.methods[0].availableScopes[0]").value("线上收款"))
                .andExpect(jsonPath("$.data.methods[0].settlementAccount").value("线上支付清分账户"))
                .andExpect(jsonPath("$.data.methods[0].lastUsedAt").value("未使用"))
                .andExpect(jsonPath("$.data.methods[0].updatedAt").value("2026-05-20 09:15"))
                .andExpect(jsonPath("$.data.methods[0].remark").value("系统支付方式：微信支付联调"))
                .andExpect(jsonPath("$.data.methods[1].id").value("29972"))
                .andExpect(jsonPath("$.data.methods[1].status").value("disabled"))
                .andExpect(jsonPath("$.data.methods[1].isPreferred").value(false));
    }

    @Test
    @Timeout(60)
    void paymentSettingsDetail_shouldReturnMethodDetailByMethodId() throws Exception {
        seedPaymentSettingScene();

        mockMvc.perform(post("/paymentSettings/detail")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"methodId":"29972"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("29972"))
                .andExpect(jsonPath("$.data.code").value("cash"))
                .andExpect(jsonPath("$.data.name").value("现金联调"))
                .andExpect(jsonPath("$.data.status").value("disabled"))
                .andExpect(jsonPath("$.data.isSystemDefault").value(true))
                .andExpect(jsonPath("$.data.isPreferred").value(false))
                .andExpect(jsonPath("$.data.availableScopes[0]").value("前台收款"))
                .andExpect(jsonPath("$.data.settlementAccount").value("门店现金账户"))
                .andExpect(jsonPath("$.data.usageCountLabel").value("当前停用，暂无收款记录"));
    }

    private void seedPaymentSettingScene() {
        insertCamp();
        rebindCurrentUserCamp();
        insertPaymentWay(
                ENABLED_PAYMENT_WAY_ID,
                "微信支付联调",
                "wechat",
                "online",
                1,
                1,
                LocalDateTime.of(2026, 5, 20, 9, 15, 0)
        );
        insertPaymentWay(
                DISABLED_PAYMENT_WAY_ID,
                "现金联调",
                "cash",
                "offline",
                2,
                0,
                LocalDateTime.of(2026, 5, 19, 18, 30, 0)
        );
    }

    private void rebindCurrentUserCamp() {
        jdbcTemplate.update("UPDATE pms_member SET camp_id = ? WHERE user_id = ?", ISOLATED_CAMP_ID, CURRENT_USER_ID);
    }

    private void insertCamp() {
        jdbcTemplate.update("""
                        INSERT INTO pms_camp (
                            camp_id,
                            name,
                            type,
                            city_name,
                            address,
                            contact_number,
                            status,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                ISOLATED_CAMP_ID,
                "支付设置联调门店",
                1,
                "深圳",
                "南山区支付设置联调路 99 号",
                "0755-2996101",
                1,
                0
        );
    }

    private void insertPaymentWay(
            long paymentWayId,
            String name,
            String code,
            String wayType,
            int sortNo,
            int status,
            LocalDateTime updatedAt
    ) {
        Timestamp timestamp = Timestamp.valueOf(updatedAt);
        jdbcTemplate.update("""
                        INSERT INTO payment_way (
                            payment_way_id,
                            camp_id,
                            payment_way_name,
                            payment_way_code,
                            way_type,
                            sort_no,
                            status,
                            created_at,
                            updated_at,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                paymentWayId,
                ISOLATED_CAMP_ID,
                name,
                code,
                wayType,
                sortNo,
                status,
                timestamp,
                timestamp,
                0
        );
    }
}
