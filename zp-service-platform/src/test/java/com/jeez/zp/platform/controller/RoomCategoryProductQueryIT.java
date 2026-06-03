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
class RoomCategoryProductQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void roomCategoryProductsPageGet_shouldReturnPaginatedProductsWithAuthorizedChannels() throws Exception {
        resetProductData();
        seedRoomCategories();

        insertChannelAccount(27301L, 17L, "路客云聚合", "tdd-localhome", "OUT-17-A", "authorized");
        insertChannelAccount(27302L, 5L, "携程", "tdd-ctrip", "OUT-5-A", "authorized");
        insertChannelAccount(27303L, 8L, "飞猪酒店", "tdd-fliggy", "OUT-8-A", "expired");

        insertChannelRoomCategoryRel(27401L, 27301L, 22001L, "on_shelf", "approved");
        insertChannelRoomCategoryRel(27402L, 27302L, 22001L, "on_shelf", "approved");
        insertChannelRoomCategoryRel(27403L, 27302L, 22002L, "on_shelf", "approved");
        insertChannelRoomCategoryRel(27404L, 27303L, 22003L, "on_shelf", "approved");
        insertChannelRoomCategoryRel(27405L, 27302L, 22003L, "off_shelf", "approved");

        insertGoodsMain(
                27501L,
                "标准大床房预售券",
                "coupon",
                19900,
                26800,
                18000,
                100,
                "13800000001",
                "请至少提前一天预约。",
                "published",
                "2026-05-26 10:00:00",
                "2026-05-26 12:00:00"
        );
        insertGoodsRoomCategoryRel(27601L, 27501L, 22001L);
        insertDistributionProduct(27701L, 27501L, 1, "2026-05-26 10:00:00", "2026-05-26 12:00:00");

        insertGoodsMain(
                27502L,
                "豪华双床房早餐套餐",
                "package",
                39900,
                46800,
                36000,
                50,
                "13800000002",
                "入住前需二次确认。",
                "draft",
                "2026-05-26 10:30:00",
                "2026-05-26 12:30:00"
        );
        insertGoodsRoomCategoryRel(27602L, 27502L, 22002L);
        insertDistributionProduct(27702L, 27502L, 1, "2026-05-26 10:30:00", "2026-05-26 12:30:00");

        insertGoodsMain(
                27503L,
                "家庭套房亲子套餐",
                "package",
                59900,
                68800,
                55000,
                30,
                "13800000003",
                "节假日需补差价。",
                "published",
                "2026-05-26 11:00:00",
                "2026-05-26 13:00:00"
        );
        insertGoodsRoomCategoryRel(27603L, 27503L, 22003L);
        insertDistributionProduct(27703L, 27503L, 0, "2026-05-26 11:00:00", "2026-05-26 13:00:00");

        mockMvc.perform(post("/roomCategoryProducts/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":1,
                                  "pageSize":2,
                                  "keyword":"",
                                  "roomCategoryId":"",
                                  "channelId":""
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.pagination.page").value(1))
                .andExpect(jsonPath("$.data.pagination.pageSize").value(2))
                .andExpect(jsonPath("$.data.pagination.total").value(3))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].productId").value("27701-22001-17"))
                .andExpect(jsonPath("$.data.list[0].id").value("27701-22001-17"))
                .andExpect(jsonPath("$.data.list[0].title").value("标准大床房预售券"))
                .andExpect(jsonPath("$.data.list[0].productName").value("标准大床房预售券"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryId").value("22001"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryName").value("TDD Room Product Standard"))
                .andExpect(jsonPath("$.data.list[0].channelId").value("17"))
                .andExpect(jsonPath("$.data.list[0].channelName").value("路客云聚合"))
                .andExpect(jsonPath("$.data.list[0].stock").value(100))
                .andExpect(jsonPath("$.data.list[0].salePrice").value(19900))
                .andExpect(jsonPath("$.data.list[0].extraPrice").value(6900))
                .andExpect(jsonPath("$.data.list[0].status").value("published"))
                .andExpect(jsonPath("$.data.list[0].reservationPhone").value("13800000001"))
                .andExpect(jsonPath("$.data.list[0].reservationNote").value("请至少提前一天预约。"))
                .andExpect(jsonPath("$.data.list[0].createdAt").value("2026-05-26 10:00:00"))
                .andExpect(jsonPath("$.data.list[0].updatedAt").value("2026-05-26 12:00:00"))
                .andExpect(jsonPath("$.data.list[1].productId").value("27701-22001-5"))
                .andExpect(jsonPath("$.data.list[1].channelName").value("携程"));
    }

