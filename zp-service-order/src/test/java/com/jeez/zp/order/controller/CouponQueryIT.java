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

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CouponQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;
    private static final long COUPON_ID = 39701L;
    private static final long DISABLED_COUPON_ID = 39702L;
    private static final long SEND_CONFIG_ID = 39751L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void couponsPageGet_shouldReturnCouponGoodsWithFrontendFields() throws Exception {
        seedCoupons();

        mockMvc.perform(post("/coupons/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "shelfStatus":1,
                                  "pageNum":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.total").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(20))
                .andExpect(jsonPath("$.data.pages").value(1))
                .andExpect(jsonPath("$.data.hasNextPage").value(false))
                .andExpect(jsonPath("$.data.list.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.list[0].id").value(String.valueOf(COUPON_ID)))
                .andExpect(jsonPath("$.data.list[0].couponId").value(String.valueOf(COUPON_ID)))
                .andExpect(jsonPath("$.data.list[0].name").value("Coupon Query Spring Stay"))
                .andExpect(jsonPath("$.data.list[0].couponName").value("Coupon Query Spring Stay"))
                .andExpect(jsonPath("$.data.list[0].typeName").value("满减券"))
                .andExpect(jsonPath("$.data.list[0].discountText").value("满 500 元减 80 元"))
                .andExpect(jsonPath("$.data.list[0].scopeText").value("标准大床房"))
                .andExpect(jsonPath("$.data.list[0].sendLimit").value(300))
                .andExpect(jsonPath("$.data.list[0].perUserLimit").value(2))
                .andExpect(jsonPath("$.data.list[0].shelfStatus").value(1))
                .andExpect(jsonPath("$.data.list[0].status").value(1));
    }

    @Test
    @Timeout(60)
    void couponSendConfigsPageGet_shouldReturnTaskRowsJoinedWithCouponGoods() throws Exception {
        seedCoupons();

        mockMvc.perform(post("/couponSendConfigs/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value(String.valueOf(SEND_CONFIG_ID)))
                .andExpect(jsonPath("$.data.list[0].couponId").value(String.valueOf(COUPON_ID)))
                .andExpect(jsonPath("$.data.list[0].couponName").value("Coupon Query Spring Stay"))
                .andExpect(jsonPath("$.data.list[0].sendMethod").value("会员标签定向派发"))
                .andExpect(jsonPath("$.data.list[0].sentCount").value(128))
                .andExpect(jsonPath("$.data.list[0].recordText").value("查看记录"));
    }

    @Test
    @Timeout(60)
    void couponQueries_shouldFallbackCurrentCampAndRejectForeignCampAccess() throws Exception {
        seedCoupons();

        mockMvc.perform(post("/coupons/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"",
                                  "pageNum":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(greaterThanOrEqualTo(2)));

        mockMvc.perform(post("/couponSendConfigs/page/get")
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
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.success").value(false));
    }

    private void seedCoupons() {
        resetCoupons();
        OrderTestCatalogFixture.ensureBaseCatalog(jdbcTemplate);
        insertCoupon(COUPON_ID, "Coupon Query Spring Stay", "on_shelf", "published", 50000, 8000, 300, 2);
        insertCoupon(DISABLED_COUPON_ID, "Coupon Query Return Guest", "off_shelf", "published", 30000, 3000, 100, 1);
        insertCouponSku(39711L, COUPON_ID, "Spring Stay SKU", 42000, 50000, 300);
        insertCouponSku(39721L, DISABLED_COUPON_ID, "Return Guest SKU", 27000, 30000, 100);
        insertGoodsRoomCategoryRel(COUPON_ID, 22001L);
        insertGoodsRoomCategoryRel(DISABLED_COUPON_ID, 22002L);
        insertCouponSendConfig();
    }

    private void resetCoupons() {
        jdbcTemplate.update("DELETE FROM coupon_send_config WHERE camp_id = ? AND config_id BETWEEN ? AND ?", CAMP_ID, SEND_CONFIG_ID, SEND_CONFIG_ID + 9);
        jdbcTemplate.update("DELETE FROM goods_room_category_rel WHERE goods_id IN (?, ?)", COUPON_ID, DISABLED_COUPON_ID);
        jdbcTemplate.update("DELETE FROM goods_sku WHERE camp_id = ? AND goods_id IN (?, ?)", CAMP_ID, COUPON_ID, DISABLED_COUPON_ID);
        jdbcTemplate.update("DELETE FROM goods_main WHERE camp_id = ? AND goods_id IN (?, ?)", CAMP_ID, COUPON_ID, DISABLED_COUPON_ID);
    }

    private void insertCoupon(
            long goodsId,
            String name,
            String shelfStatus,
            String status,
            long thresholdCent,
            long discountCent,
            int sendLimit,
            int perUserLimit
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
                "coupon",
                name,
                81L,
                "满减券",
                1,
                discountCent,
                thresholdCent,
                discountCent,
                sendLimit,
                "per_user_limit:" + perUserLimit,
                shelfStatus,
                "2026-05-01 00:00:00",
                "2026-06-01 23:59:59",
                "Coupon Query fixture",
                "领取后7天有效",
                "13800000000",
                "所有人可以领",
                status,
                "integration-test",
                0
        );
    }

    private void insertCouponSku(long skuId, long goodsId, String skuName, long sellingPriceCent, long originalPriceCent, int stock) {
        jdbcTemplate.update("""
                        INSERT INTO goods_sku (
                            goods_sku_id,
                            camp_id,
                            goods_id,
                            room_category_product_id,
                            sku_name,
                            selling_price_cent,
                            original_price_cent,
                            stock,
                            sort_no,
                            status,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                skuId,
                CAMP_ID,
                goodsId,
                String.valueOf(skuId),
                skuName,
                sellingPriceCent,
                originalPriceCent,
                stock,
                1,
                "published",
                0
        );
    }

    private void insertGoodsRoomCategoryRel(long goodsId, long roomCategoryId) {
        jdbcTemplate.update("""
                        INSERT INTO goods_room_category_rel (
                            id,
                            goods_id,
                            room_category_id
                        ) VALUES (?, ?, ?)
                        """,
                goodsId + roomCategoryId,
                goodsId,
                roomCategoryId
        );
    }

    private void insertCouponSendConfig() {
        jdbcTemplate.update("""
                        INSERT INTO coupon_send_config (
                            config_id,
                            camp_id,
                            coupon_id,
                            send_type,
                            status,
                            rule_json,
                            created_at
                        ) VALUES (?, ?, ?, ?, ?, CAST(? AS JSON), ?)
                        """,
                SEND_CONFIG_ID,
                CAMP_ID,
                COUPON_ID,
                "member_tag",
                1,
                "{\"sentCount\":128,\"target\":\"VIP guests\"}",
                "2026-05-18 11:30:00"
        );
    }
}
