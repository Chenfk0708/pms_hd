package com.jeez.zp.finance.controller;

import com.jeez.zp.order.ZpServiceOrderApplication;
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

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ZpServiceOrderApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FullMarketingQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;
    private static final long USER_ID = 12001L;
    private static final long POI_ID = 61010L;
    private static final long CALENDAR_ROOM_CATEGORY_ID = 61021L;
    private static final long COUPON_ROOM_CATEGORY_ID = 61022L;
    private static final long CALENDAR_ROOM_ID = 61031L;
    private static final long COUPON_ROOM_ID = 61032L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void promotionPlanProductsPageGet_shouldReturnDistributionProductsWithCommissionRatios() throws Exception {
        seedFullMarketingData();

        mockMvc.perform(post("/promotionPlanProducts/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":1,
                                  "pageSize":10,
                                  "type":"0",
                                  "keyword":"TDD日历"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.list[0].productId").value("61001"))
                .andExpect(jsonPath("$.data.list[0].name").value("TDD日历房分销商品"))
                .andExpect(jsonPath("$.data.list[0].directRatio").value(6))
                .andExpect(jsonPath("$.data.list[0].parentRatio").value(2))
                .andExpect(jsonPath("$.data.list[0].type").value(0))
                .andExpect(jsonPath("$.data.list[0].state").value(1));
    }

    @Test
    @Timeout(60)
    void promotionReports_shouldReturnMetricsAndProductSalesFromDistributionOrders() throws Exception {
        seedFullMarketingData();

        mockMvc.perform(post("/report/promotion/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "startDate":"2036-05-29",
                                  "endDate":"2036-05-30",
                                  "type":null
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.turnover").value(1000.0))
                .andExpect(jsonPath("$.data.commission").value(68.0))
                .andExpect(jsonPath("$.data.orderCount").value(2));

        mockMvc.perform(post("/report/promotion/productSale/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":1,
                                  "pageSize":10,
                                  "startDate":"2036-05-29",
                                  "endDate":"2036-05-30",
                                  "type":"1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].productId").value("61002"))
                .andExpect(jsonPath("$.data.list[0].name").value("TDD预售券分销商品"))
                .andExpect(jsonPath("$.data.list[0].sales").value(1))
                .andExpect(jsonPath("$.data.list[0].turnover").value(320.0))
                .andExpect(jsonPath("$.data.list[0].commission").value(18.0));
    }

    @Test
    @Timeout(60)
    void fullMarketingQueries_shouldFallbackCurrentCampAndRejectForeignCampAccess() throws Exception {
        seedFullMarketingData();

        mockMvc.perform(post("/promotionPlanProducts/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pageNum":1,"pageSize":10,"type":"0"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total", greaterThanOrEqualTo(1)));

        mockMvc.perform(post("/report/promotion/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10002","startDate":"2026-05-01","endDate":"2026-06-01"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301));
    }

    private void seedFullMarketingData() {
        resetFullMarketingData();
        insertFullMarketingRoomData();
        insertGoodsMain(61001L, "calendar_room", "TDD日历房分销商品", 1, 68000L);
        insertGoodsMain(61002L, "coupon", "TDD预售券分销商品", 2, 32000L);
        insertDistributionProduct(62001L, 61001L, CALENDAR_ROOM_CATEGORY_ID, "{\"directRatio\":6,\"parentRatio\":2}");
        insertDistributionProduct(62002L, 61002L, COUPON_ROOM_CATEGORY_ID, "{\"directRatio\":5,\"parentRatio\":1}");
        insertOrder(63001L, 61001L, CALENDAR_ROOM_CATEGORY_ID, CALENDAR_ROOM_ID, 68000L, 17001L, "FM-63001", LocalDateTime.of(2036, 5, 29, 9, 0));
        insertOrder(63002L, 61002L, COUPON_ROOM_CATEGORY_ID, COUPON_ROOM_ID, 32000L, 17002L, "FM-63002", LocalDateTime.of(2036, 5, 29, 10, 0));
        insertDistributionOrder(64001L, 63001L, 17001L, 5000L, LocalDateTime.of(2036, 5, 29, 9, 30));
        insertDistributionOrder(64002L, 63002L, 17002L, 1800L, LocalDateTime.of(2036, 5, 29, 10, 30));
    }

    private void resetFullMarketingData() {
        jdbcTemplate.update("DELETE FROM distribution_order WHERE distribution_order_id BETWEEN 64001 AND 64099");
        jdbcTemplate.update("DELETE FROM order_main WHERE order_id BETWEEN 63001 AND 63099");
        jdbcTemplate.update("DELETE FROM distribution_product WHERE distribution_product_id BETWEEN 62001 AND 62099");
        jdbcTemplate.update("DELETE FROM goods_sku WHERE goods_id BETWEEN 61001 AND 61099");
        jdbcTemplate.update("DELETE FROM goods_main WHERE goods_id BETWEEN 61001 AND 61099");
        jdbcTemplate.update("DELETE FROM room WHERE room_id IN (?, ?)", CALENDAR_ROOM_ID, COUPON_ROOM_ID);
        jdbcTemplate.update("DELETE FROM room_category WHERE room_category_id IN (?, ?)", CALENDAR_ROOM_CATEGORY_ID, COUPON_ROOM_CATEGORY_ID);
        jdbcTemplate.update("DELETE FROM pms_poi WHERE poi_id = ?", POI_ID);
    }

    private void insertFullMarketingRoomData() {
        jdbcTemplate.update("INSERT INTO pms_poi (poi_id,camp_id,poi_name,is_availability,sort_no,status,is_deleted) VALUES (?,?,?,?,?,?,?)",
                POI_ID, CAMP_ID, "Full Marketing Store", 1, 1, 1, 0);
        insertRoomCategory(CALENDAR_ROOM_CATEGORY_ID, "Full Marketing Calendar Room", 1);
        insertRoomCategory(COUPON_ROOM_CATEGORY_ID, "Full Marketing Coupon Room", 2);
        jdbcTemplate.update("INSERT INTO room (room_id,camp_id,poi_id,room_category_id,room_name,room_no,status,sort_no,is_deleted) VALUES (?,?,?,?,?,?,?,?,?)",
                CALENDAR_ROOM_ID, CAMP_ID, POI_ID, CALENDAR_ROOM_CATEGORY_ID, "FM-61031", "FM-61031", 1, 1, 0);
        jdbcTemplate.update("INSERT INTO room (room_id,camp_id,poi_id,room_category_id,room_name,room_no,status,sort_no,is_deleted) VALUES (?,?,?,?,?,?,?,?,?)",
                COUPON_ROOM_ID, CAMP_ID, POI_ID, COUPON_ROOM_CATEGORY_ID, "FM-61032", "FM-61032", 1, 2, 0);
    }

    private void insertRoomCategory(long roomCategoryId, String name, int sortNo) {
        jdbcTemplate.update("INSERT INTO room_category (room_category_id,camp_id,poi_id,name,display_name,room_count,guest_count,weekday_price_cent,weekend_price_cent,holiday_price_cent,earliest_check_in_hour,latest_check_in_hour,latest_check_out_hour,highlight_description,nearby_description,article_description,sort_no,status,is_deleted) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                roomCategoryId,
                CAMP_ID,
                POI_ID,
                name,
                name,
                1,
                2,
                38800L,
                38800L,
                38800L,
                14,
                23,
                12,
                "full marketing",
                "full marketing",
                "full marketing room",
                sortNo,
                1,
                0);
    }

    private void insertGoodsMain(long goodsId, String goodsType, String name, int roomCategoryType, long sellingPriceCent) {
        jdbcTemplate.update("""
                        INSERT INTO goods_main (
                            goods_id, camp_id, goods_type, name, category_id, category_name, room_category_type,
                            selling_price_cent, original_price_cent, settlement_price_cent, stock, stock_mode, shelf_status,
                            effective_start_at, effective_end_at, description, refund_rule, reservation_phone,
                            reservation_note, status, remark, is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                goodsId,
                CAMP_ID,
                goodsType,
                name,
                1L,
                "TDD分销类目",
                roomCategoryType,
                sellingPriceCent,
                sellingPriceCent,
                sellingPriceCent,
                100,
                "manual",
                "on_shelf",
                "2026-05-01 00:00:00",
                "2026-12-31 23:59:59",
                name,
                "按规则退款",
                "13800000000",
                "TDD",
                "published",
                "full-marketing-it",
                0
        );
        jdbcTemplate.update("""
                        INSERT INTO goods_sku (
                            goods_sku_id, camp_id, goods_id, room_category_product_id, sku_name,
                            selling_price_cent, original_price_cent, stock, status, sort_no, is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                goodsId + 1000,
                CAMP_ID,
                goodsId,
                "sku-" + goodsId,
                name + "标准 SKU",
                sellingPriceCent,
                sellingPriceCent,
                100,
                "published",
                1,
                0
        );
    }

    private void insertDistributionProduct(long id, long goodsId, long roomCategoryId, String ruleJson) {
        jdbcTemplate.update("""
                        INSERT INTO distribution_product (
                            distribution_product_id, camp_id, goods_id, room_category_id, commission_rule_json, status
                        ) VALUES (?, ?, ?, ?, CAST(? AS JSON), 1)
                        """,
                id,
                CAMP_ID,
                goodsId,
                roomCategoryId,
                ruleJson
        );
    }

    private void insertOrder(long orderId, long goodsId, long roomCategoryId, long roomId, long totalPayCent, long channelId, String orderNo, LocalDateTime createdAt) {
        jdbcTemplate.update("""
                        INSERT INTO order_main (
                            order_id, camp_id, poi_id, room_category_id, room_id, goods_id, channel_id,
                            order_no, out_order_no, order_type, status, guest_name, start_at, end_at, day_num,
                            total_price_cent, total_pay_price_cent, payment_status, source_type, created_at, is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
                        """,
                orderId,
                CAMP_ID,
                POI_ID,
                roomCategoryId,
                roomId,
                goodsId,
                channelId,
                orderNo,
                "OUT-" + orderNo,
                "house",
                "completed",
                "Full Marketing Guest",
                Timestamp.valueOf(createdAt.plusDays(1)),
                Timestamp.valueOf(createdAt.plusDays(2)),
                1,
                totalPayCent,
                totalPayCent,
                "paid",
                "distribution",
                Timestamp.valueOf(createdAt)
        );
    }

    private void insertDistributionOrder(long distributionOrderId, long sourceOrderId, long channelId, long commissionCent, LocalDateTime createdAt) {
        jdbcTemplate.update("""
                        INSERT INTO distribution_order (
                            distribution_order_id, camp_id, source_order_id, channel_id, commission_price_cent,
                            settlement_status, settled_at, created_at
                        ) VALUES (?, ?, ?, ?, ?, 'settled', ?, ?)
                        """,
                distributionOrderId,
                CAMP_ID,
                sourceOrderId,
                channelId,
                commissionCent,
                Timestamp.valueOf(createdAt.plusDays(1)),
                Timestamp.valueOf(createdAt)
        );
    }
}
