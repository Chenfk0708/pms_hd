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
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StatementOrderQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CURRENT_USER_ID = 12001L;
    private static final long ISOLATED_CAMP_ID = 29901L;
    private static final long PRIMARY_POI_ID = 29911L;
    private static final long SECONDARY_POI_ID = 29912L;
    private static final long ROOM_CATEGORY_ID = 29921L;
    private static final long GOODS_ID = 29931L;
    private static final long PAYMENT_WAY_WECHAT_ID = 29941L;
    private static final long PAYMENT_WAY_PLATFORM_ID = 29942L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void reportStorerStatementGet_shouldReturnMallOrdersForCurrentPoi() throws Exception {
        seedStatementScene();

        mockMvc.perform(post("/report/storer/statement/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"29901",
                                  "poiIds":["29911"],
                                  "bookingStartDate":"2026-05-01",
                                  "bookingEndDate":"2026-05-31",
                                  "pageNum":1,
                                  "pageSize":20,
                                  "current":1,
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
                .andExpect(jsonPath("$.data.list[0].orderId").value("OUT-MALL-29951"))
                .andExpect(jsonPath("$.data.list[0].customerInfo").value("小程序客人A / 13900003001"))
                .andExpect(jsonPath("$.data.list[0].productType").value("预售券"))
                .andExpect(jsonPath("$.data.list[0].productName").value("联调预售券"))
                .andExpect(jsonPath("$.data.list[0].bookingTime").value("2026-05-05 10:15:00"))
                .andExpect(jsonPath("$.data.list[0].channelName").value("品牌小程序"))
                .andExpect(jsonPath("$.data.list[0].payableAmount").value(closeTo(199.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].paidAmount").value(closeTo(189.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].discountAmount").value(closeTo(10.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].refundAmount").value(closeTo(0.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].paymentFee").value(closeTo(1.50, 0.001)))
                .andExpect(jsonPath("$.data.list[0].platformServiceFee").value(closeTo(5.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].distributorCommission").value(closeTo(7.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].paymentWayName").value("微信支付"))
                .andExpect(jsonPath("$.data.list[0].settlementAmount").value(closeTo(175.50, 0.001)));
    }

    @Test
    @Timeout(60)
    void reportStorerStatementGet_shouldFallbackInvalidCampIdAndRejectForeignCamp() throws Exception {
        seedStatementScene();

        mockMvc.perform(post("/report/storer/statement/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"invalid-camp-id",
                                  "bookingStartDate":"2026-05-01",
                                  "bookingEndDate":"2026-05-31",
                                  "pageNum":1,
                                  "pageSize":20,
                                  "current":1,
                                  "breakTemp":false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].orderId").value("OUT-MALL-29951"))
                .andExpect(jsonPath("$.data.list[1].orderId").value("ORDER-29952"))
                .andExpect(jsonPath("$.data.list[1].productType").value("钟点房"))
                .andExpect(jsonPath("$.data.list[1].productName").value("联调房型"))
                .andExpect(jsonPath("$.data.list[1].paymentWayName").value("平台代收"));

        mockMvc.perform(post("/report/storer/statement/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "bookingStartDate":"2026-05-01",
                                  "bookingEndDate":"2026-05-31",
                                  "pageNum":1,
                                  "pageSize":20,
                                  "current":1,
                                  "breakTemp":false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Timeout(60)
    void reportStorerStatementGet_shouldReturnExportUrlWhenExportRequested() throws Exception {
        seedStatementScene();

        mockMvc.perform(post("/report/storer/statement/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"29901",
                                  "poiIds":["29911"],
                                  "bookingStartDate":"2026-05-01",
                                  "bookingEndDate":"2026-05-31",
                                  "pageNum":1,
                                  "pageSize":9999,
                                  "current":1,
                                  "breakTemp":false,
                                  "exportExcelMenuId":"1732967098146951178"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", containsString("/downloads/report-storer-statement/29901/")))
                .andExpect(jsonPath("$.data", containsString(".xlsx")));
    }

    private void seedStatementScene() {
        insertCamp();
        rebindCurrentUserCamp();
        insertPoi(PRIMARY_POI_ID, "联调门店一店");
        insertPoi(SECONDARY_POI_ID, "联调门店二店");
        insertRoomCategory();
        insertGoods();
        insertPaymentWay(PAYMENT_WAY_WECHAT_ID, "微信支付", "wechat", 1);
        insertPaymentWay(PAYMENT_WAY_PLATFORM_ID, "平台代收", "platform_collect", 2);

        insertOrderMain(29951L, PRIMARY_POI_ID, ROOM_CATEGORY_ID, GOODS_ID, "coupon_order", "completed", "mall",
                "小程序客人A", "13900003001", LocalDateTime.of(2026, 5, 5, 10, 15, 0),
                19900L, 1000L, 18900L, 0L, 150L, 500L, 700L, 17550L, PAYMENT_WAY_WECHAT_ID, "mall primary order");
        insertOrderMain(29952L, SECONDARY_POI_ID, ROOM_CATEGORY_ID, null, "hourly_room", "completed", "mall",
                "小程序客人B", "13900003002", LocalDateTime.of(2026, 5, 8, 16, 30, 0),
                15900L, 0L, 15900L, 0L, 120L, 300L, 0L, 15480L, PAYMENT_WAY_PLATFORM_ID, "mall secondary order");
        insertOrderMain(29953L, PRIMARY_POI_ID, ROOM_CATEGORY_ID, null, "daily_room", "completed", "frontdesk",
                "前台客人C", "13900003003", LocalDateTime.of(2026, 5, 9, 9, 0, 0),
                26800L, 0L, 26800L, 0L, 0L, 0L, 0L, 26800L, PAYMENT_WAY_WECHAT_ID, "frontdesk order should be excluded");
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
                "小程序报表联调门店",
                1,
                "深圳",
                "南山区小程序联调路 99 号",
                "0755-2990101",
                1,
                0
        );
    }

    private void insertPoi(long poiId, String poiName) {
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
                poiId,
                ISOLATED_CAMP_ID,
                poiName,
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
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                ROOM_CATEGORY_ID,
                ISOLATED_CAMP_ID,
                PRIMARY_POI_ID,
                "联调房型",
                "联调房型",
                2,
                2,
                18800L,
                20800L,
                22800L,
                14,
                23,
                12,
                "小程序报表房型亮点",
                "小程序报表房型周边",
                "statement order query test room category",
                1,
                1,
                0
        );
    }

    private void insertGoods() {
        jdbcTemplate.update("""
                        INSERT INTO goods_main (
                            goods_id,
                            camp_id,
                            goods_type,
                            name,
                            category_name,
                            selling_price_cent,
                            original_price_cent,
                            settlement_price_cent,
                            stock,
                            stock_mode,
                            shelf_status,
                            status,
                            remark,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                GOODS_ID,
                ISOLATED_CAMP_ID,
                "coupon",
                "联调预售券",
                "小程序商品",
                19900L,
                20900L,
                18000L,
                99,
                "manual",
                "on_shelf",
                "published",
                "statement order query goods",
                0
        );
    }

    private void insertPaymentWay(long paymentWayId, String name, String code, int sortNo) {
        jdbcTemplate.update("""
                        INSERT INTO payment_way (
                            payment_way_id,
                            camp_id,
                            payment_way_name,
                            payment_way_code,
                            way_type,
                            sort_no,
                            status,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                paymentWayId,
                ISOLATED_CAMP_ID,
                name,
                code,
                "online",
                sortNo,
                1,
                0
        );
    }

    private void insertOrderMain(
            long orderId,
            long poiId,
            Long roomCategoryId,
            Long goodsId,
            String orderType,
            String status,
            String sourceType,
            String guestName,
            String guestMobile,
            LocalDateTime createdAt,
            long totalPriceCent,
            long discountPriceCent,
            long totalPayPriceCent,
            long refundPriceCent,
            long paymentFeeCent,
            long platformServiceFeeCent,
            long distributorCommissionCent,
            long settlementAmountCent,
            long paymentWayId,
            String remark
    ) {
        LocalDate startDate = createdAt.toLocalDate();
        LocalDate endDate = "hourly_room".equals(orderType) ? startDate : startDate.plusDays(1);
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
                poiId,
                roomCategoryId,
                null,
                null,
                goodsId,
                "ORDER-" + orderId,
                orderId == 29951L ? "OUT-MALL-" + orderId : null,
                orderType,
                status,
                guestName,
                guestMobile,
                Timestamp.valueOf(startDate.atTime(LocalTime.of(14, 0))),
                Timestamp.valueOf(endDate.atTime(LocalTime.of(12, 0))),
                Math.max(1, (int) (endDate.toEpochDay() - startDate.toEpochDay())),
                totalPriceCent,
                discountPriceCent,
                totalPayPriceCent,
                refundPriceCent,
                distributorCommissionCent,
                paymentFeeCent,
                platformServiceFeeCent,
                distributorCommissionCent,
                settlementAmountCent,
                "paid",
                17101L,
                paymentWayId,
                sourceType,
                remark,
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt.plusMinutes(10)),
                CURRENT_USER_ID,
                CURRENT_USER_ID
        );
    }
}
