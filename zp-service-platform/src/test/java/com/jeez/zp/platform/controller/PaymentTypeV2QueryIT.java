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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PaymentTypeV2QueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String CAMP_ID = "10001";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void paymentTypesGetV2_shouldReturnGroupedEnabledPaymentTypesByBizType() throws Exception {
        insertPaymentTypeGroup(97101L, 11, "测试收入组", 91, 1, 10);
        insertPaymentTypeGroup(97102L, 12, "测试支出组", 91, 0, 20);
        insertPaymentType(97201L, 97101L, "测试收入项A", 11, "测试收入组", 91, 1, 0, 1, 10);
        insertPaymentType(97202L, 97101L, "测试收入项B", 11, "测试收入组", 91, 1, 1, 0, 20);
        insertPaymentType(97203L, 97102L, "测试支出项C", 12, "测试支出组", 91, 0, 0, 0, 30);
        insertPaymentTypeDisabled(97204L, 97102L, "已禁用项", 12, "测试支出组", 91, 0, 0, 0, 40);

        mockMvc.perform(post("/paymentTypes/get/v2")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"%s","bizTypes":[91],"isEnable":1}
                                """.formatted(CAMP_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.paymentGroups.length()").value(2))
                .andExpect(jsonPath("$.data.paymentGroups[0].groupType").value(11))
                .andExpect(jsonPath("$.data.paymentGroups[0].groupTypeName").value("测试收入组"))
                .andExpect(jsonPath("$.data.paymentGroups[0].paymentTypes.length()").value(2))
                .andExpect(jsonPath("$.data.paymentGroups[0].paymentTypes[0].paymentTypeId").value("97201"))
                .andExpect(jsonPath("$.data.paymentGroups[0].paymentTypes[0].paymentTypeName").value("测试收入项A"))
                .andExpect(jsonPath("$.data.paymentGroups[0].paymentTypes[0].ignoreOrderGetItem").value(1))
                .andExpect(jsonPath("$.data.paymentGroups[0].paymentTypes[0].isCustom").value(0))
                .andExpect(jsonPath("$.data.paymentGroups[0].paymentTypes[0].isIncome").value(1))
                .andExpect(jsonPath("$.data.paymentGroups[0].paymentTypes[0].isEnable").value(1))
                .andExpect(jsonPath("$.data.paymentGroups[0].paymentTypes[0].bizType").value(91))
                .andExpect(jsonPath("$.data.paymentGroups[0].paymentTypes[0].groupType").value(11))
                .andExpect(jsonPath("$.data.paymentGroups[1].groupType").value(12))
                .andExpect(jsonPath("$.data.paymentGroups[1].paymentTypes.length()").value(1))
                .andExpect(jsonPath("$.data.paymentGroups[1].paymentTypes[0].paymentTypeId").value("97203"));
    }

    @Test
    @Timeout(60)
    void paymentTypesGetV2_shouldFallbackCurrentCampAndReturnEmptyGroupsWhenNoMatch() throws Exception {
        mockMvc.perform(post("/paymentTypes/get/v2")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"","bizTypes":[999],"isEnable":1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.paymentGroups.length()").value(0));
    }

    @Test
    @Timeout(60)
    void paymentTypesGetV2_shouldRejectForeignCampAccess() throws Exception {
        mockMvc.perform(post("/paymentTypes/get/v2")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10002","bizTypes":[91],"isEnable":1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
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
                1,
                sortNo,
                0
        );
    }

    private void insertPaymentTypeDisabled(
            long paymentTypeId,
            long paymentTypeGroupId,
            String paymentTypeName,
            int groupType,
            String groupName,
            int bizType,
            int isIncome,
            int isCustom,
            int ignoreOrderGetItem,
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
                0,
                sortNo,
                0
        );
    }
}
