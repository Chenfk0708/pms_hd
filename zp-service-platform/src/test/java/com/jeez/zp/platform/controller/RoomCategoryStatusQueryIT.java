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
class RoomCategoryStatusQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void roomCategoryStatusesCentralGet_shouldReturnCurrentFrontendCalendarShape() throws Exception {
        seedRoomCategoryStatuses();

        mockMvc.perform(post("/roomCategoryStatuses/central/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "channelIds":["5"],
                                  "date":"2026-05-18",
                                  "days":3,
                                  "pageNum":1,
                                  "pageSize":10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.pageX.total").value(2))
                .andExpect(jsonPath("$.data.pageX.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageX.pageSize").value(10))
                .andExpect(jsonPath("$.data.pageX.hasNextPage").value(false))
                .andExpect(jsonPath("$.data.roomStatusViews.length()").value(2))
                .andExpect(jsonPath("$.data.roomStatusViews[0].roomCategoryId").value("22001"))
                .andExpect(jsonPath("$.data.roomStatusViews[0].roomCategoryName").value("标准大床房"))
                .andExpect(jsonPath("$.data.roomStatusViews[0].normalPrice").value(19900))
                .andExpect(jsonPath("$.data.roomStatusViews[0].normalActualSalePrice").value(19900))
                .andExpect(jsonPath("$.data.roomStatusViews[0].statusViews.length()").value(3))
                .andExpect(jsonPath("$.data.roomStatusViews[0].statusViews[0].date").value("2026-05-18"))
                .andExpect(jsonPath("$.data.roomStatusViews[0].statusViews[0].totalStock").value(5))
                .andExpect(jsonPath("$.data.roomStatusViews[0].statusViews[0].price").value(19900))
                .andExpect(jsonPath("$.data.roomStatusViews[0].channelRoomCategoryStatuses.length()").value(1))
                .andExpect(jsonPath("$.data.roomStatusViews[0].channelRoomCategoryStatuses[0].channelId").value("5"))
                .andExpect(jsonPath("$.data.roomStatusViews[0].channelRoomCategoryStatuses[0].channelName").value("携程"))
                .andExpect(jsonPath("$.data.roomStatusViews[0].channelRoomCategoryStatuses[0].channelRoomCategoryName").value("标准大床房<无早>"))
                .andExpect(jsonPath("$.data.roomStatusViews[0].channelRoomCategoryStatuses[0].expressValue").value("1.00"))
                .andExpect(jsonPath("$.data.roomStatusViews[0].channelRoomCategoryStatuses[0].statusViews[0].salePrice").value(19900));
    }

    @Test
    @Timeout(60)
    void roomCategoryStatusesRoomCategoryChannelGet_shouldReturnChannelRpRowsUsedByCurrentFrontend() throws Exception {
        seedRoomCategoryStatuses();

        mockMvc.perform(post("/roomCategoryStatuses/roomCategory/channel/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"",
                                  "channelIds":["5"],
                                  "date":"2026-05-18",
                                  "days":3,
                                  "pageNum":1,
                                  "pageSize":10,
                                  "isFinalChannelRp":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].roomCategoryName").value("标准大床房"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryProductName").value("标准大床房<无早>"))
                .andExpect(jsonPath("$.data.list[0].expressValue").value("1.00"))
                .andExpect(jsonPath("$.data.list[0].normalPrice").value(19900))
                .andExpect(jsonPath("$.data.list[0].normalActualSalePrice").value(19900))
                .andExpect(jsonPath("$.data.list[0].statusViews.length()").value(3))
                .andExpect(jsonPath("$.data.list[0].statusViews[0].date").value("2026-05-18"))
                .andExpect(jsonPath("$.data.list[0].statusViews[0].price").value(19900))
                .andExpect(jsonPath("$.data.list[0].statusViews[0].salePrice").value(19900));
    }

    @Test
    @Timeout(60)
    void roomCategoryStatusEndpoints_shouldRejectForeignCampAccess() throws Exception {
        mockMvc.perform(post("/roomCategoryStatuses/central/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "date":"2026-05-18",
                                  "days":3,
                                  "pageNum":1,
                                  "pageSize":10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301));

        mockMvc.perform(post("/roomCategoryStatuses/roomCategory/channel/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "date":"2026-05-18",
                                  "days":3,
                                  "pageNum":1,
                                  "pageSize":10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301));
    }

    private void seedRoomCategoryStatuses() {
        resetRoomCategoryStatuses();

        insertChannelAccount(38301L, 17L, "路客云聚合", "tdd-localhome", "OUT-17-A", "authorized");
        insertChannelAccount(38302L, 5L, "携程", "tdd-ctrip", "OUT-5-A", "authorized");

        insertChannelRoomCategoryRel(38401L, 38301L, 22001L, "on_shelf", "approved");
        insertChannelRoomCategoryRel(38402L, 38302L, 22001L, "on_shelf", "approved");
        insertChannelRoomCategoryRel(38403L, 38302L, 22002L, "on_shelf", "approved");

        insertGoodsMain(38501L, "标准大床房<无早>", 19900, "published");
        insertGoodsMain(38502L, "豪华双床房<双早>", 39900, "published");
        insertGoodsRoomCategoryRel(38601L, 38501L, 22001L);
        insertGoodsRoomCategoryRel(38602L, 38502L, 22002L);
        insertDistributionProduct(38701L, 38501L, 1, 0.10d);
        insertDistributionProduct(38702L, 38502L, 1, 0.12d);
    }

    private void resetRoomCategoryStatuses() {
        jdbcTemplate.update("""
                DELETE grr
                FROM goods_room_category_rel grr
                JOIN goods_main gm ON gm.goods_id = grr.goods_id
                WHERE gm.camp_id = ?
                """, CAMP_ID);
        jdbcTemplate.update("DELETE FROM distribution_product WHERE camp_id = ?", CAMP_ID);
        jdbcTemplate.update("DELETE FROM goods_sku WHERE camp_id = ?", CAMP_ID);
        jdbcTemplate.update("DELETE FROM goods_main WHERE camp_id = ?", CAMP_ID);
        jdbcTemplate.update("DELETE FROM channel_room_category_rel WHERE camp_id = ?", CAMP_ID);
        jdbcTemplate.update("DELETE FROM channel_poi_rel WHERE camp_id = ?", CAMP_ID);
        jdbcTemplate.update("DELETE FROM channel_account WHERE camp_id = ?", CAMP_ID);
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
                CAMP_ID,
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
                CAMP_ID,
                accountId,
                roomCategoryId,
                "OUT-RC-" + roomCategoryId + "-" + accountId,
                shelfStatus,
                auditStatus
        );
    }

    private void insertGoodsMain(long goodsId, String name, int sellingPrice, String status) {
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
                            is_deleted
                        ) VALUES (?, ?, 'package', ?, ?, ?, ?, ?, ?, ?, ?, 0)
                        """,
                goodsId,
                CAMP_ID,
                name,
                sellingPrice,
                sellingPrice,
                sellingPrice,
                50,
                "13800000001",
                "自动生成",
                status
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

    private void insertDistributionProduct(long distributionProductId, long goodsId, int status, double rate) {
        jdbcTemplate.update("""
                        INSERT INTO distribution_product (
                            distribution_product_id,
                            camp_id,
                            goods_id,
                            room_category_id,
                            commission_rule_json,
                            status
                        ) VALUES (?, ?, ?, NULL, CAST(? AS JSON), ?)
                        """,
                distributionProductId,
                CAMP_ID,
                goodsId,
                "{\"rate\": " + rate + ", \"type\": \"fixed_rate\"}",
                status
        );
    }
}