    @Test
    @Timeout(60)
    void roomCategoryProductsPageGet_shouldFilterByKeywordRoomCategoryAndChannel() throws Exception {
        seedBasicProductData();

        mockMvc.perform(post("/roomCategoryProducts/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":1,
                                  "pageSize":20,
                                  "keyword":"早餐",
                                  "roomCategoryId":"22002",
                                  "channelId":"5"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].productId").value("27702-22002-5"))
                .andExpect(jsonPath("$.data.list[0].title").value("豪华双床房早餐套餐"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryName").value("TDD Room Product Twin"))
                .andExpect(jsonPath("$.data.list[0].channelName").value("携程"));
    }

    @Test
    @Timeout(60)
    void roomCategoryProductsPageGet_shouldFallbackCurrentCampWhenCampIdBlank() throws Exception {
        seedBasicProductData();

        mockMvc.perform(post("/roomCategoryProducts/page/get")
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
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.list.length()").value(3));
    }

    @Test
    @Timeout(60)
    void roomCategoryProductsPageGet_shouldRejectForeignCampAccess() throws Exception {
        mockMvc.perform(post("/roomCategoryProducts/page/get")
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
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    private void seedBasicProductData() {
        resetProductData();
        seedRoomCategories();

        insertChannelAccount(27301L, 17L, "路客云聚合", "tdd-localhome", "OUT-17-A", "authorized");
        insertChannelAccount(27302L, 5L, "携程", "tdd-ctrip", "OUT-5-A", "authorized");

        insertChannelRoomCategoryRel(27401L, 27301L, 22001L, "on_shelf", "approved");
        insertChannelRoomCategoryRel(27402L, 27302L, 22001L, "on_shelf", "approved");
        insertChannelRoomCategoryRel(27403L, 27302L, 22002L, "on_shelf", "approved");

        insertGoodsMain(
                27501L,
                "标准大床房预售券",
                "coupon",
                19900,
                26800,
                18000,
                100,
                "13800000001",
                "请至少提前一天预约。",
                "published",
                "2026-05-26 10:00:00",
                "2026-05-26 12:00:00"
        );
        insertGoodsRoomCategoryRel(27601L, 27501L, 22001L);
        insertDistributionProduct(27701L, 27501L, 1, "2026-05-26 10:00:00", "2026-05-26 12:00:00");

        insertGoodsMain(
                27502L,
                "豪华双床房早餐套餐",
                "package",
                39900,
                46800,
                36000,
                50,
                "13800000002",
                "入住前需二次确认。",
                "draft",
                "2026-05-26 10:30:00",
                "2026-05-26 12:30:00"
        );
        insertGoodsRoomCategoryRel(27602L, 27502L, 22002L);
        insertDistributionProduct(27702L, 27502L, 1, "2026-05-26 10:30:00", "2026-05-26 12:30:00");
    }

    private void resetProductData() {
        jdbcTemplate.update("""
                DELETE grr
                FROM goods_room_category_rel grr
                JOIN goods_main gm ON gm.goods_id = grr.goods_id
                WHERE gm.camp_id = ?
                """, 10001L);
        jdbcTemplate.update("DELETE FROM distribution_product WHERE camp_id = ?", 10001L);
        jdbcTemplate.update("DELETE FROM goods_sku WHERE camp_id = ?", 10001L);
        jdbcTemplate.update("DELETE FROM goods_main WHERE camp_id = ?", 10001L);
        jdbcTemplate.update("DELETE FROM channel_room_category_rel WHERE camp_id = ?", 10001L);
        jdbcTemplate.update("DELETE FROM channel_poi_rel WHERE camp_id = ?", 10001L);
        jdbcTemplate.update("DELETE FROM channel_account WHERE camp_id = ?", 10001L);
    }


    private void seedRoomCategories() {
        insertRoomCategory(22001L, "TDD Room Product Standard", 1);
        insertRoomCategory(22002L, "TDD Room Product Twin", 2);
        insertRoomCategory(22003L, "TDD Room Product Family", 3);
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
                10001L,
                11001L,
                name,
                name,
                1,
                1,
                sortNo,
                0
        );
    }

    private void insertChannelAccount(
            long accountId,
            long channelId,
            String channelName,
            String accountName,
            String outAccountId,
            String status
    ) {
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
                10001L,
                channelId,
                channelName,
                accountName,
                outAccountId,
                status
        );
    }

    private void insertChannelRoomCategoryRel(
            long id,
            long accountId,
            long roomCategoryId,
            String shelfStatus,
            String auditStatus
    ) {
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
                        ) VALUES (?, ?, ?, ?, ?, 'calendar_room', ?, ?)
                        """,
                id,
                10001L,
                accountId,
                roomCategoryId,
                "OUT-RC-" + roomCategoryId + "-" + accountId,
                shelfStatus,
                auditStatus
        );
    }

    private void insertGoodsMain(
            long goodsId,
            String name,
            String goodsType,
            int sellingPrice,
            int originalPrice,
            int settlementPrice,
            int stock,
            String reservationPhone,
            String reservationNote,
            String status,
            String createdAt,
            String updatedAt
    ) {
        jdbcTemplate.update("""
                        INSERT INTO goods_main (
                            goods_id,
                            camp_id,
                            goods_type,
                            name,
                            selling_price_cent,
                            original_price_cent,
                            settlement_price_cent,
                            stock,
                            reservation_phone,
                            reservation_note,
                            status,
                            created_at,
                            updated_at,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
                        """,
                goodsId,
                10001L,
                goodsType,
                name,
                sellingPrice,
                originalPrice,
                settlementPrice,
                stock,
                reservationPhone,
                reservationNote,
                status,
                createdAt,
                updatedAt
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

    private void insertDistributionProduct(
            long distributionProductId,
            long goodsId,
            int status,
            String createdAt,
            String updatedAt
    ) {
        jdbcTemplate.update("""
                        INSERT INTO distribution_product (
                            distribution_product_id,
                            camp_id,
                            goods_id,
                            room_category_id,
                            commission_rule_json,
                            status,
                            created_at,
                            updated_at
                        ) VALUES (?, ?, ?, NULL, CAST(? AS JSON), ?, ?, ?)
                        """,
                distributionProductId,
                10001L,
                goodsId,
                "{\"rate\": 0.10, \"type\": \"fixed_rate\"}",
                status,
                createdAt,
                updatedAt
        );
    }
}
