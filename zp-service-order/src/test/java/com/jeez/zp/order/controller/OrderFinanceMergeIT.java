package com.jeez.zp.order.controller;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrderFinanceMergeIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;
    private static final long PAYMENT_WAY_ID = 83001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void orderService_shouldExposeFinancePaymentWayEndpointAfterMerge() throws Exception {
        jdbcTemplate.update("DELETE FROM payment_way WHERE payment_way_id = ?", PAYMENT_WAY_ID);
        jdbcTemplate.update(
                "INSERT INTO payment_way (payment_way_id,camp_id,payment_way_name,payment_way_code,way_type,sort_no,status,is_deleted) VALUES (?,?,?,?,?,?,1,0)",
                PAYMENT_WAY_ID,
                CAMP_ID,
                "Merged Service Test Pay",
                "merged_service_test_pay",
                "online",
                83001
        );

        mockMvc.perform(post("/paymentWays/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.paymentWays[?(@.paymentWayId == '83001')].paymentWayName").value("Merged Service Test Pay"));
    }
}
