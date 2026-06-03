package com.jeez.zp.platform.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PsbLogControllerIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void psbLogPageGet_shouldReturnFrontendFieldsFromCurrentCampOrders() throws Exception {
        seedPsbOrder(98101L, 98201L, "PSB20260531001", "PSB-CHANNEL-001", "公安测试客人", "13888880001", "440301199901011234");

        mockMvc.perform(post("/checkinGuestPsbLog/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":1,
                                  "pageSize":20,
                                  "psbType":["4","5"],
                                  "keyword":"PSB20260531001"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value("98201"))
                .andExpect(jsonPath("$.data.list[0].guestName").value("公安测试客人"))
                .andExpect(jsonPath("$.data.list[0].name").value("公安测试客人"))
                .andExpect(jsonPath("$.data.list[0].mobile").value("13888880001"))
                .andExpect(jsonPath("$.data.list[0].idCard").value("440301199901011234"))
                .andExpect(jsonPath("$.data.list[0].roomNo").value("101"))
                .andExpect(jsonPath("$.data.list[0].orderSource").value("携程"))
                .andExpect(jsonPath("$.data.list[0].channelName").value("携程"))
                .andExpect(jsonPath("$.data.list[0].orderNo").value("PSB20260531001"))
                .andExpect(jsonPath("$.data.list[0].channelOrderNo").value("PSB-CHANNEL-001"))
                .andExpect(jsonPath("$.data.list[0].bizType").value("5"))
                .andExpect(jsonPath("$.data.list[0].state").value("0"))
                .andExpect(jsonPath("$.data.list[0].remark").value("公安回执：待重新上报"))
                .andExpect(jsonPath("$.data.list[0].receiptMessage").value("公安回执：待重新上报"))
                .andExpect(jsonPath("$.data.list[0].poiId").value("11001"))
                .andExpect(jsonPath("$.data.list[0].uploadTime").exists());
    }

    @Test
    @Timeout(60)
    void psbLogRetry_shouldReturnSuccessfulUpdatedRowForAccessibleOrder() throws Exception {
        seedPsbOrder(98102L, 98202L, "PSB20260531002", "PSB-CHANNEL-002", "公安重报客人", "13888880002", "440301199902022345");

        MvcResult result = mockMvc.perform(post("/checkinGuestPsbLog/retry")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "id":"98202",
                                  "orderNo":"PSB20260531002"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("98202"))
                .andExpect(jsonPath("$.data.orderNo").value("PSB20260531002"))
                .andExpect(jsonPath("$.data.guestName").value("公安重报客人"))
                .andExpect(jsonPath("$.data.state").value("1"))
                .andExpect(jsonPath("$.data.bizType").value("5"))
                .andExpect(jsonPath("$.data.remark").value("重新上报成功，公安回执已更新"))
                .andExpect(jsonPath("$.data.receiptMessage").value("公安回执：重新上报成功"))
                .andExpect(jsonPath("$.data.reportTime").exists())
                .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsByteArray()).path("data");
        assertThat(data.path("reportTime").asText()).isNotBlank();
    }

    @Test
    @Timeout(60)
    void psbLogEndpoints_shouldFallbackCurrentCampAndRejectForeignCampAccess() throws Exception {
        seedPsbOrder(98103L, 98203L, "PSB20260531003", "PSB-CHANNEL-003", "公安默认门店", "13888880003", "440301199903033456");

        mockMvc.perform(post("/checkinGuestPsbLog/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"",
                                  "pageNum":1,
                                  "pageSize":20,
                                  "keyword":"PSB20260531003"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].orderNo").value("PSB20260531003"));

        mockMvc.perform(post("/checkinGuestPsbLog/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "pageNum":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").value("无权访问当前门店公安上报日志"));
    }

    private void seedPsbOrder(
            long orderId,
            long guestId,
            String orderNo,
            String channelOrderNo,
            String guestName,
            String mobile,
            String idCard
    ) {
        jdbcTemplate.update("DELETE FROM order_guest WHERE id = ?", guestId);
        jdbcTemplate.update("DELETE FROM order_main WHERE order_id = ?", orderId);

        jdbcTemplate.update("""
                        INSERT INTO order_main (
                            order_id,
                            camp_id,
                            poi_id,
                            room_category_id,
                            room_id,
                            channel_id,
                            order_no,
                            out_order_no,
                            order_type,
                            status,
                            guest_name,
                            guest_mobile,
                            start_at,
                            end_at,
                            day_num,
                            total_price_cent,
                            total_pay_price_cent,
                            payment_status,
                            source_type,
                            created_at,
                            updated_at,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                orderId,
                CAMP_ID,
                11001L,
                22001L,
                23001L,
                25302L,
                orderNo,
                channelOrderNo,
                "daily",
                "checked_in",
                guestName,
                mobile,
                "2026-05-31 14:00:00",
                "2026-06-01 12:00:00",
                1,
                29900,
                29900,
                "paid",
                "channel",
                "2026-05-31 10:00:00",
                "2026-05-31 10:05:00",
                0
        );
        jdbcTemplate.update("""
                        INSERT INTO order_guest (
                            id,
                            order_id,
                            guest_name,
                            guest_mobile,
                            guest_id_card,
                            guest_type,
                            created_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                guestId,
                orderId,
                guestName,
                mobile,
                idCard,
                "adult",
                "2026-05-31 10:03:00"
        );
    }
}
