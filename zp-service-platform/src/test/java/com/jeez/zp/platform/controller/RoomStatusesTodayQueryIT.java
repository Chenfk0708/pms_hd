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
import java.time.ZoneId;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RoomStatusesTodayQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;
    private static final long POI_ID = 99121L;
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    private static final LocalDate TARGET_DATE = LocalDate.of(2026, 5, 18);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void roomStatusesTodayGet_shouldReturnRoomTypeViewUsingLiveLegacyShape() throws Exception {
        seedRoomStatusesTodayScene();

        mockMvc.perform(post("/roomStatusesToday/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "poiIds":["99121"],
                                  "date":1779033600000,
                                  "queryCode":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.errorMsg").doesNotExist())
                .andExpect(jsonPath("$.data.basic.preComeNum").value(1))
                .andExpect(jsonPath("$.data.basic.liveNum").value(2))
                .andExpect(jsonPath("$.data.basic.preLeaveNum").value(1))
                .andExpect(jsonPath("$.data.basic.orderRemarkNum").value(1))
                .andExpect(jsonPath("$.data.basic.roomNum").value(4))
                .andExpect(jsonPath("$.data.basic.soldNum").value(1))
                .andExpect(jsonPath("$.data.basic.idleNum").value(1))
                .andExpect(jsonPath("$.data.basic.occNum").value(3))
                .andExpect(jsonPath("$.data.basic.idleDirtyNum").value(1))
                .andExpect(jsonPath("$.data.basic.liveCleanNum").value(1))
                .andExpect(jsonPath("$.data.basic.liveDirtyNum").value(1))
                .andExpect(jsonPath("$.data.roomCategories.length()").value(2))
                .andExpect(jsonPath("$.data.roomCategories[0].roomCategoryId").value("99221"))
                .andExpect(jsonPath("$.data.roomCategories[0].roomCategoryName").value("TDD今日房态A"))
                .andExpect(jsonPath("$.data.roomCategories[0].roomNum").value(2))
                .andExpect(jsonPath("$.data.roomCategories[0].soldNum").value(1))
                .andExpect(jsonPath("$.data.roomCategories[0].liveNum").value(1))
                .andExpect(jsonPath("$.data.roomCategories[0].occNum").value(2))
                .andExpect(jsonPath("$.data.roomCategories[0].rooms[0].roomId").value("99421"))
                .andExpect(jsonPath("$.data.roomCategories[0].rooms[0].roomName").value("A-901"))
                .andExpect(jsonPath("$.data.roomCategories[0].rooms[0].isDirty").value(0))
                .andExpect(jsonPath("$.data.roomCategories[0].rooms[0].isOcc").value(1))
                .andExpect(jsonPath("$.data.roomCategories[0].rooms[0].isLive").value(0))
                .andExpect(jsonPath("$.data.roomCategories[0].rooms[0].isPreCome").value(1))
                .andExpect(jsonPath("$.data.roomCategories[0].rooms[0].isOrderRemark").value(1))
                .andExpect(jsonPath("$.data.roomCategories[0].rooms[0].guestName").value("张三"))
                .andExpect(jsonPath("$.data.roomCategories[0].rooms[0].orders.length()").value(1))
                .andExpect(jsonPath("$.data.roomCategories[0].rooms[0].orders[0].orderId").value("99621"))
                .andExpect(jsonPath("$.data.roomCategories[0].rooms[0].orders[0].channelName").value("路客云聚合"))
                .andExpect(jsonPath("$.data.roomCategories[1].roomCategoryId").value("99222"))
                .andExpect(jsonPath("$.data.roomCategories[1].roomNum").value(2))
                .andExpect(jsonPath("$.data.roomCategories[1].idleNum").value(1))
                .andExpect(jsonPath("$.data.roomCategories[1].occNum").value(1))
                .andExpect(jsonPath("$.data.roomViews.length()").value(0))
                .andExpect(jsonPath("$.data.floorViews.length()").value(0))
                .andExpect(jsonPath("$.data.isInitFloor").doesNotExist());
    }

    @Test
    @Timeout(60)
    void roomStatusesTodayGet_shouldSupportCurrentFrontendViewModeKeywordAndStatusFilters() throws Exception {
        seedRoomStatusesTodayScene();

        mockMvc.perform(post("/roomStatusesToday/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "storeId":"99121",
                                  "date":"2026-05-18",
                                  "viewMode":"按房间号",
                                  "keyword":"张三",
                                  "statusFilters":["预抵"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.roomCategories.length()").value(0))
                .andExpect(jsonPath("$.data.floorViews.length()").value(0))
                .andExpect(jsonPath("$.data.roomViews.length()").value(1))
                .andExpect(jsonPath("$.data.roomViews[0].roomId").value("99421"))
                .andExpect(jsonPath("$.data.roomViews[0].roomName").value("A-901"))
                .andExpect(jsonPath("$.data.roomViews[0].isPreCome").value(1))
                .andExpect(jsonPath("$.data.roomViews[0].guestName").value("张三"))
                .andExpect(jsonPath("$.data.roomViews[0].orders[0].orderId").value("99621"));
    }

    @Test
    @Timeout(60)
    void roomStatusesTodayGet_shouldFallbackToSourceLabelSnapshotWhenChannelAccountMissing() throws Exception {
        seedRoomStatusesTodayScene();
        insertOrderMain(99624L, null, 99222L, 99424L, "booked", "paid", "SNAPSHOT-GUEST", "13900000007",
                TARGET_DATE, TARGET_DATE.plusDays(1), TARGET_DATE.minusDays(1).atTime(7, 0), null, "Snapshot OTA");

        mockMvc.perform(post("/roomStatusesToday/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "storeId":"99121",
                                  "date":"2026-05-18",
                                  "viewMode":"按房间号",
                                  "keyword":"SNAPSHOT-GUEST"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.roomViews.length()").value(1))
                .andExpect(jsonPath("$.data.roomViews[0].roomId").value("99424"))
                .andExpect(jsonPath("$.data.roomViews[0].guestName").value("SNAPSHOT-GUEST"))
                .andExpect(jsonPath("$.data.roomViews[0].orders[0].orderId").value("99624"))
                .andExpect(jsonPath("$.data.roomViews[0].orders[0].channelName").value("Snapshot OTA"));
    }

    @Test
    @Timeout(60)
    void roomStatusesTodayGet_shouldFallbackInvalidCampIdForFloorViewAndRejectForeignCamp() throws Exception {
        seedRoomStatusesTodayScene();

        mockMvc.perform(post("/roomStatusesToday/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"mock-camp-main",
                                  "poiIds":["99121"],
                                  "date":"2026-05-18",
                                  "viewMode":"按楼层"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.roomCategories.length()").value(0))
                .andExpect(jsonPath("$.data.roomViews.length()").value(0))
                .andExpect(jsonPath("$.data.isInitFloor").value(1))
                .andExpect(jsonPath("$.data.floorViews.length()").value(2))
                .andExpect(jsonPath("$.data.floorViews[0].floorId").value("99321"))
                .andExpect(jsonPath("$.data.floorViews[0].floorName").value("9F"))
                .andExpect(jsonPath("$.data.floorViews[0].rooms.length()").value(2))
                .andExpect(jsonPath("$.data.floorViews[1].floorId").value("99322"))
                .andExpect(jsonPath("$.data.floorViews[1].floorName").value("10F"))
                .andExpect(jsonPath("$.data.floorViews[1].rooms.length()").value(2));

        mockMvc.perform(post("/roomStatusesToday/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "queryCode":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.success").value(false));
    }

    private void seedRoomStatusesTodayScene() {
        insertPoi(POI_ID, "TDD今日房态门店", 91);
        insertRoomCategory(99221L, POI_ID, 99301L, "TDD今日房态A", 10);
        insertRoomCategory(99222L, POI_ID, 99301L, "TDD今日房态B", 20);
        insertFloor(99321L, POI_ID, "9F", 9);
        insertFloor(99322L, POI_ID, "10F", 10);
        insertRoom(99421L, 99221L, 99321L, "A-901", "clean", 1);
        insertRoom(99422L, 99221L, 99321L, "A-902", "clean", 2);
        insertRoom(99423L, 99222L, 99322L, "B-1001", "dirty", 1);
        insertRoom(99424L, 99222L, 99322L, "B-1002", "dirty", 2);
        insertChannelAccount(99521L, 17L, "路客云聚合");
        insertChannelAccount(99522L, 5L, "携程");

        insertOrderMain(99621L, 99521L, 99221L, 99421L, "booked", "paid", "张三", "13900000001",
                TARGET_DATE, TARGET_DATE.plusDays(1), TARGET_DATE.minusDays(1).atTime(9, 0), "需无烟房");
        insertOrderMain(99622L, 99522L, 99221L, 99422L, "checked_in", "paid", "李四", "13900000002",
                TARGET_DATE.minusDays(1), TARGET_DATE, TARGET_DATE.minusDays(2).atTime(10, 0), null);
        insertOrderMain(99623L, 99522L, 99222L, 99423L, "checked_in", "paid", "王五", "13900000003",
                TARGET_DATE.minusDays(1), TARGET_DATE.plusDays(1), TARGET_DATE.minusDays(2).atTime(11, 0), null);
    }

    private void insertPoi(long poiId, String poiName, int sortNo) {
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
                poiId,
                CAMP_ID,
                poiName,
                1,
                sortNo,
                1,
                0
        );
    }

    private void insertRoomCategory(long roomCategoryId, long poiId, long groupId, String name, int sortNo) {
        jdbcTemplate.update("""
                        INSERT INTO room_category (
                            room_category_id,
                            camp_id,
                            poi_id,
                            group_id,
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
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                roomCategoryId,
                CAMP_ID,
                poiId,
                groupId,
                name,
                name,
                2,
                2,
                26800L,
                28800L,
                30800L,
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

    private void insertFloor(long floorId, long poiId, String floorName, int sortNo) {
        jdbcTemplate.update("""
                        INSERT INTO room_floor (
                            floor_id,
                            camp_id,
                            poi_id,
                            floor_name,
                            sort_no,
                            status,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                floorId,
                CAMP_ID,
                poiId,
                floorName,
                sortNo,
                1,
                0
        );
    }

    private void insertRoom(long roomId, long roomCategoryId, long floorId, String roomName, String cleanStatus, int sortNo) {
        jdbcTemplate.update("""
                        INSERT INTO room (
                            room_id,
                            camp_id,
                            poi_id,
                            room_category_id,
                            floor_id,
                            room_name,
                            lock_status,
                            sale_type,
                            clean_status,
                            status,
                            sort_no,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                roomId,
                CAMP_ID,
                POI_ID,
                roomCategoryId,
                floorId,
                roomName,
                "online",
                "normal",
                cleanStatus,
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
                CAMP_ID,
                channelId,
                channelName,
                channelName + "账号",
                "OUT-" + accountId,
                "authorized"
        );
    }

    private void insertOrderMain(
            long orderId,
            Long channelAccountId,
            long roomCategoryId,
            long roomId,
            String status,
            String paymentStatus,
            String guestName,
            String guestMobile,
            LocalDate startDate,
            LocalDate endDate,
            LocalDateTime createdAt,
            String remark
    ) {
        insertOrderMain(orderId, channelAccountId, roomCategoryId, roomId, status, paymentStatus, guestName, guestMobile,
                startDate, endDate, createdAt, remark, null);
    }

    private void insertOrderMain(
            long orderId,
            Long channelAccountId,
            long roomCategoryId,
            long roomId,
            String status,
            String paymentStatus,
            String guestName,
            String guestMobile,
            LocalDate startDate,
            LocalDate endDate,
            LocalDateTime createdAt,
            String remark,
            String sourceLabelSnapshot
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
                            source_label_snapshot,
                            remark,
                            created_at,
                            updated_at,
                            created_by,
                            updated_by,
                            is_deleted,
                            version_no
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 0)
                        """,
                orderId,
                CAMP_ID,
                POI_ID,
                roomCategoryId,
                roomId,
                channelAccountId,
                null,
                "ORDER-" + orderId,
                "OUT-ORDER-" + orderId,
                "daily_room",
                status,
                guestName,
                guestMobile,
                Timestamp.valueOf(startDate.atTime(LocalTime.of(14, 0))),
                Timestamp.valueOf(endDate.atTime(LocalTime.of(12, 0))),
                Math.max(1, (int) (endDate.toEpochDay() - startDate.toEpochDay())),
                26800L,
                0L,
                26800L,
                0L,
                2680L,
                0L,
                0L,
                2680L,
                24120L,
                paymentStatus,
                17101L,
                17202L,
                "channel",
                sourceLabelSnapshot,
                remark,
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt.plusMinutes(30)),
                14001L,
                14001L
        );
    }
}
