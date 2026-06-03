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
class WeiRoomCategoryQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CATALOG_CAMP_ID = 64064L;
    private static final long BUY_CAMP_ID = 10001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void weiRoomCategoriesPageGet_shouldReturnPagedCatalogItemsByGoodsType() throws Exception {
        insertCatalogFixture();

        mockMvc.perform(post("/weiRoomCategories/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"64064",
                                  "buyCampId":"10001",
                                  "roomCategoryTypes":[1],
                                  "goodsTypes":[7],
                                  "pageNum":1,
                                  "pageSize":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.size").value(1))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pages").value(2))
                .andExpect(jsonPath("$.data.hasNextPage").value(true))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].channelRoomCategoryId").value("98601"))
                .andExpect(jsonPath("$.data.list[0].channelRoomCategoryName").value("TDD Wei Electronic Price Tag"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryType").value(1))
                .andExpect(jsonPath("$.data.list[0].goodsType").value(7))
                .andExpect(jsonPath("$.data.list[0].mainPhoto").value("https://example.com/wei-room-category-98601.jpg"))
                .andExpect(jsonPath("$.data.list[0].lowestSellingPrice").value(49900))
                .andExpect(jsonPath("$.data.list[0].lowestOriginalPrice").value(89900))
                .andExpect(jsonPath("$.data.list[0].lowestSettlementPrice").value(43000))
                .andExpect(jsonPath("$.data.list[0].totalStock").value("120"))
                .andExpect(jsonPath("$.data.list[0].isCanBooking").value(1))
                .andExpect(jsonPath("$.data.list[0].isAvailability").value("1"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryProductGetViews.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].roomCategoryProductGetViews[0].roomCategoryProductId").value("98611"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryProductGetViews[0].roomCategoryProductName").value("1年"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryProductGetViews[0].sellingPrice").value(49900))
                .andExpect(jsonPath("$.data.list[0].roomCategoryProductGetViews[0].originalPrice").value(89900))
                .andExpect(jsonPath("$.data.list[0].roomCategoryProductGetViews[0].stock").value(100))
                .andExpect(jsonPath("$.data.list[0].roomCategoryProductGetViews[1].roomCategoryProductId").value("98612"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryProductGetViews[1].roomCategoryProductName").value("2年"));
    }

    @Test
    @Timeout(60)
    void weiRoomCategoriesPageGet_shouldFallbackCurrentBuyerCampAndReturnVersionGoods() throws Exception {
        insertCatalogFixture();

        mockMvc.perform(post("/weiRoomCategories/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"64064",
                                  "buyCampId":"",
                                  "roomCategoryTypes":[1],
                                  "goodsTypes":[2]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].channelRoomCategoryId").value("98603"))
                .andExpect(jsonPath("$.data.list[0].channelRoomCategoryName").value("TDD Wei Enjoy Edition"))
                .andExpect(jsonPath("$.data.list[0].goodsType").value(2))
                .andExpect(jsonPath("$.data.list[0].roomCategoryProductGetViews[0].roomCategoryProductName").value("12个月"));
    }

    @Test
    @Timeout(60)
    void weiRoomCategoriesPageGet_shouldRejectForeignBuyerCampAccess() throws Exception {
        mockMvc.perform(post("/weiRoomCategories/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"64064",
                                  "buyCampId":"10002",
                                  "roomCategoryTypes":[1],
                                  "goodsTypes":[7]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @Timeout(60)
    void weiRoomCategoriesPageGet_shouldOrderByPersistedSortRemark() throws Exception {
        insertCatalogFixture();

        jdbcTemplate.update("UPDATE goods_main SET remark = 'sort:2' WHERE goods_id = 98601");
        jdbcTemplate.update("UPDATE goods_main SET remark = 'sort:1' WHERE goods_id = 98602");
        jdbcTemplate.update("UPDATE goods_main SET remark = 'integration-test' WHERE goods_id = 98603");

        mockMvc.perform(post("/weiRoomCategories/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"64064",
                                  "buyCampId":"10001",
                                  "roomCategoryTypes":[1],
                                  "goodsTypes":[7],
                                  "pageNum":1,
                                  "pageSize":10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.list[0].channelRoomCategoryId").value("98602"))
                .andExpect(jsonPath("$.data.list[1].channelRoomCategoryId").value("98601"));
    }

    private void insertCatalogFixture() {
        insertCamp(CATALOG_CAMP_ID, "中央商品目录");
        seedBuyerRoomCategories();
        insertMediaResource(98591L, BUY_CAMP_ID, "wei-room-category-98601.jpg", "https://example.com/wei-room-category-98601.jpg");
        insertRoomCategoryMedia(98592L, BUY_CAMP_ID, 22001L, 98591L, "https://example.com/wei-room-category-98601.jpg", 0);

        insertGoodsMain(98601L, CATALOG_CAMP_ID, "7", "TDD Wei Electronic Price Tag", 1, 49900L, 89900L, 43000L, 120, "manual", "on_shelf", "published", "电子房价牌年付套餐");
        insertGoodsSku(98611L, CATALOG_CAMP_ID, 98601L, "1年", 49900L, 89900L, 100, 1);
        insertGoodsSku(98612L, CATALOG_CAMP_ID, 98601L, "2年", 99800L, 179800L, 20, 2);
        insertGoodsRoomCategoryRel(98601L, 22001L);
        insertDistributionProduct(98593L, BUY_CAMP_ID, 98601L);

        insertGoodsMain(98602L, CATALOG_CAMP_ID, "7", "TDD Wei Ctrip Direct", 1, 24600L, 24600L, 24600L, 30, "manual", "on_shelf", "published", "渠道直连资源");
        insertGoodsSku(98621L, CATALOG_CAMP_ID, 98602L, "30天", 7380L, 7380L, 30, 1);
        insertGoodsRoomCategoryRel(98602L, 22002L);

        insertGoodsMain(98603L, CATALOG_CAMP_ID, "2", "TDD Wei Enjoy Edition", 1, 299000L, 399000L, 280000L, 9999, "manual", "on_shelf", "published", "版本订阅资源");
        insertGoodsSku(98631L, CATALOG_CAMP_ID, 98603L, "12个月", 299000L, 399000L, 9999, 1);
        insertGoodsRoomCategoryRel(98603L, 22003L);
    }


    private void seedBuyerRoomCategories() {
        insertRoomCategory(22001L, "TDD Wei Standard", 1);
        insertRoomCategory(22002L, "TDD Wei Twin", 2);
        insertRoomCategory(22003L, "TDD Wei Family", 3);
    }

    private void insertRoomCategory(long roomCategoryId, String name, int sortNo) {
        jdbcTemplate.update("""
                        INSERT INTO room_category (
                            room_category_id,
                            camp_id,
                            poi_id,
                            name,
                            display_name,
                            room_count,
                            status,
                            sort_no,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE
                            camp_id = VALUES(camp_id),
                            poi_id = VALUES(poi_id),
                            name = VALUES(name),
                            display_name = VALUES(display_name),
                            room_count = VALUES(room_count),
                            status = VALUES(status),
                            sort_no = VALUES(sort_no),
                            is_deleted = VALUES(is_deleted)
                        """,
                roomCategoryId,
                BUY_CAMP_ID,
                11001L,
                name,
                name,
                1,
                1,
                sortNo,
                0
        );
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

    private void insertCamp(long campId, String name) {
        jdbcTemplate.update("""
                        INSERT INTO pms_camp (
                            camp_id,
                            name,
                            type,
                            network_num,
                            province_id,
                            province_name,
                            city_id,
                            city_name,
                            county_id,
                            county_name,
                            address,
                            contact_number,
                            status,
                            created_by,
                            updated_by,
                            is_deleted,
                            version_no
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE
                            name = VALUES(name),
                            network_num = VALUES(network_num),
                            status = VALUES(status),
                            is_deleted = VALUES(is_deleted),
                            version_no = VALUES(version_no)
                        """,
                campId,
                name,
                1,
                "network-" + campId,
                "440000",
                "广东省",
                "440300",
                "深圳市",
                "440305",
                "南山区",
                "南山区科技园",
                "0755-00000000",
                1,
                12001L,
                12001L,
                0,
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

    private void insertMediaResource(long mediaResourceId, long campId, String name, String url) {
        jdbcTemplate.update("""
                        INSERT INTO media_resource (
                            media_resource_id,
                            camp_id,
                            parent_id,
                            path,
                            name,
                            is_dir,
                            format,
                            size_bytes,
                            width,
                            height,
                            url,
                            biz_type,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                mediaResourceId,
                campId,
                null,
                "/integration/" + name,
                name,
                0,
                "jpg",
                1024L,
                640,
                480,
                url,
                "integration-test",
                0
        );
    }

    private void insertRoomCategoryMedia(
            long mediaId,
            long campId,
            long roomCategoryId,
            long mediaResourceId,
            String mediaUrl,
            int sortNo
    ) {
        jdbcTemplate.update("""
                        INSERT INTO room_category_media (
                            media_id,
                            camp_id,
                            room_category_id,
                            media_resource_id,
                            media_type,
                            media_url,
                            scene_type,
                            sort_no,
                            status
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                mediaId,
                campId,
                roomCategoryId,
                mediaResourceId,
                "image",
                mediaUrl,
                "cover",
                sortNo,
                1
        );
    }

    private void insertDistributionProduct(long distributionProductId, long campId, long goodsId) {
        jdbcTemplate.update("""
                        INSERT INTO distribution_product (
                            distribution_product_id,
                            camp_id,
                            goods_id,
                            room_category_id,
                            commission_rule_json,
                            status
                        ) VALUES (?, ?, ?, ?, ?, ?)
                        """,
                distributionProductId,
                campId,
                goodsId,
                null,
                "{\"rate\":0.1,\"type\":\"fixed_rate\"}",
                1
        );
    }
}
