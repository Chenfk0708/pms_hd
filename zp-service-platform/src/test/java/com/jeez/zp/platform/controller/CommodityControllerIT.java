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
class CommodityControllerIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CATALOG_CAMP_ID = 64L;
    private static final long BUY_CAMP_ID = 10001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void commodityGet_shouldReturnCommodityDetailForAccessibleCamp() throws Exception {
        insertCommodityFixture();

        mockMvc.perform(post("/youzan/commodity/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "commodityId":"99601"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.commodityId").value("99601"))
                .andExpect(jsonPath("$.data.commodityName").value("门卡管理系统"))
                .andExpect(jsonPath("$.data.description").value("门卡制卡、发卡与房卡管理统一采购入口"))
                .andExpect(jsonPath("$.data.purchaseTermLabel").value("1年"))
                .andExpect(jsonPath("$.data.sellingPriceCent").value(80000))
                .andExpect(jsonPath("$.data.originalPriceCent").value(120000))
                .andExpect(jsonPath("$.data.roomCategoryIds.length()").value(2))
                .andExpect(jsonPath("$.data.roomCategoryIds[0]").value("22001"))
                .andExpect(jsonPath("$.data.roomCategoryIds[1]").value("22002"));
    }

    @Test
    @Timeout(60)
    void commodityGet_shouldRejectForeignCampAccess() throws Exception {
        mockMvc.perform(post("/youzan/commodity/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "commodityId":"99601"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @Timeout(60)
    void commodityGet_shouldReturnNotFoundWhenCommodityDoesNotExist() throws Exception {
        mockMvc.perform(post("/youzan/commodity/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "commodityId":"99999"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40404))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    private void insertCommodityFixture() {
        insertGoodsMain(
                99601L,
                CATALOG_CAMP_ID,
                "6",
                "门卡管理系统",
                1,
                80000L,
                120000L,
                76000L,
                100,
                "manual",
                "on_shelf",
                "published",
                "门卡制卡、发卡与房卡管理统一采购入口"
        );
        insertGoodsSku(99611L, CATALOG_CAMP_ID, 99601L, "1年", 80000L, 120000L, 100, 1);
        insertGoodsSku(99612L, CATALOG_CAMP_ID, 99601L, "2年", 150000L, 200000L, 20, 2);
        insertGoodsRoomCategoryRel(99601L, 22001L);
        insertGoodsRoomCategoryRel(99601L, 22002L);
    }

    private void insertGoodsMain(
            long goodsId,
            long campId,
            String goodsType,
            String name,
            int roomCategoryType,
            long sellingPriceCent,
            long originalPriceCent,
            long settlementPriceCent,
            int stock,
            String stockMode,
            String shelfStatus,
            String status,
            String description
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
                campId,
                goodsType,
                name,
                1L,
                "测试类目",
                roomCategoryType,
                sellingPriceCent,
                originalPriceCent,
                settlementPriceCent,
                stock,
                stockMode,
                shelfStatus,
                "2026-05-01 00:00:00",
                "2027-05-01 00:00:00",
                description,
                "购买后按门店规则退款",
                "13800000000",
                "请联系客服确认",
                status,
                "integration-test",
                0
        );
    }

    private void insertGoodsSku(
            long goodsSkuId,
            long campId,
            long goodsId,
            String skuName,
            long sellingPriceCent,
            long originalPriceCent,
            int stock,
            int sortNo
    ) {
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
                goodsSkuId,
                campId,
                goodsId,
                String.valueOf(goodsSkuId),
                skuName,
                sellingPriceCent,
                originalPriceCent,
                stock,
                sortNo,
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
}
