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
class SalesReportQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CURRENT_USER_ID = 12001L;
    private static final long ISOLATED_CAMP_ID = 19701L;
    private static final long ISOLATED_POI_ID = 19721L;
    private static final long ROOM_CATEGORY_ID = 19731L;
    private static final long ROOM_ID_1 = 19741L;
    private static final long ROOM_ID_2 = 19742L;
    private static final long MEITUAN_ACCOUNT_ID = 19771L;
    private static final long CTRIP_ACCOUNT_ID = 19772L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void reportOpenRoomGet_shouldAggregateSalesByDay() throws Exception {
        seedSalesReportScene();

        mockMvc.perform(post("/report/open/room/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"19701",
                                  "startDate":"2026-05-10",
                                  "endDate":"2026-05-11",
                                  "queryType":1,
                                  "pageNum":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(3))
                .andExpect(jsonPath("$.data.list[0].date").value("合计"))
                .andExpect(jsonPath("$.data.list[0].roomCount").value(4))
                .andExpect(jsonPath("$.data.list[0].canSaleRoomCount").value(4))
                .andExpect(jsonPath("$.data.list[0].openRoomCount").value(3))
                .andExpect(jsonPath("$.data.list[0].allDayOpenRoomCount").value(3))
                .andExpect(jsonPath("$.data.list[0].hourOpenRoomCount").value(0))
                .andExpect(jsonPath("$.data.list[0].occ").value("75.00%"))
                .andExpect(jsonPath("$.data.list[0].adr").value(closeTo(166.67, 0.001)))
                .andExpect(jsonPath("$.data.list[0].adrAfterCommission").value(closeTo(150.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].revPar").value(closeTo(125.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].revParAfterCommission").value(closeTo(112.50, 0.001)))
                .andExpect(jsonPath("$.data.list[0].roomFeeIncludingCommission").value(closeTo(500.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].commission").value(closeTo(50.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].roomFeeMinusCommission").value(closeTo(450.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].orderCount").value(3))
                .andExpect(jsonPath("$.data.list[1].date").value("2026-05-10"))
                .andExpect(jsonPath("$.data.list[1].roomCount").value(2))
                .andExpect(jsonPath("$.data.list[1].openRoomCount").value(1))
                .andExpect(jsonPath("$.data.list[1].occ").value("50.00%"))
                .andExpect(jsonPath("$.data.list[1].roomFeeIncludingCommission").value(closeTo(200.00, 0.001)))
                .andExpect(jsonPath("$.data.list[2].date").value("2026-05-11"))
                .andExpect(jsonPath("$.data.list[2].roomCount").value(2))
                .andExpect(jsonPath("$.data.list[2].openRoomCount").value(2))
                .andExpect(jsonPath("$.data.list[2].occ").value("100.00%"))
                .andExpect(jsonPath("$.data.list[2].roomFeeIncludingCommission").value(closeTo(300.00, 0.001)));
    }

    @Test
    @Timeout(60)
    void reportOpenRoomGet_shouldAggregateSalesByChannel() throws Exception {
        seedSalesReportScene();

        mockMvc.perform(post("/report/open/room/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"19701",
                                  "startDate":"2026-05-10",
                                  "endDate":"2026-05-11",
                                  "queryType":4,
                                  "pageNum":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.list[0].channelName").value("合计"))
                .andExpect(jsonPath("$.data.list[0].openRoomCount").value(3))
                .andExpect(jsonPath("$.data.list[1].channelName").value("Meituan"))
                .andExpect(jsonPath("$.data.list[1].openRoomCount").value(2))
                .andExpect(jsonPath("$.data.list[1].roomFeeIncludingCommission").value(closeTo(400.00, 0.001)))
                .andExpect(jsonPath("$.data.list[1].saleRoomRate").value("66.67%"))
                .andExpect(jsonPath("$.data.list[1].orderRate").value("66.67%"))
                .andExpect(jsonPath("$.data.list[2].channelName").value("Ctrip"))
                .andExpect(jsonPath("$.data.list[2].openRoomCount").value(1))
                .andExpect(jsonPath("$.data.list[2].roomFeeIncludingCommission").value(closeTo(100.00, 0.001)))
                .andExpect(jsonPath("$.data.list[2].saleRoomRate").value("33.33%"))
                .andExpect(jsonPath("$.data.list[2].orderRate").value("33.33%"));
    }

    private void seedSalesReportScene() {
        insertCamp();
        rebindCurrentUserCamp();
        insertPoi();
        insertRoomCategory();
        insertRoom(ROOM_ID_1, "SR-101", 1);
        insertRoom(ROOM_ID_2, "SR-102", 2);
        insertChannelAccount(MEITUAN_ACCOUNT_ID, 1L, "Meituan");
        insertChannelAccount(CTRIP_ACCOUNT_ID, 2L, "Ctrip");
        insertOrderMain(
                19781L,
                MEITUAN_ACCOUNT_ID,
                ROOM_ID_1,
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 12),
                40000L,
                4000L,
                36000L
        );
        insertOrderMain(
                19782L,
                CTRIP_ACCOUNT_ID,
                ROOM_ID_2,
                LocalDate.of(2026, 5, 11),
                LocalDate.of(2026, 5, 12),
                10000L,
                1000L,
                9000L
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
                "Sales Report Camp",
                1,
                "Shenzhen",
                "Sales Road 1",
                "0755-1970101",
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
                "Sales Report Store",
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
                ISOLATED_POI_ID,
                "Sales Room Type",
                "Sales Room Type",
                2,
                2,
                26800L,
                28800L,
                30800L,
                14,
                23,
                12,
                "Sales room type highlight",
                "Sales room type nearby",
                "Sales room type article",
                1,
                1,
                0
        );
    }

    private void insertRoom(long roomId, String roomName, int sortNo) {
        jdbcTemplate.update("""
                        INSERT INTO room (
                            room_id,
                            camp_id,
                            poi_id,
                            room_category_id,
                            room_name,
                            lock_status,
                            sale_type,
                            clean_status,
                            status,
                            sort_no,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                roomId,
                ISOLATED_CAMP_ID,
                ISOLATED_POI_ID,
                ROOM_CATEGORY_ID,
                roomName,
                "normal",
                "overnight",
                "clean",
                1,
                sortNo,
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
                channelName + " Account",
                "OUT-" + accountId,
                "authorized"
        );
    }

    private void insertOrderMain(
            long orderId,
            long channelAccountId,
            long roomId,
            LocalDate startDate,
            LocalDate endDate,
            long totalPayPriceCent,
            long commissionPriceCent,
            long settlementAmountCent
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
                roomId,
                channelAccountId,
                null,
                "SR-" + orderId,
                "OUT-SR-" + orderId,
                "daily_room",
                "checked_in",
                "Sales Guest " + orderId,
                "1391970" + orderId,
                Timestamp.valueOf(startDate.atTime(LocalTime.of(14, 0))),
                Timestamp.valueOf(endDate.atTime(LocalTime.of(12, 0))),
                Math.max(1, (int) (endDate.toEpochDay() - startDate.toEpochDay())),
                totalPayPriceCent,
                0L,
                totalPayPriceCent,
                0L,
                commissionPriceCent,
                0L,
                0L,
                commissionPriceCent,
                settlementAmountCent,
                "paid",
                17101L,
                17202L,
                "channel",
                "sales report test",
                Timestamp.valueOf(LocalDateTime.of(2026, 5, 9, 10, 0).plusMinutes(orderId - 19780L)),
                Timestamp.valueOf(LocalDateTime.of(2026, 5, 9, 10, 30).plusMinutes(orderId - 19780L)),
                CURRENT_USER_ID,
                CURRENT_USER_ID
        );
    }
}
