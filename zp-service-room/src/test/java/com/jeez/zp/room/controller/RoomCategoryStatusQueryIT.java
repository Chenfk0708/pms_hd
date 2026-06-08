package com.jeez.zp.room.controller;

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

import static org.assertj.core.api.Assertions.assertThat;
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
    private static final long POI_ID = 11001L;
    private static final long CENTRAL_ROOM_CATEGORY_ID = 126001L;
    private static final long CENTRAL_ROOM_ID_A = 126101L;
    private static final long CENTRAL_ROOM_ID_B = 126102L;
    private static final long CENTRAL_ROOM_ID_C = 126103L;
    private static final String CENTRAL_SALE_STATUS_REASON = "\u4e2d\u592e\u4ef7\u505c\u552e\u8054\u52a8\u5173\u623f";
    private static final String MANUAL_CLOSE_REASON = "manual-room-close";

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
                                  "channelIds":["100"],
                                  "roomCategoryIds":["126001","126002"],
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
                .andExpect(jsonPath("$.data.roomStatusViews[0].roomCategoryId").value("126001"))
                .andExpect(jsonPath("$.data.roomStatusViews[0].roomCategoryName").value("TDD状态标准大床房"))
                .andExpect(jsonPath("$.data.roomStatusViews[0].normalPrice").value(19900))
                .andExpect(jsonPath("$.data.roomStatusViews[0].normalActualSalePrice").value(19900))
                .andExpect(jsonPath("$.data.roomStatusViews[0].statusViews.length()").value(3))
                .andExpect(jsonPath("$.data.roomStatusViews[0].statusViews[0].date").value("2026-05-18"))
                .andExpect(jsonPath("$.data.roomStatusViews[0].statusViews[0].totalStock").value(0))
                .andExpect(jsonPath("$.data.roomStatusViews[0].statusViews[0].price").value(19900))
                .andExpect(jsonPath("$.data.roomStatusViews[0].statusViews[0].saleEnabled").value(true))
                .andExpect(jsonPath("$.data.roomStatusViews[0].channelRoomCategoryStatuses.length()").value(1))
                .andExpect(jsonPath("$.data.roomStatusViews[0].channelRoomCategoryStatuses[0].channelId").value("100"))
                .andExpect(jsonPath("$.data.roomStatusViews[0].channelRoomCategoryStatuses[0].channelName").value("宿银平台"))
                .andExpect(jsonPath("$.data.roomStatusViews[0].channelRoomCategoryStatuses[0].channelRoomCategoryName").value("TDD状态标准大床房<无早>"))
                .andExpect(jsonPath("$.data.roomStatusViews[0].channelRoomCategoryStatuses[0].expressValue").value("1.00"))
                .andExpect(jsonPath("$.data.roomStatusViews[0].channelRoomCategoryStatuses[0].statusViews[0].salePrice").value(19900));
    }

    @Test
    @Timeout(60)
    void roomCategoryStatusesCentralGet_shouldIncludeRoomCategoriesWithoutChannelBinding() throws Exception {
        seedRoomCategoryStatuses();
        insertRoomCategory(126003L, "TDD\u65b0\u589e\u65e0\u6e20\u9053\u7ed1\u5b9a\u623f\u578b", 30, 1, 28800);

        mockMvc.perform(post("/roomCategoryStatuses/central/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "channelIds":null,
                                  "roomCategoryIds":["126003"],
                                  "date":"2026-05-18",
                                  "days":3,
                                  "pageNum":1,
                                  "pageSize":10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.pageX.total").value(1))
                .andExpect(jsonPath("$.data.roomStatusViews[0].roomCategoryId").value("126003"))
                .andExpect(jsonPath("$.data.roomStatusViews[0].roomCategoryName").value("TDD\u65b0\u589e\u65e0\u6e20\u9053\u7ed1\u5b9a\u623f\u578b"))
                .andExpect(jsonPath("$.data.roomStatusViews[0].normalPrice").value(28800))
                .andExpect(jsonPath("$.data.roomStatusViews[0].normalActualSalePrice").value(28800))
                .andExpect(jsonPath("$.data.roomStatusViews[0].statusViews.length()").value(3))
                .andExpect(jsonPath("$.data.roomStatusViews[0].channelRoomCategoryStatuses.length()").value(0));
    }

    @Test
    @Timeout(60)
    void roomCategoryStatusesCentralSaleStatusSave_shouldPersistSaleEnabledFlag() throws Exception {
        seedRoomCategoryStatuses();

        mockMvc.perform(post("/roomCategoryStatuses/central/saleStatus/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "roomCategoryId":"126001",
                                  "date":"2026-05-18",
                                  "saleEnabled":false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.roomCategoryId").value("126001"))
                .andExpect(jsonPath("$.data.date").value("2026-05-18"))
                .andExpect(jsonPath("$.data.saleEnabled").value(false));

        mockMvc.perform(post("/roomCategoryStatuses/central/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "campId":"10001",
                  "channelIds":["100"],
                  "roomCategoryIds":["126001"],
                  "date":"2026-05-18",
                  "days":1,
                  "pageNum":1,
                                  "pageSize":10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.roomStatusViews[0].roomCategoryId").value("126001"))
                .andExpect(jsonPath("$.data.roomStatusViews[0].statusViews[0].date").value("2026-05-18"))
                .andExpect(jsonPath("$.data.roomStatusViews[0].statusViews[0].saleEnabled").value(false));
    }

    @Test
    @Timeout(60)
    void roomCategoryStatusesCentralSaleStatusSave_shouldSyncMonthGridBlocksWithoutOpeningManualBlocks() throws Exception {
        seedRoomCategoryStatuses();
        resetCentralSaleStatusRoomFixtures();
        insertRoom(CENTRAL_ROOM_ID_A, "CS-6101");
        insertRoom(CENTRAL_ROOM_ID_B, "CS-6102");
        insertRoomStatusDaily(126801L, "2026-05-18", 2, 0, 2, 0);

        mockMvc.perform(post("/roomStatuses/close/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "roomCategoryId":"126001",
                                  "roomId":"126101",
                                  "date":"2026-05-18",
                                  "reason":"manual-room-close"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/roomCategoryStatuses/central/saleStatus/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "roomCategoryId":"126001",
                                  "date":"2026-05-18",
                                  "saleEnabled":false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.saleEnabled").value(false));

        assertThat(countActiveBlocks(MANUAL_CLOSE_REASON)).isEqualTo(1);
        assertThat(countActiveBlocks(CENTRAL_SALE_STATUS_REASON)).isEqualTo(1);
        assertThat(queryDailyCounter("availability_count")).isEqualTo(0);
        assertThat(queryDailyCounter("close_room_count")).isEqualTo(2);

        mockMvc.perform(post("/roomCategoryStatuses/central/saleStatus/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "roomCategoryId":"126001",
                                  "date":"2026-05-18",
                                  "saleEnabled":true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.saleEnabled").value(true));

        assertThat(countActiveBlocks(MANUAL_CLOSE_REASON)).isEqualTo(1);
        assertThat(countActiveBlocks(CENTRAL_SALE_STATUS_REASON)).isZero();
        assertThat(queryDailyCounter("availability_count")).isEqualTo(1);
        assertThat(queryDailyCounter("close_room_count")).isEqualTo(1);
    }

    @Test
    @Timeout(60)
    void roomCategoryStatusesCentralGet_shouldReturnMonthGridAvailableStock() throws Exception {
        seedRoomCategoryStatuses();
        resetCentralSaleStatusRoomFixtures();
        insertRoom(CENTRAL_ROOM_ID_A, "CS-6101");
        insertRoom(CENTRAL_ROOM_ID_B, "CS-6102");
        insertRoom(CENTRAL_ROOM_ID_C, "CS-6103");
        insertClosedBlock(126821L, CENTRAL_ROOM_ID_B, "2026-05-18", MANUAL_CLOSE_REASON);
        insertOrderMain(126831L, CENTRAL_ROOM_ID_C, "2026-05-18 14:00:00", "2026-05-20 12:00:00");

        mockMvc.perform(post("/roomCategoryStatuses/central/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "channelIds":["100"],
                                  "roomCategoryIds":["126001"],
                                  "date":"2026-05-18",
                                  "days":2,
                                  "pageNum":1,
                                  "pageSize":10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.roomStatusViews[0].statusViews[0].date").value("2026-05-18"))
                .andExpect(jsonPath("$.data.roomStatusViews[0].statusViews[0].totalStock").value(1))
                .andExpect(jsonPath("$.data.roomStatusViews[0].statusViews[1].date").value("2026-05-19"))
                .andExpect(jsonPath("$.data.roomStatusViews[0].statusViews[1].totalStock").value(2))
                .andExpect(jsonPath("$.data.roomStatusViews[0].channelRoomCategoryStatuses[0].statusViews[0].totalStock").value(1))
                .andExpect(jsonPath("$.data.roomStatusViews[0].channelRoomCategoryStatuses[0].statusViews[1].totalStock").value(2));
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
                                  "channelIds":["100"],
                                  "roomCategoryIds":["126001","126002"],
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
                .andExpect(jsonPath("$.data.pageX.total").value(2))
                .andExpect(jsonPath("$.data.pageX.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageX.pageSize").value(10))
                .andExpect(jsonPath("$.data.pageX.hasNextPage").value(false))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].roomCategoryId").value("126001"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryName").value("TDD\u72B6\u6001\u6807\u51C6\u5927\u5E8A\u623F"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryProductName").value("TDD\u72B6\u6001\u6807\u51C6\u5927\u5E8A\u623F<\u65E0\u65E9>"))
                .andExpect(jsonPath("$.data.list[0].channelId").value("100"))
                .andExpect(jsonPath("$.data.list[0].channelName").value("宿银平台"))
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

    @Test
    @Timeout(60)
    void resetRoomCategoryStatuses_shouldPreserveNonFixtureChannelBinding() {
        long preservedRoomCategoryId = 126901L;
        long preservedAccountId = 126931L;
        long preservedRelId = 126941L;

        jdbcTemplate.update(
                "DELETE FROM channel_room_category_rel WHERE id = ? OR account_id = ? OR room_category_id = ?",
                preservedRelId,
                preservedAccountId,
                preservedRoomCategoryId
        );
        jdbcTemplate.update("DELETE FROM channel_poi_rel WHERE account_id = ?", preservedAccountId);
        jdbcTemplate.update("DELETE FROM channel_account WHERE account_id = ?", preservedAccountId);
        jdbcTemplate.update("DELETE FROM room_category WHERE room_category_id = ?", preservedRoomCategoryId);

        insertRoomCategory(preservedRoomCategoryId, "TDD preserved room category", 90, 1, 18800);
        insertChannelAccount(preservedAccountId, 100L, "TDD preserved channel", "tdd-preserved", "TDD-PRESERVED", "authorized");
        insertChannelRoomCategoryRel(preservedRelId, preservedAccountId, preservedRoomCategoryId, "on_shelf", "approved");

        resetRoomCategoryStatuses();

        assertThat(countRows("SELECT COUNT(1) FROM room_category WHERE room_category_id = ?", preservedRoomCategoryId))
                .isEqualTo(1);
        assertThat(countRows("SELECT COUNT(1) FROM channel_account WHERE account_id = ?", preservedAccountId))
                .isEqualTo(1);
        assertThat(countRows("SELECT COUNT(1) FROM channel_room_category_rel WHERE id = ?", preservedRelId))
                .isEqualTo(1);
    }

    private void seedRoomCategoryStatuses() {
        resetRoomCategoryStatuses();

        insertRoomCategory(126001L, "TDD状态标准大床房", 10, 5, 19900);
        insertRoomCategory(126002L, "TDD状态豪华双床房", 20, 5, 39900);

        insertChannelAccount(126301L, 100L, "宿银平台", "tdd-suyin", "SUYIN-TDD-A", "authorized");

        insertChannelRoomCategoryRel(126401L, 126301L, 126001L, "on_shelf", "approved");
        insertChannelRoomCategoryRel(126402L, 126301L, 126002L, "on_shelf", "approved");

        insertGoodsMain(126501L, "TDD状态标准大床房<无早>", 19900, "published");
        insertGoodsMain(126502L, "TDD状态豪华双床房<双早>", 39900, "published");
        insertGoodsRoomCategoryRel(126601L, 126501L, 126001L);
        insertGoodsRoomCategoryRel(126602L, 126502L, 126002L);
        insertDistributionProduct(126701L, 126501L, 1, 0.10d);
        insertDistributionProduct(126702L, 126502L, 1, 0.12d);
    }

    private void resetRoomCategoryStatuses() {
        jdbcTemplate.update("""
                DELETE og
                FROM order_guest og
                JOIN order_main om ON om.order_id = og.order_id
                WHERE om.camp_id = ?
                  AND (
                    om.room_category_id IN (126001, 126002, 126003)
                    OR om.room_id IN (126101, 126102, 126103)
                  )
                """, CAMP_ID);
        jdbcTemplate.update("""
                DELETE FROM order_main
                WHERE camp_id = ?
                  AND (
                    room_category_id IN (126001, 126002, 126003)
                    OR room_id IN (126101, 126102, 126103)
                  )
                """, CAMP_ID);
        jdbcTemplate.update("""
                DELETE FROM room_price_snapshot
                WHERE camp_id = ?
                  AND room_category_id IN (126001, 126002, 126003)
                """, CAMP_ID);
        jdbcTemplate.update("""
                DELETE FROM room_status_daily
                WHERE camp_id = ?
                  AND room_category_id IN (126001, 126002, 126003)
                """, CAMP_ID);
        jdbcTemplate.update("""
                DELETE FROM room_status_block
                WHERE camp_id = ?
                  AND room_category_id IN (126001, 126002, 126003)
                """, CAMP_ID);
        jdbcTemplate.update("""
                DELETE FROM room
                WHERE camp_id = ?
                  AND (
                    room_category_id IN (126001, 126002, 126003)
                    OR room_id IN (126101, 126102, 126103)
                  )
                """, CAMP_ID);
        jdbcTemplate.update("""
                DELETE FROM goods_room_category_rel
                WHERE goods_id IN (126501, 126502)
                   OR room_category_id IN (126001, 126002, 126003)
                """);
        jdbcTemplate.update("""
                DELETE FROM distribution_product
                WHERE camp_id = ?
                  AND (
                    distribution_product_id IN (126701, 126702)
                    OR goods_id IN (126501, 126502)
                    OR room_category_id IN (126001, 126002, 126003)
                  )
                """, CAMP_ID);
        jdbcTemplate.update("""
                DELETE FROM goods_sku
                WHERE camp_id = ?
                  AND goods_id IN (126501, 126502)
                """, CAMP_ID);
        jdbcTemplate.update("""
                DELETE FROM goods_main
                WHERE camp_id = ?
                  AND goods_id IN (126501, 126502)
                """, CAMP_ID);
        jdbcTemplate.update("""
                DELETE FROM channel_room_category_rel
                WHERE camp_id = ?
                  AND (
                    id IN (126401, 126402)
                    OR account_id = 126301
                    OR room_category_id IN (126001, 126002, 126003)
                  )
                """, CAMP_ID);
        jdbcTemplate.update("""
                DELETE FROM channel_poi_rel
                WHERE camp_id = ?
                  AND account_id = 126301
                """, CAMP_ID);
        jdbcTemplate.update("""
                DELETE FROM channel_account
                WHERE camp_id = ?
                  AND account_id = 126301
                """, CAMP_ID);
        jdbcTemplate.update("""
                DELETE FROM room_category
                WHERE camp_id = ?
                  AND room_category_id IN (126001, 126002, 126003)
                """, CAMP_ID);
    }

    private void resetCentralSaleStatusRoomFixtures() {
        jdbcTemplate.update("""
                DELETE og
                FROM order_guest og
                JOIN order_main om ON om.order_id = og.order_id
                WHERE om.camp_id = ?
                  AND om.room_id IN (?, ?, ?)
                """, CAMP_ID, CENTRAL_ROOM_ID_A, CENTRAL_ROOM_ID_B, CENTRAL_ROOM_ID_C);
        jdbcTemplate.update("""
                DELETE FROM order_main
                WHERE camp_id = ?
                  AND room_id IN (?, ?, ?)
                """, CAMP_ID, CENTRAL_ROOM_ID_A, CENTRAL_ROOM_ID_B, CENTRAL_ROOM_ID_C);
        jdbcTemplate.update("""
                DELETE FROM room_status_block
                WHERE camp_id = ?
                  AND room_category_id = ?
                  AND biz_date >= '2026-05-18'
                  AND biz_date < '2026-05-21'
                """, CAMP_ID, CENTRAL_ROOM_CATEGORY_ID);
        jdbcTemplate.update("""
                DELETE FROM room_status_daily
                WHERE camp_id = ?
                  AND room_category_id = ?
                  AND biz_date >= '2026-05-18'
                  AND biz_date < '2026-05-21'
                """, CAMP_ID, CENTRAL_ROOM_CATEGORY_ID);
        jdbcTemplate.update("""
                DELETE FROM room
                WHERE camp_id = ?
                  AND room_id IN (?, ?, ?)
                """, CAMP_ID, CENTRAL_ROOM_ID_A, CENTRAL_ROOM_ID_B, CENTRAL_ROOM_ID_C);
    }

    private void insertRoom(long roomId, String roomName) {
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
                CAMP_ID,
                POI_ID,
                CENTRAL_ROOM_CATEGORY_ID,
                roomName,
                "online",
                "normal",
                "clean",
                1,
                1,
                0
        );
    }

    private void insertRoomStatusDaily(
            long id,
            String bizDate,
            int availabilityCount,
            int openRoomCount,
            int vacantCount,
            int closeRoomCount
    ) {
        jdbcTemplate.update("""
                        INSERT INTO room_status_daily (
                            id,
                            camp_id,
                            poi_id,
                            biz_date,
                            room_category_id,
                            availability_count,
                            open_room_count,
                            room_sale_count,
                            close_room_count,
                            user_busy_count,
                            retain_count,
                            repair_count,
                            vacant_count,
                            pre_come_count,
                            live_count,
                            pre_leave_count,
                            clean_count,
                            dirty_count
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                id,
                CAMP_ID,
                POI_ID,
                bizDate,
                CENTRAL_ROOM_CATEGORY_ID,
                availabilityCount,
                openRoomCount,
                openRoomCount,
                closeRoomCount,
                0,
                0,
                0,
                vacantCount,
                0,
                openRoomCount,
                0,
                availabilityCount,
                0
        );
    }

    private void insertClosedBlock(long blockId, long roomId, String bizDate, String reason) {
        jdbcTemplate.update("""
                        INSERT INTO room_status_block (
                            block_id,
                            camp_id,
                            poi_id,
                            biz_date,
                            room_category_id,
                            room_id,
                            reason,
                            status,
                            created_by,
                            updated_by,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, 'closed', ?, ?, 0)
                        """,
                blockId,
                CAMP_ID,
                POI_ID,
                bizDate,
                CENTRAL_ROOM_CATEGORY_ID,
                roomId,
                reason,
                12001L,
                12001L
        );
    }

    private void insertOrderMain(long orderId, long roomId, String startAt, String endAt) {
        jdbcTemplate.update("""
                        INSERT INTO order_main (
                            order_id,
                            camp_id,
                            poi_id,
                            room_category_id,
                            room_id,
                            order_no,
                            order_type,
                            status,
                            guest_name,
                            start_at,
                            end_at,
                            day_num,
                            total_price_cent,
                            total_pay_price_cent,
                            is_deleted,
                            version_no
                        ) VALUES (?, ?, ?, ?, ?, ?, 'daily_room', 'booked', ?, ?, ?, 2, 39800, 39800, 0, 0)
                        """,
                orderId,
                CAMP_ID,
                POI_ID,
                CENTRAL_ROOM_CATEGORY_ID,
                roomId,
                "ORDER-CENTRAL-STOCK-" + orderId,
                "central stock guest",
                startAt,
                endAt
        );
    }

    private int countActiveBlocks(String reason) {
        Integer value = jdbcTemplate.queryForObject("""
                        SELECT COUNT(1)
                        FROM room_status_block
                        WHERE camp_id = ?
                          AND room_category_id = ?
                          AND biz_date = '2026-05-18'
                          AND reason = ?
                          AND status = 'closed'
                          AND is_deleted = 0
                        """,
                Integer.class,
                CAMP_ID,
                CENTRAL_ROOM_CATEGORY_ID,
                reason
        );
        return value == null ? 0 : value;
    }

    private int queryDailyCounter(String columnName) {
        Integer value = jdbcTemplate.queryForObject(
                "SELECT " + columnName + " FROM room_status_daily WHERE camp_id = ? AND room_category_id = ? AND biz_date = '2026-05-18'",
                Integer.class,
                CAMP_ID,
                CENTRAL_ROOM_CATEGORY_ID
        );
        return value == null ? 0 : value;
    }

    private int countRows(String sql, Object... args) {
        Integer value = jdbcTemplate.queryForObject(sql, Integer.class, args);
        return value == null ? 0 : value;
    }

    private void insertRoomCategory(long roomCategoryId, String name, int sortNo, int roomCount, long priceCent) {
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
                roomCategoryId,
                CAMP_ID,
                11001L,
                name,
                name,
                roomCount,
                2,
                priceCent,
                priceCent,
                priceCent,
                14,
                23,
                12,
                name + "亮点",
                name + "周边",
                name + "图文",
                sortNo,
                1,
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
