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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.hamcrest.Matchers.closeTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DistributionFlowQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CURRENT_USER_ID = 12001L;
    private static final long ISOLATED_CAMP_ID = 29891L;
    private static final long ISOLATED_POI_ID = 29892L;
    private static final long ROOM_CATEGORY_ID = 29893L;
    private static final long CHANNEL_ACCOUNT_ID_1 = 29894L;
    private static final long CHANNEL_ACCOUNT_ID_2 = 29895L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void reportFlowsGet_shouldReturnNonBreakTempDistributionRows() throws Exception {
        seedDistributionOrders();

        mockMvc.perform(post("/report/flows/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"29891",
                                  "pageNum":1,
                                  "pageSize":20,
                                  "current":1,
                                  "bookingStartDate":"2026-05-01",
                                  "bookingEndDate":"2026-05-31",
                                  "keyword":"",
                                  "breakTemp":false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pages").value(1))
                .andExpect(jsonPath("$.data.hasNextPage").value(false))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].orderNo").value("OUT-ORDER-29871"))
                .andExpect(jsonPath("$.data.list[0].customerName").value("分销客人A"))
                .andExpect(jsonPath("$.data.list[0].customerPhone").value("13900002001"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryName").value("分销联调房型"))
                .andExpect(jsonPath("$.data.list[0].invoicePrice").value(closeTo(435.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].commission").value(closeTo(65.25, 0.001)))
                .andExpect(jsonPath("$.data.list[0].incomePrice").value(closeTo(369.75, 0.001)))
                .andExpect(jsonPath("$.data.list[0].settledPrice").value(closeTo(0.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].paidAmount").value(closeTo(435.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].serviceFee").value(closeTo(65.25, 0.001)))
                .andExpect(jsonPath("$.data.list[0].settlementAmount").value(closeTo(369.75, 0.001)))
                .andExpect(jsonPath("$.data.list[0].settledAmount").value(closeTo(0.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].settledState").value("pending"))
                .andExpect(jsonPath("$.data.list[0].settlementStatus").value("待结算"))
                .andExpect(jsonPath("$.data.list[0].orderFilter").value("非置换订单"));
    }

    @Test
    @Timeout(60)
    void reportFlowsGet_shouldReturnOnlyBreakTempRows() throws Exception {
        seedDistributionOrders();

        mockMvc.perform(post("/report/flows/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"29891",
                                  "pageNum":1,
                                  "pageSize":20,
                                  "current":1,
                                  "bookingStartDate":"2026-05-01",
                                  "bookingEndDate":"2026-05-31",
                                  "keyword":"",
                                  "breakTemp":true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].orderNo").value("OUT-ORDER-29872"))
                .andExpect(jsonPath("$.data.list[0].settledState").value("settled"))
                .andExpect(jsonPath("$.data.list[0].settlementStatus").value("已结算"))
                .andExpect(jsonPath("$.data.list[0].orderFilter").value("置换订单"));
    }

    @Test
    @Timeout(60)
    void reportFlowsGet_shouldReturnAllRowsWhenBreakTempNotProvided() throws Exception {
        seedDistributionOrders();

        mockMvc.perform(post("/report/flows/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"29891",
                                  "pageNum":1,
                                  "pageSize":20,
                                  "current":1,
                                  "bookingStartDate":"2026-05-01",
                                  "bookingEndDate":"2026-05-31",
                                  "keyword":""
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].orderNo").value("OUT-ORDER-29871"))
                .andExpect(jsonPath("$.data.list[0].orderFilter").value("非置换订单"))
                .andExpect(jsonPath("$.data.list[1].orderNo").value("OUT-ORDER-29872"))
                .andExpect(jsonPath("$.data.list[1].settledState").value("settled"))
                .andExpect(jsonPath("$.data.list[1].orderFilter").value("置换订单"));
    }

    @Test
    @Timeout(60)
    void distributionOrdersPageGet_shouldReturnFrontendDistributionOrderContract() throws Exception {
        seedDistributionOrders();

        mockMvc.perform(post("/distribution/orders/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"29891",
                                  "pageNum":1,
                                  "pageSize":20,
                                  "current":1,
                                  "bookingStartDate":"2026-05-01",
                                  "bookingEndDate":"2026-05-31",
                                  "keyword":"",
                                  "settledState":"pending"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.camp.campId").value("29891"))
                .andExpect(jsonPath("$.data.camp.campName").value("分销联调门店"))
                .andExpect(jsonPath("$.data.pagination.page").value(1))
                .andExpect(jsonPath("$.data.pagination.pageSize").value(20))
                .andExpect(jsonPath("$.data.pagination.total").value(1))
                .andExpect(jsonPath("$.data.summary.invoicePrice").value(closeTo(435.00, 0.001)))
                .andExpect(jsonPath("$.data.summary.commission").value(closeTo(65.25, 0.001)))
                .andExpect(jsonPath("$.data.summary.incomePrice").value(closeTo(369.75, 0.001)))
                .andExpect(jsonPath("$.data.summary.settledPrice").value(closeTo(0.00, 0.001)))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].orderId").value("OUT-ORDER-29871"))
                .andExpect(jsonPath("$.data.list[0].customerInfo").value("分销客人A/13900002001"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryName").value("分销联调房型"))
                .andExpect(jsonPath("$.data.list[0].bookedTime").value("2026-05-13 11:50:49"))
                .andExpect(jsonPath("$.data.list[0].invoicePrice").value(closeTo(435.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].commission").value(closeTo(65.25, 0.001)))
                .andExpect(jsonPath("$.data.list[0].incomePrice").value(closeTo(369.75, 0.001)))
                .andExpect(jsonPath("$.data.list[0].settledPrice").value(closeTo(0.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].settledState").value("pending"));
    }

    private void seedDistributionOrders() {
        insertCamp();
        rebindCurrentUserCamp();
        insertPoi();
        insertChannelAccount(CHANNEL_ACCOUNT_ID_1, 5L, "携程");
        insertChannelAccount(CHANNEL_ACCOUNT_ID_2, 7L, "美团");
        insertRoomCategory();

        insertOrderMain(29871L, CHANNEL_ACCOUNT_ID_1, "分销客人A", "13900002001",
                LocalDate.of(2026, 5, 13), LocalDate.of(2026, 5, 14), LocalDateTime.of(2026, 5, 13, 11, 50, 49),
                43500, 43500, 36975, "channel", "非置换订单");
        insertDistributionOrder(29881L, 29871L, CHANNEL_ACCOUNT_ID_1, 6525, "pending");

        insertOrderMain(29872L, CHANNEL_ACCOUNT_ID_2, "分销客人B", "13900002002",
                LocalDate.of(2026, 5, 19), LocalDate.of(2026, 5, 20), LocalDateTime.of(2026, 5, 19, 15, 42, 15),
                24105, 24105, 20130, "channel", "置换订单");
        insertDistributionOrder(29882L, 29872L, CHANNEL_ACCOUNT_ID_2, 3975, "settled");
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
                "分销联调门店",
                1,
                "深圳",
                "南山区分销联调路 91 号",
                "0755-2989101",
                1,
                0
        );
    }

    private void insertPoi() {
        jdbcTemplate.update("""
                        INSERT INTO pms_poi (
                            poi_id,
                            camp_id,
                            poi_name,
                            is_availability,
                            sort_no,
                            status,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                ISOLATED_POI_ID,
                ISOLATED_CAMP_ID,
                "分销联调门店",
                1,
                1,
                1,
                0
        );
    }

    private void insertRoomCategory() {
        jdbcTemplate.update("""
                        INSERT INTO room_category (
                            room_category_id,
                            camp_id,
                            poi_id,
                            group_id,
                            name,
                            display_name,
                            room_count,
                            guest_count,
                            weekday_price_cent,
                            weekend_price_cent,
                            holiday_price_cent,
                            earliest_check_in_hour,
                            latest_check_in_hour,
                            latest_check_out_hour,
                            highlight_description,
                            nearby_description,
                            article_description,
                            sort_no,
                            status,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                ROOM_CATEGORY_ID,
                ISOLATED_CAMP_ID,
                ISOLATED_POI_ID,
                21001L,
                "分销联调房型",
                "分销联调房型",
                2,
                2,
                18800L,
                20800L,
                22800L,
                14,
                23,
                12,
                "分销联调",
                "分销联调",
                "distribution flow query test room category",
                1,
                1,
                0
        );
    }

    private void insertChannelAccount(long accountId, long channelId, String channelName) {
        jdbcTemplate.update("""
                        INSERT INTO channel_account (
                            account_id,
                            camp_id,
                            channel_id,
                            channel_name,
                            account_name,
                            out_account_id,
                            status
                        ) VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                accountId,
                ISOLATED_CAMP_ID,
                channelId,
                channelName,
                channelName + "账号",
                "OUT-" + accountId,
                "authorized"
        );
    }

    private void insertOrderMain(
            long orderId,
            Long channelAccountId,
            String guestName,
            String guestMobile,
            LocalDate startDate,
            LocalDate endDate,
            LocalDateTime createdAt,
            long totalPayPriceCent,
            long totalPriceCent,
            long settlementAmountCent,
            String sourceType,
            String remark
    ) {
        jdbcTemplate.update("""
                        INSERT INTO order_main (
                            order_id,
                            camp_id,
                            poi_id,
                            room_category_id,
                            room_id,
                            channel_id,
                            goods_id,
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
                            discount_price_cent,
                            total_pay_price_cent,
                            refund_price_cent,
                            commission_price_cent,
                            payment_fee_cent,
                            platform_service_fee_cent,
                            distribution_commission_cent,
                            settlement_amount_cent,
                            payment_status,
                            payment_type_id,
                            payment_way_id,
                            source_type,
                            remark,
                            created_at,
                            updated_at,
                            created_by,
                            updated_by,
                            is_deleted,
                            version_no
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 0)
                        """,
                orderId,
                ISOLATED_CAMP_ID,
                ISOLATED_POI_ID,
                ROOM_CATEGORY_ID,
                null,
                channelAccountId,
                null,
                "ORDER-" + orderId,
                "OUT-ORDER-" + orderId,
                "daily_room",
                "booked",
                guestName,
                guestMobile,
                Timestamp.valueOf(startDate.atTime(LocalTime.of(14, 0))),
                Timestamp.valueOf(endDate.atTime(LocalTime.of(12, 0))),
                Math.max(1, (int) (endDate.toEpochDay() - startDate.toEpochDay())),
                totalPriceCent,
                0,
                totalPayPriceCent,
                0,
                0,
                0,
                0,
                0,
                settlementAmountCent,
                "paid",
                17101L,
                17202L,
                sourceType,
                remark,
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt.plusMinutes(15)),
                CURRENT_USER_ID,
                CURRENT_USER_ID
        );
    }

    private void insertDistributionOrder(
            long distributionOrderId,
            long sourceOrderId,
            long channelAccountId,
            long commissionPriceCent,
            String settlementStatus
    ) {
        jdbcTemplate.update("""
                        INSERT INTO distribution_order (
                            distribution_order_id,
                            camp_id,
                            source_order_id,
                            channel_id,
                            commission_price_cent,
                            settlement_status
                        ) VALUES (?, ?, ?, ?, ?, ?)
                        """,
                distributionOrderId,
                ISOLATED_CAMP_ID,
                sourceOrderId,
                channelAccountId,
                commissionPriceCent,
                settlementStatus
        );
    }
}
