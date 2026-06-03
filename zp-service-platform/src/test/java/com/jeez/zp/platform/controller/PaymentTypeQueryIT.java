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

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PaymentTypeQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String CAMP_ID = "10001";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void paymentTypesGet_shouldReturnFlatCampPaymentTypesIncludingDisabledItems() throws Exception {
        insertPaymentTypeGroup(98101L, 21, "微信支付A", 2, 1, 240);
        insertPaymentTypeGroup(98102L, 22, "现金支付B", 3, 0, 241);
        insertPaymentType(98201L, 98101L, "微信支付A", 21, "微信支付A", 2, 1, 0, 1, 1, 240);
        insertPaymentType(98202L, 98102L, "现金支付B", 22, "现金支付B", 3, 0, 1, 0, 0, 241);

        mockMvc.perform(post("/paymentTypes/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"%s"}
                                """.formatted(CAMP_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.paymentTypes[*].paymentTypeId", hasItem("98201")))
                .andExpect(jsonPath("$.data.paymentTypes[*].paymentTypeId", hasItem("98202")))
                .andExpect(jsonPath("$.data.paymentTypes[?(@.paymentTypeId=='98201')].paymentTypeName", hasItem("微信支付A")))
                .andExpect(jsonPath("$.data.paymentTypes[?(@.paymentTypeId=='98201')].ignoreOrderGetItem", hasItem(1)))
                .andExpect(jsonPath("$.data.paymentTypes[?(@.paymentTypeId=='98201')].isCustom", hasItem(0)))
                .andExpect(jsonPath("$.data.paymentTypes[?(@.paymentTypeId=='98201')].isIncome", hasItem(1)))
                .andExpect(jsonPath("$.data.paymentTypes[?(@.paymentTypeId=='98201')].isEnable", hasItem(1)))
                .andExpect(jsonPath("$.data.paymentTypes[?(@.paymentTypeId=='98201')].bizType", hasItem(2)))
                .andExpect(jsonPath("$.data.paymentTypes[?(@.paymentTypeId=='98201')].groupType", hasItem(21)))
                .andExpect(jsonPath("$.data.paymentTypes[?(@.paymentTypeId=='98202')].paymentTypeName", hasItem("现金支付B")))
                .andExpect(jsonPath("$.data.paymentTypes[?(@.paymentTypeId=='98202')].isEnable", hasItem(0)))
                .andExpect(jsonPath("$.data.paymentTypes[?(@.paymentTypeId=='98202')].isCustom", hasItem(1)))
                .andExpect(jsonPath("$.data.paymentTypes[?(@.paymentTypeId=='98202')].isIncome", hasItem(0)));
    }

    @Test
    @Timeout(60)
    void paymentTypesGet_shouldFallbackCurrentCampWhenCampIdBlank() throws Exception {
        insertPaymentTypeGroup(98103L, 23, "储值支付", 2, 1, 242);
        insertPaymentType(98203L, 98103L, "储值支付", 23, "储值支付", 2, 1, 0, 0, 1, 242);

        mockMvc.perform(post("/paymentTypes/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":""}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.paymentTypes[*].paymentTypeId", hasItem("98203")))
                .andExpect(jsonPath("$.data.paymentTypes[?(@.paymentTypeId=='98203')].paymentTypeName", hasItem("储值支付")));
    }

    @Test
    @Timeout(60)
    void paymentTypesGet_shouldRejectForeignCampAccess() throws Exception {
        mockMvc.perform(post("/paymentTypes/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10002"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.message").value("无权访问当前门店支付方式"))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @Timeout(60)
    void paymentTypesGet_shouldReturnReadableMessageWhenCurrentUserCampMissing() throws Exception {
        mockMvc.perform(post("/paymentTypes/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40401))
                .andExpect(jsonPath("$.message").value("当前用户未绑定可用门店"))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    private void insertPaymentTypeGroup(
            long paymentTypeGroupId,
            int groupType,
            String groupName,
            int bizType,
            int isIncome,
            int sortNo
    ) {
        jdbcTemplate.update("""
                        INSERT INTO payment_type_group (
                            payment_type_group_id,
                            camp_id,
                            group_type,
                            group_name,
                            biz_type,
                            is_income,
                            sort_no,
                            status,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                paymentTypeGroupId,
                10001L,
                groupType,
                groupName,
                bizType,
                isIncome,
                sortNo,
                1,
                0
        );
    }

    private void insertPaymentType(
            long paymentTypeId,
            long paymentTypeGroupId,
            String paymentTypeName,
            int groupType,
            String groupName,
            int bizType,
            int isIncome,
            int isCustom,
            int ignoreOrderGetItem,
            int status,
            int sortNo
    ) {
        jdbcTemplate.update("""
                        INSERT INTO payment_type (
                            payment_type_id,
                            camp_id,
                            payment_type_group_id,
                            payment_type_name,
                            group_type,
                            group_name,
                            biz_type,
                            is_income,
                            is_custom,
                            ignore_order_get_item,
                            status,
                            sort_no,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                paymentTypeId,
                10001L,
                paymentTypeGroupId,
                paymentTypeName,
                groupType,
                groupName,
                bizType,
                isIncome,
                isCustom,
                ignoreOrderGetItem,
                status,
                sortNo,
                0
        );
    }
}
