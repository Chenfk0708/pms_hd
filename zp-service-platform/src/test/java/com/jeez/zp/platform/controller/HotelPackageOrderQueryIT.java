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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class HotelPackageOrderQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void ordersPageGet_shouldReturnHotelPackageRowsWithCurrentFrontendFields() throws Exception {
        seedHotelPackageOrders();

        mockMvc.perform(post("/orders/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":"1",
                                  "pageSize":"20",
                                  "roomCategoryTypes":["4"],
                                  "orderStates":[],
                                  "orderChannelIds":[],
                                  "paymentWayIds":[],
                                  "refundDisplayState":"",
                                  "keyword":""
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.pages").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(3))
                .andExpect(jsonPath("$.data.list[0].orderId").value("59001"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryName").value("总统套间双晚套餐"))
                .andExpect(jsonPath("$.data.list[0].count").value(1))
                .andExpect(jsonPath("$.data.list[0].unitPrice").value(129900))
                .andExpect(jsonPath("$.data.list[0].schedulePriceDiff").value(0))
                .andExpect(jsonPath("$.data.list[0].paidAmount").value(129900))
                .andExpect(jsonPath("$.data.list[0].contactPhone").value("13800001234"))
                .andExpect(jsonPath("$.data.list[0].orderStateName").value("已支付"))
                .andExpect(jsonPath("$.data.list[0].refundDisplayStateName").value("无售后"))
                .andExpect(jsonPath("$.data.list[0].orderChannelName").value("微信商城"))
                .andExpect(jsonPath("$.data.list[0].bookedAt").value("2026-05-18 10:16"));
    }

    @Test
    @Timeout(60)
    void ordersPageGet_shouldSupportHotelPackageFilters() throws Exception {
        seedHotelPackageOrders();

        long bookedStartDate = Timestamp.valueOf(LocalDateTime.of(2026, 5, 17, 0, 0)).getTime();
        long bookedEndDate = Timestamp.valueOf(LocalDateTime.of(2026, 5, 18, 0, 0)).getTime();

        mockMvc.perform(post("/orders/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":"1",
                                  "pageSize":"20",
                                  "roomCategoryTypes":["4"],
                                  "orderStates":["finished"],
                                  "orderChannelIds":["wechat"],
                                  "paymentWayIds":[],
                                  "refundDisplayState":"none",
                                  "bookedStartDate":%d,
                                  "bookedEndDate":%d,
                                  "keyword":"周末"
                                }
                                """.formatted(bookedStartDate, bookedEndDate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].orderId").value("59002"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryName").value("顶层套房周末套餐"))
                .andExpect(jsonPath("$.data.list[0].count").value(2))
                .andExpect(jsonPath("$.data.list[0].schedulePriceDiff").value(20000))
                .andExpect(jsonPath("$.data.list[0].orderStateName").value("已完成"))
                .andExpect(jsonPath("$.data.list[0].refundDisplayStateName").value("无售后"))
                .andExpect(jsonPath("$.data.list[0].orderChannelName").value("微信商城"));
    }

    @Test
    @Timeout(60)
    void hotelPackageOrdersPageGet_shouldFallbackCurrentCampAndRejectForeignCampAccess() throws Exception {
        seedHotelPackageOrders();

        mockMvc.perform(post("/orders/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"",
                                  "pageNum":"1",
                                  "pageSize":"20",
                                  "roomCategoryTypes":["4"],
                                  "orderStates":[],
                                  "orderChannelIds":[],
                                  "paymentWayIds":[]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.list.length()").value(3));

        mockMvc.perform(post("/orders/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "pageNum":"1",
                                  "pageSize":"20",
                                  "roomCategoryTypes":["4"],
                                  "orderStates":[],
                                  "orderChannelIds":[],
                                  "paymentWayIds":[]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301));
    }

    private void seedHotelPackageOrders() {
        resetHotelPackageOrders();

        insertChannelAccount(29451L, 31L, "微信商城");
        insertChannelAccount(29452L, 32L, "线下导入");

        insertGoodsMain(58501L, "总统套间双晚套餐", 4, 129900L, 159900L);
        insertGoodsMain(58502L, "顶层套房周末套餐", 4, 89900L, 99900L);
        insertGoodsMain(58503L, "电竞麻将房三小时套餐", 4, 35900L, 39900L);

        insertOrderMain(59001L, 58501L, 29451L, "wechat", "paid", "paid", "Alice Hotel", "13800001234",
                LocalDateTime.of(2026, 5, 18, 10, 16), 1, 129900L, 129900L, 0L);
        insertOrderMain(59002L, 58502L, 29451L, "wechat", "completed", "paid", "Bob Weekend", "13800004567",
                LocalDateTime.of(2026, 5, 17, 21, 42), 2, 199800L, 199800L, 0L);
        insertOrderMain(59003L, 58503L, 29452L, "offline", "cancelled", "refunded", "Carol Refund", "13800007890",
                LocalDateTime.of(2026, 5, 16, 15, 20), 1, 32900L, 32900L, 32900L);
    }

    private void resetHotelPackageOrders() {
        jdbcTemplate.update("DELETE FROM order_payment_record WHERE camp_id = ?", CAMP_ID);
        jdbcTemplate.update("""
                DELETE le
                FROM ledger_entry le
                JOIN order_main om ON om.order_id = le.order_id
                WHERE om.camp_id = ?
                """, CAMP_ID);
        jdbcTemplate.update("""
                DELETE og
                FROM order_guest og
                JOIN order_main om ON om.order_id = og.order_id
                WHERE om.camp_id = ?
                """, CAMP_ID);
        jdbcTemplate.update("DELETE FROM distribution_order WHERE camp_id = ?", CAMP_ID);
        jdbcTemplate.update("DELETE FROM order_main WHERE camp_id = ?", CAMP_ID);
        jdbcTemplate.update("DELETE FROM goods_main WHERE goods_id IN (?, ?, ?)", 58501L, 58502L, 58503L);
        jdbcTemplate.update("DELETE FROM channel_account WHERE account_id IN (?, ?)", 29451L, 29452L);
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
                        ) VALUES (?, ?, ?, ?, ?, ?, 'authorized')
                        """,
                accountId,
                CAMP_ID,
                channelId,
                channelName,
                channelName + "账号",
                "OUT-" + accountId
        );
    }

    private void insertGoodsMain(
            long goodsId,
            String name,
            int roomCategoryType,
            long sellingPriceCent,
            long originalPriceCent
    ) {
        jdbcTemplate.update("""
                        INSERT INTO goods_main (
                            goods_id,
                            camp_id,
                            goods_type,
                            name,
                            category_id,
                            category_name,
                            room_category_type,
                            selling_price_cent,
                            original_price_cent,
                            settlement_price_cent,
                            stock,
                            stock_mode,
                            shelf_status,
                            effective_start_at,
                            effective_end_at,
                            description,
                            refund_rule,
                            reservation_phone,
                            reservation_note,
                            status,
                            remark,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                goodsId,
                CAMP_ID,
                "package",
                name,
                1L,
                "酒店套餐",
                roomCategoryType,
                sellingPriceCent,
                originalPriceCent,
                sellingPriceCent,
                100,
                "manual",
                "on_shelf",
                "2026-05-01 00:00:00",
                "2027-05-01 00:00:00",
                name + "描述",
                "购买后按门店规则退款",
                "13800009999",
                "请联系门店确认预约时间",
                "published",
                "integration-test",
                0
        );
    }

    private void insertOrderMain(
            long orderId,
            long goodsId,
            long channelAccountId,
            String sourceType,
            String status,
            String paymentStatus,
            String guestName,
            String guestMobile,
            LocalDateTime createdAt,
            int count,
            long totalPriceCent,
            long totalPayPriceCent,
            long refundPriceCent
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
                CAMP_ID,
                11001L,
                22001L,
                null,
                channelAccountId,
                goodsId,
                "ORDER-" + orderId,
                "OUT-" + orderId,
                "goods_order",
                status,
                guestName,
                guestMobile,
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt.plusHours(2)),
                count,
                totalPriceCent,
                0L,
                totalPayPriceCent,
                refundPriceCent,
                0L,
                0L,
                0L,
                0L,
                totalPayPriceCent - refundPriceCent,
                paymentStatus,
                17101L,
                17202L,
                sourceType,
                "hotel-package-order",
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt.plusMinutes(10)),
                12001L,
                12001L
        );
    }
}
