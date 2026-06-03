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
class ChannelRoomCategoryPageV2IT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;
    private static final long USER_ID = 12001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void channelRoomCategoriesPageGetV2_shouldReturnFilteredRealGoodsWithChannelsProductsAndSoldCount() throws Exception {
        seedFixture();

        mockMvc.perform(post("/channelRoomCategories/page/get/v2")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "roomCategoryTypes":[1],
                                  "categoryIds":["14"],
                                  "searchKey":"Breakfast",
                                  "pageNum":1,
                                  "pageSize":20,
                                  "getChannelIds":[],
                                  "isGetChannelInfo":1,
                                  "channelIds":[17],
                                  "poiIds":["11001"],
                                  "shelfStatuses":["selling"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pages").value(1))
                .andExpect(jsonPath("$.data.hasNextPage").value(false))
                .andExpect(jsonPath("$.data.pagination.page").value(1))
                .andExpect(jsonPath("$.data.pagination.pageSize").value(20))
                .andExpect(jsonPath("$.data.pagination.total").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].channelRoomCategoryId").value("129501"))
                .andExpect(jsonPath("$.data.list[0].channelRoomCategoryName").value("TDD Breakfast Coupon"))
                .andExpect(jsonPath("$.data.list[0].categoryId").value("14"))
                .andExpect(jsonPath("$.data.list[0].categoryName").value("Room Coupon"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryType").value(1))
                .andExpect(jsonPath("$.data.list[0].goodsType").value(7))
                .andExpect(jsonPath("$.data.list[0].channelIds.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].channelIds[0]").value("5"))
                .andExpect(jsonPath("$.data.list[0].channelIds[1]").value("17"))
                .andExpect(jsonPath("$.data.list[0].channelNames[0]").value("Ctrip"))
                .andExpect(jsonPath("$.data.list[0].channelNames[1]").value("LocalHome"))
                .andExpect(jsonPath("$.data.list[0].totalStock").value("120"))
                .andExpect(jsonPath("$.data.list[0].soldCount").value(3))
                .andExpect(jsonPath("$.data.list[0].lowestSellingPrice").value(19900))
                .andExpect(jsonPath("$.data.list[0].lowestOriginalPrice").value(26800))
                .andExpect(jsonPath("$.data.list[0].isCanBooking").value(1))
                .andExpect(jsonPath("$.data.list[0].isAvailability").value("1"))
                .andExpect(jsonPath("$.data.list[0].shelfStatus").value("selling"))
                .andExpect(jsonPath("$.data.list[0].createdAt").value("2026-05-26 10:00:00"))
                .andExpect(jsonPath("$.data.list[0].updatedAt").value("2026-05-26 12:00:00"))
                .andExpect(jsonPath("$.data.list[0].description").value("Breakfast package"))
                .andExpect(jsonPath("$.data.list[0].refundRule").value("Refund before use"))
                .andExpect(jsonPath("$.data.list[0].products.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].products[0].roomCategoryProductId").value("129511"))
                .andExpect(jsonPath("$.data.list[0].products[0].roomCategoryProductName").value("Breakfast single"))
                .andExpect(jsonPath("$.data.list[0].products[0].sellingPrice").value(19900))
                .andExpect(jsonPath("$.data.list[0].products[0].originalPrice").value(26800))
                .andExpect(jsonPath("$.data.list[0].products[0].stock").value(80));
    }

    @Test
    @Timeout(60)
    void channelRoomCategoriesPageGetV2_shouldFilterSoldOutAndWarehouseStatus() throws Exception {
        seedFixture();

        mockMvc.perform(post("/channelRoomCategories/page/get/v2")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "roomCategoryTypes":[1,2,3],
                                  "pageNum":1,
                                  "pageSize":20,
                                  "channelIds":[0],
                                  "shelfStatuses":["soldOut","warehouse"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].channelRoomCategoryId").value("129502"))
                .andExpect(jsonPath("$.data.list[0].shelfStatus").value("soldOut"))
                .andExpect(jsonPath("$.data.list[0].isCanBooking").value(0))
                .andExpect(jsonPath("$.data.list[0].isAvailability").value("0"))
                .andExpect(jsonPath("$.data.list[1].channelRoomCategoryId").value("129503"))
                .andExpect(jsonPath("$.data.list[1].shelfStatus").value("warehouse"));
    }

    @Test
    @Timeout(60)
    void channelRoomCategoriesPageGetV2_shouldRejectForeignCampAccess() throws Exception {
        mockMvc.perform(post("/channelRoomCategories/page/get/v2")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
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
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    private void seedFixture() {
        resetFixtureData();

        insertRoomCategory(129401L, 11001L, "TDD Deluxe King");
        insertRoomCategory(129402L, 11001L, "TDD Family Suite");

        insertChannelAccount(129301L, 17L, "LocalHome", "localhome-main", "OUT-17");
        insertChannelAccount(129302L, 5L, "Ctrip", "ctrip-main", "OUT-5");
        insertChannelAccount(129303L, 8L, "Fliggy", "fliggy-main", "OUT-8");
        insertChannelPoiRel(129201L, 129301L, 11001L);
        insertChannelPoiRel(129202L, 129302L, 11001L);
        insertChannelPoiRel(129203L, 129303L, 11001L);
        insertChannelRoomCategoryRel(129101L, 129301L, 129401L, "on_shelf", "approved");
        insertChannelRoomCategoryRel(129102L, 129302L, 129401L, "on_shelf", "approved");
        insertChannelRoomCategoryRel(129103L, 129303L, 129402L, "on_shelf", "approved");

        insertGoodsMain(129501L, "TDD Breakfast Coupon", 14L, "Room Coupon", 1, 19900L, 26800L, 120, "limited", "on_shelf", "published", "Breakfast package", "Refund before use", "2026-05-26 10:00:00", "2026-05-26 12:00:00");
        insertGoodsRoomCategoryRel(129601L, 129501L, 129401L);
        insertGoodsSku(129511L, 129501L, "Breakfast single", 19900L, 26800L, 80, 1);
        insertGoodsSku(129512L, 129501L, "Breakfast double", 39900L, 46800L, 40, 2);
        insertOrderMain(129701L, 129501L, 129301L, "completed", 2);
        insertOrderMain(129702L, 129501L, 129302L, "completed", 1);
        insertOrderMain(129703L, 129501L, 129302L, "cancelled", 99);

        insertGoodsMain(129502L, "TDD Sold Out Coupon", 16L, "Dining Coupon", 2, 49800L, 58800L, 0, "limited", "on_shelf", "published", "Sold out package", "No refund after use", "2026-05-27 10:00:00", "2026-05-27 12:00:00");
        insertGoodsRoomCategoryRel(129602L, 129502L, 129401L);
        insertGoodsSku(129521L, 129502L, "Weekend single", 49800L, 58800L, 0, 1);

        insertGoodsMain(129503L, "TDD Warehouse Coupon", 17L, "Package Coupon", 3, 88800L, 108800L, 30, "limited", "off_shelf", "draft", "Warehouse package", "Refund before booking", "2026-05-28 10:00:00", "2026-05-28 12:00:00");
        insertGoodsRoomCategoryRel(129603L, 129503L, 129402L);
        insertGoodsSku(129531L, 129503L, "Birthday package", 88800L, 108800L, 30, 1);
    }

    private void resetFixtureData() {
        jdbcTemplate.update("DELETE FROM order_main WHERE goods_id BETWEEN 129501 AND 129599 OR order_id BETWEEN 129701 AND 129799");
        jdbcTemplate.update("DELETE FROM goods_sku WHERE goods_id BETWEEN 129501 AND 129599 OR goods_sku_id BETWEEN 129511 AND 129599");
        jdbcTemplate.update("DELETE FROM goods_room_category_rel WHERE goods_id BETWEEN 129501 AND 129599 OR id BETWEEN 129601 AND 129699");
        jdbcTemplate.update("DELETE FROM goods_main WHERE goods_id BETWEEN 129501 AND 129599");
        jdbcTemplate.update("DELETE FROM channel_room_category_rel WHERE id BETWEEN 129101 AND 129199");
        jdbcTemplate.update("DELETE FROM channel_poi_rel WHERE id BETWEEN 129201 AND 129299");
        jdbcTemplate.update("DELETE FROM channel_account WHERE account_id BETWEEN 129301 AND 129399");
        jdbcTemplate.update("DELETE FROM room_category WHERE room_category_id BETWEEN 129401 AND 129499");
    }

    private void insertRoomCategory(long roomCategoryId, long poiId, String name) {
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
                        """,
                roomCategoryId,
                CAMP_ID,
                poiId,
                name,
                name,
                1,
                1,
                1,
                0
        );
    }

    private void insertChannelAccount(long accountId, long channelId, String channelName, String accountName, String outAccountId) {
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
                CAMP_ID,
                channelId,
                channelName,
                accountName,
                outAccountId,
                "authorized"
        );
    }

    private void insertChannelPoiRel(long id, long accountId, long poiId) {
        jdbcTemplate.update("""
                        INSERT INTO channel_poi_rel (
                            id,
                            camp_id,
                            account_id,
                            poi_id,
                            out_poi_id,
                            sync_status
                        ) VALUES (?, ?, ?, ?, ?, ?)
                        """,
                id,
                CAMP_ID,
                accountId,
                poiId,
                "OUT-POI-" + poiId,
                "synced"
        );
    }

    private void insertChannelRoomCategoryRel(long id, long accountId, long roomCategoryId, String shelfStatus, String auditStatus) {
        jdbcTemplate.update("""
                        INSERT INTO channel_room_category_rel (
                            id,
                            camp_id,
                            account_id,
                            room_category_id,
                            out_room_category_id,
                            project_type,
                            shelf_status,
                            audit_status
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                id,
                CAMP_ID,
                accountId,
                roomCategoryId,
                "OUT-RC-" + roomCategoryId + "-" + accountId,
                "calendar_room",
                shelfStatus,
                auditStatus
        );
    }

    private void insertGoodsMain(
            long goodsId,
            String name,
            long categoryId,
            String categoryName,
            int roomCategoryType,
            long sellingPriceCent,
            long originalPriceCent,
            int stock,
            String stockMode,
            String shelfStatus,
            String status,
            String description,
            String refundRule,
            String createdAt,
            String updatedAt
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
                            description,
                            refund_rule,
                            reservation_phone,
                            reservation_note,
                            status,
                            remark,
                            created_at,
                            updated_at,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                goodsId,
                CAMP_ID,
                "7",
                name,
                categoryId,
                categoryName,
                roomCategoryType,
                sellingPriceCent,
                originalPriceCent,
                sellingPriceCent,
                stock,
                stockMode,
                shelfStatus,
                description,
                refundRule,
                "13800000000",
                "booking note",
                status,
                "integration-test",
                createdAt,
                updatedAt,
                0
        );
    }

    private void insertGoodsRoomCategoryRel(long id, long goodsId, long roomCategoryId) {
        jdbcTemplate.update("""
                        INSERT INTO goods_room_category_rel (
                            id,
                            goods_id,
                            room_category_id
                        ) VALUES (?, ?, ?)
                        """,
                id,
                goodsId,
                roomCategoryId
        );
    }

    private void insertGoodsSku(long goodsSkuId, long goodsId, String skuName, long sellingPriceCent, long originalPriceCent, int stock, int sortNo) {
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
                CAMP_ID,
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

    private void insertOrderMain(long orderId, long goodsId, long channelAccountId, String status, int count) {
        LocalDateTime createdAt = LocalDateTime.of(2026, 5, 29, 10, 0).plusMinutes(orderId - 129700L);
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
                129401L,
                null,
                channelAccountId,
                goodsId,
                "ORDER-" + orderId,
                "OUT-" + orderId,
                "goods_order",
                status,
                "Guest " + orderId,
                "1390000" + orderId,
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt.plusHours(2)),
                count,
                19900L * count,
                0L,
                19900L * count,
                0L,
                0L,
                0L,
                0L,
                0L,
                19900L * count,
                "paid",
                null,
                null,
                "mini_program",
                "channel-room-category-v2-test",
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt.plusMinutes(5)),
                USER_ID,
                USER_ID
        );
    }
}
