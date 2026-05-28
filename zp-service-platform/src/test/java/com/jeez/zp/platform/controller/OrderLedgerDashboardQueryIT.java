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

import java.sql.Date;
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
class OrderLedgerDashboardQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CURRENT_USER_ID = 12001L;
    private static final long ISOLATED_CAMP_ID = 29981L;
    private static final long POI_ID = 29982L;
    private static final long ROOM_CATEGORY_ID = 29983L;
    private static final long ROOM_ID = 29984L;
    private static final long PAYMENT_TYPE_INCOME_ID = 29985L;
    private static final long PAYMENT_TYPE_EXPENSE_ID = 29986L;
    private static final long PAYMENT_WAY_WECHAT_ID = 29987L;
    private static final long PAYMENT_WAY_CASH_ID = 29988L;
    private static final long ORDER_ID = 29989L;
    private static final long INCOME_LEDGER_ID = 29991L;
    private static final long EXPENSE_LEDGER_ID = 29992L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void orderLedgerDashboardGet_shouldReturnAggregatedLedgerDashboard() throws Exception {
        seedOrderLedgerScene();

        mockMvc.perform(post("/orderLedger/dashboard/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"29981",
                                  "pageNum":1,
                                  "pageSize":10,
                                  "beginTime":"2026-05-18",
                                  "endTime":"2026-05-20",
                                  "paymentTypeIds":[],
                                  "paymentWayIds":[],
                                  "roomIds":[],
                                  "poiIds":[],
                                  "keyword":"",
                                  "isIncome":null,
                                  "type":null
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.provider").value("api"))
                .andExpect(jsonPath("$.data.state").value("success"))
                .andExpect(jsonPath("$.data.stores[0].id").value("29982"))
                .andExpect(jsonPath("$.data.stores[0].name").value("账本联调门店"))
                .andExpect(jsonPath("$.data.projectOptions.length()").value(2))
                .andExpect(jsonPath("$.data.projectOptions[0].value").value("29985"))
                .andExpect(jsonPath("$.data.paymentWayOptions.length()").value(2))
                .andExpect(jsonPath("$.data.roomOptions[0].roomCategoryId").value("29983"))
                .andExpect(jsonPath("$.data.roomOptions[0].rooms[0].roomId").value("29984"))
                .andExpect(jsonPath("$.data.summary.totalIncome").value(closeTo(320.00, 0.001)))
                .andExpect(jsonPath("$.data.summary.totalExpense").value(closeTo(45.00, 0.001)))
                .andExpect(jsonPath("$.data.summary.netIncome").value(closeTo(275.00, 0.001)))
                .andExpect(jsonPath("$.data.pagination.page").value(1))
                .andExpect(jsonPath("$.data.pagination.pageSize").value(10))
                .andExpect(jsonPath("$.data.pagination.total").value(2))
                .andExpect(jsonPath("$.data.records.length()").value(2))
                .andExpect(jsonPath("$.data.records[0].id").value("29992"))
                .andExpect(jsonPath("$.data.records[0].typeLabel").value("支出"))
                .andExpect(jsonPath("$.data.records[0].sourceLabel").value("记一笔"))
                .andExpect(jsonPath("$.data.records[0].projectLabel").value("保洁费联调"))
                .andExpect(jsonPath("$.data.records[0].amount").value(closeTo(45.00, 0.001)))
                .andExpect(jsonPath("$.data.records[0].paymentWayLabel").value("现金联调"))
                .andExpect(jsonPath("$.data.records[0].createdAt").value("2026-05-20 10:30:00"))
                .andExpect(jsonPath("$.data.records[0].roomLabel").value("账本联调房型-账本-101"))
                .andExpect(jsonPath("$.data.records[0].detail.paymentRecords[0].amount").value(closeTo(45.00, 0.001)))
                .andExpect(jsonPath("$.data.records[1].id").value("29991"))
                .andExpect(jsonPath("$.data.records[1].typeLabel").value("收入"))
                .andExpect(jsonPath("$.data.records[1].sourceLabel").value("住宿订单"))
                .andExpect(jsonPath("$.data.records[1].orderId").value("29989"))
                .andExpect(jsonPath("$.data.records[1].paymentNo").value("LEDGER-29991"));
    }

    @Test
    @Timeout(60)
    void orderLedgerDashboardGet_shouldApplyIncomeAndKeywordFilters() throws Exception {
        seedOrderLedgerScene();

        mockMvc.perform(post("/orderLedger/dashboard/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"29981",
                                  "pageNum":1,
                                  "pageSize":10,
                                  "beginTime":"2026-05-18",
                                  "endTime":"2026-05-20",
                                  "paymentTypeIds":["29985"],
                                  "paymentWayIds":["29987"],
                                  "roomIds":["29984"],
                                  "poiIds":["29982"],
                                  "keyword":"账本收入",
                                  "isIncome":1,
                                  "type":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.summary.totalIncome").value(closeTo(320.00, 0.001)))
                .andExpect(jsonPath("$.data.summary.totalExpense").value(closeTo(0.00, 0.001)))
                .andExpect(jsonPath("$.data.records.length()").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value("29991"))
                .andExpect(jsonPath("$.data.records[0].typeLabel").value("收入"))
                .andExpect(jsonPath("$.data.records[0].projectLabel").value("房费联调"));
    }

    private void seedOrderLedgerScene() {
        insertCamp();
        rebindCurrentUserCamp();
        insertPoi();
        insertRoomCategory();
        insertRoom();
        insertPaymentTypeGroup();
        insertPaymentType(PAYMENT_TYPE_INCOME_ID, "房费联调", 1, 1);
        insertPaymentType(PAYMENT_TYPE_EXPENSE_ID, "保洁费联调", 0, 2);
        insertPaymentWay(PAYMENT_WAY_WECHAT_ID, "微信联调", "wechat", 1);
        insertPaymentWay(PAYMENT_WAY_CASH_ID, "现金联调", "cash", 2);
        insertOrderMain();
        insertLedgerEntry(
                INCOME_LEDGER_ID,
                "income",
                PAYMENT_TYPE_INCOME_ID,
                PAYMENT_WAY_WECHAT_ID,
                ORDER_ID,
                "order",
                32000L,
                LocalDateTime.of(2026, 5, 19, 9, 15, 0),
                "账本收入联调",
                "前台小路"
        );
        insertLedgerEntry(
                EXPENSE_LEDGER_ID,
                "expense",
                PAYMENT_TYPE_EXPENSE_ID,
                PAYMENT_WAY_CASH_ID,
                ORDER_ID,
                "manual",
                4500L,
                LocalDateTime.of(2026, 5, 20, 10, 30, 0),
                "账本支出联调",
                "店长小账"
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
                "账本联调租户",
                1,
                "深圳",
                "南山区账本联调路 99 号",
                "0755-2998101",
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
                POI_ID,
                ISOLATED_CAMP_ID,
                "账本联调门店",
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
                POI_ID,
                "账本联调房型",
                "账本联调房型",
                1,
                2,
                32000L,
                35000L,
                38000L,
                14,
                23,
                12,
                "账本房型亮点",
                "账本房型周边",
                "order ledger query room category",
                1,
                1,
                0
        );
    }

    private void insertRoom() {
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
                ROOM_ID,
                ISOLATED_CAMP_ID,
                POI_ID,
                ROOM_CATEGORY_ID,
                "账本-101",
                "normal",
                "overnight",
                "clean",
                1,
                1,
                0
        );
    }

    private void insertPaymentTypeGroup() {
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
                29980L,
                ISOLATED_CAMP_ID,
                1,
                "账本项目分组",
                3,
                1,
                1,
                1,
                0
        );
    }

    private void insertPaymentType(long paymentTypeId, String name, int isIncome, int sortNo) {
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
                ISOLATED_CAMP_ID,
                29980L,
                name,
                1,
                "账本项目分组",
                3,
                isIncome,
                0,
                0,
                1,
                sortNo,
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
                "offline",
                sortNo,
                1,
                0
        );
    }

    private void insertOrderMain() {
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
                ORDER_ID,
                ISOLATED_CAMP_ID,
                POI_ID,
                ROOM_CATEGORY_ID,
                ROOM_ID,
                null,
                null,
                "ORDER-" + ORDER_ID,
                "OUT-LEDGER-" + ORDER_ID,
                "daily_room",
                "completed",
                "账本客人",
                "13900009981",
                Timestamp.valueOf(LocalDate.of(2026, 5, 19).atTime(LocalTime.of(14, 0))),
                Timestamp.valueOf(LocalDate.of(2026, 5, 20).atTime(LocalTime.of(12, 0))),
                1,
                32000L,
                0L,
                32000L,
                0L,
                0L,
                0L,
                0L,
                0L,
                32000L,
                "paid",
                PAYMENT_TYPE_INCOME_ID,
                PAYMENT_WAY_WECHAT_ID,
                "frontdesk",
                "账本订单联调",
                Timestamp.valueOf(LocalDateTime.of(2026, 5, 19, 8, 0, 0)),
                Timestamp.valueOf(LocalDateTime.of(2026, 5, 19, 9, 20, 0)),
                CURRENT_USER_ID,
                CURRENT_USER_ID
        );
    }

    private void insertLedgerEntry(
            long ledgerEntryId,
            String entryType,
            long paymentTypeId,
            long paymentWayId,
            Long orderId,
            String sourceType,
            long amountCent,
            LocalDateTime occurredAt,
            String remark,
            String operatorName
    ) {
        jdbcTemplate.update("""
                        INSERT INTO ledger_entry (
                            ledger_entry_id,
                            camp_id,
                            poi_id,
                            entry_type,
                            payment_type_id,
                            payment_way_id,
                            order_id,
                            source_type,
                            source_id,
                            amount_cent,
                            occurred_at,
                            operator_id,
                            operator_name,
                            biz_date,
                            remark,
                            created_at,
                            updated_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                ledgerEntryId,
                ISOLATED_CAMP_ID,
                POI_ID,
                entryType,
                paymentTypeId,
                paymentWayId,
                orderId,
                sourceType,
                orderId,
                amountCent,
                Timestamp.valueOf(occurredAt),
                CURRENT_USER_ID,
                operatorName,
                Date.valueOf(occurredAt.toLocalDate()),
                remark,
                Timestamp.valueOf(occurredAt),
                Timestamp.valueOf(occurredAt)
        );
    }
}
