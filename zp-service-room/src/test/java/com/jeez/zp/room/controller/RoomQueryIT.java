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

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RoomQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;
    private static final long POI_ID = 11001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void roomsGet_shouldReturnGroupedRoomsForRequestedCategories() throws Exception {
        insertRoomCategory(102501L, "TDD房型A", 10);
        insertRoomCategory(102502L, "TDD房型B", 20);
        insertFloor(102601L, "9F", 9);
        insertFloor(102602L, "10F", 10);
        insertRoom(102701L, 102501L, 102601L, "A-101", "online", "normal", "clean", 1, 1, 0);
        insertRoom(102702L, 102501L, 102601L, "A-102", "online", "normal", "dirty", 2, 1, 0);
        insertRoom(102703L, 102501L, 102602L, "A-103", "offline", "hourly", "dirty", 3, 1, 0);
        insertRoom(102704L, 102501L, 102602L, "A-104", "online", "normal", "clean", 4, 0, 0);
        insertRoom(102705L, 102502L, 102602L, "B-201", "online", "normal", "clean", 1, 1, 0);

        mockMvc.perform(post("/rooms/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "roomCategoryIds":["102502","102501"],
                                  "saleType":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.roomCategoryRooms.length()").value(2))
                .andExpect(jsonPath("$.data.roomCategoryRooms[0].roomCategoryId").value("102501"))
                .andExpect(jsonPath("$.data.roomCategoryRooms[0].roomCategoryName").value("TDD房型A"))
                .andExpect(jsonPath("$.data.roomCategoryRooms[0].rooms.length()").value(2))
                .andExpect(jsonPath("$.data.roomCategoryRooms[0].rooms[0].roomId").value("102701"))
                .andExpect(jsonPath("$.data.roomCategoryRooms[0].rooms[0].roomName").value("A-101"))
                .andExpect(jsonPath("$.data.roomCategoryRooms[0].rooms[0].floorId").value("102601"))
                .andExpect(jsonPath("$.data.roomCategoryRooms[0].rooms[0].floorName").value("9F"))
                .andExpect(jsonPath("$.data.roomCategoryRooms[0].rooms[0].seq").value(1))
                .andExpect(jsonPath("$.data.roomCategoryRooms[0].rooms[0].deviceViews.length()").value(0))
                .andExpect(jsonPath("$.data.roomCategoryRooms[0].rooms[*].roomId", not(hasItem("102703"))))
                .andExpect(jsonPath("$.data.roomCategoryRooms[0].rooms[*].roomId", not(hasItem("102704"))))
                .andExpect(jsonPath("$.data.roomCategoryRooms[1].roomCategoryId").value("102502"))
                .andExpect(jsonPath("$.data.roomCategoryRooms[1].rooms[0].roomId").value("102705"));
    }

    @Test
    @Timeout(60)
    void roomsGet_shouldFallbackCurrentCampWhenCampIdBlank() throws Exception {
        insertRoomCategory(102801L, "TDD回退房型", 30);
        insertFloor(102811L, "11F", 11);
        insertRoom(102821L, 102801L, 102811L, "F-1101", "online", "normal", "clean", 1, 1, 0);

        mockMvc.perform(post("/rooms/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"",
                                  "roomCategoryIds":["102801"],
                                  "saleType":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.roomCategoryRooms.length()").value(1))
                .andExpect(jsonPath("$.data.roomCategoryRooms[0].roomCategoryId").value("102801"))
                .andExpect(jsonPath("$.data.roomCategoryRooms[0].rooms[0].roomId").value("102821"));
    }

    @Test
    @Timeout(60)
    void roomsGet_shouldRejectForeignCampAccess() throws Exception {
        mockMvc.perform(post("/rooms/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "roomCategoryIds":["102501"],
                                  "saleType":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @Timeout(60)
    void roomsPageGet_shouldReturnFlatPagedRoomOptionsForFrontendFilters() throws Exception {
        insertRoomCategory(102901L, "TDD房型筛选A", 10);
        insertRoomCategory(102902L, "TDD房型筛选B", 20);
        insertFloor(102911L, "12F", 12);
        insertRoom(102921L, 102901L, 102911L, "TDD-1201", "online", "normal", "clean", 1, 1, 0);
        insertRoom(102922L, 102901L, 102911L, "TDD-1202", "online", "normal", "dirty", 2, 1, 0);
        insertRoom(102923L, 102902L, 102911L, "OTHER-1203", "online", "normal", "clean", 3, 1, 0);
        insertRoom(102924L, 102901L, 102911L, "TDD-OFFLINE", "offline", "normal", "clean", 4, 1, 0);
        insertRoom(102925L, 102901L, 102911L, "TDD-DELETED", "online", "normal", "clean", 5, 0, 0);

        mockMvc.perform(post("/rooms/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "isAvailability":1,
                                  "pageNum":1,
                                  "pageSize":1,
                                  "saleType":1,
                                  "keyword":"TDD-12"
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
                .andExpect(jsonPath("$.data.pageSize").value(1))
                .andExpect(jsonPath("$.data.hasNextPage").value(true))
                .andExpect(jsonPath("$.data.pages").value(2))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].roomId").value("102921"))
                .andExpect(jsonPath("$.data.list[0].id").value("102921"))
                .andExpect(jsonPath("$.data.list[0].roomName").value("TDD-1201"))
                .andExpect(jsonPath("$.data.list[0].name").value("TDD-1201"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryId").value("102901"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryName").value("TDD房型筛选A"))
                .andExpect(jsonPath("$.data.list[0].floorId").value("102911"))
                .andExpect(jsonPath("$.data.list[0].floorName").value("12F"));
    }

    @Test
    @Timeout(60)
    void roomStatusesRoomsGet_shouldReturnMonthlyRoomCategoryRoomsShape() throws Exception {
        insertRoomCategory(103001L, "TDD月房态A", 10);
        insertRoomCategory(103002L, "TDD月房态B", 20);
        insertFloor(103011L, "13F", 13);
        insertRoom(103021L, 103001L, 103011L, "M-1301", "online", "normal", "clean", 1, 1, 0);
        insertRoom(103022L, 103001L, 103011L, "M-1302", "online", "normal", "dirty", 2, 1, 0);
        insertRoom(103023L, 103002L, 103011L, "M-2301", "online", "normal", "clean", 1, 1, 0);
        insertRoom(103024L, 103001L, 103011L, "M-OFFLINE", "offline", "normal", "clean", 3, 1, 0);
        insertRoomStatusDaily(103031L, 103001L, "2026-05-20", 2);
        insertRoomStatusDaily(103032L, 103002L, "2026-05-20", 1);

        mockMvc.perform(post("/roomStatuses/rooms/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "startDate":"2026-05-20",
                                  "days":7,
                                  "roomCategoryIds":["103001"],
                                  "page":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.isSingleInventory").value(0))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].campId").value("10001"))
                .andExpect(jsonPath("$.data.list[0].storeId").value("11001"))
                .andExpect(jsonPath("$.data.list[0].storeName").exists())
                .andExpect(jsonPath("$.data.list[0].roomCategoryId").value("103001"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryName").value("TDD月房态A"))
                .andExpect(jsonPath("$.data.list[0].rooms.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].rooms[0].roomId").value("103021"))
                .andExpect(jsonPath("$.data.list[0].rooms[0].roomName").value("M-1301"))
                .andExpect(jsonPath("$.data.list[0].rooms[1].roomId").value("103022"))
                .andExpect(jsonPath("$.data.pagination.page").value(1))
                .andExpect(jsonPath("$.data.pagination.pageSize").value(20))
                .andExpect(jsonPath("$.data.pagination.total").value(1));
    }

    @Test
    @Timeout(60)
    void roomStatusesRoomsGet_shouldKeepOccupiedRoomsInRequestedDateRange() throws Exception {
        insertRoomCategory(103101L, "TDD可用房过滤", 10);
        insertFloor(103111L, "14F", 14);
        insertRoom(103121L, 103101L, 103111L, "OCC-1401", "online", "normal", "clean", 1, 1, 0);
        insertRoom(103122L, 103101L, 103111L, "AVL-1402", "online", "normal", "clean", 2, 1, 0);
        insertOccupiedOrder(
                103131L,
                103101L,
                103121L,
                LocalDateTime.of(2026, 6, 10, 14, 0),
                LocalDateTime.of(2026, 6, 11, 12, 0)
        );

        mockMvc.perform(post("/roomStatuses/rooms/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "startDate":"2026-06-10",
                                  "days":1,
                                  "roomCategoryIds":["103101"],
                                  "page":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].roomCategoryId").value("103101"))
                .andExpect(jsonPath("$.data.list[0].rooms.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].rooms[0].roomId").value("103121"))
                .andExpect(jsonPath("$.data.list[0].rooms[0].roomName").value("OCC-1401"))
                .andExpect(jsonPath("$.data.list[0].rooms[1].roomId").value("103122"))
                .andExpect(jsonPath("$.data.list[0].rooms[1].roomName").value("AVL-1402"));
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
                        """,
                roomCategoryId,
                CAMP_ID,
                POI_ID,
                name,
                name,
                0,
                1,
                sortNo,
                0
        );
    }

    private void insertFloor(long floorId, String floorName, int sortNo) {
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
                POI_ID,
                floorName,
                sortNo,
                1,
                0
        );
    }

    private void insertRoom(
            long roomId,
            long roomCategoryId,
            long floorId,
            String roomName,
            String lockStatus,
            String saleType,
            String cleanStatus,
            int sortNo,
            int status,
            int isDeleted
    ) {
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
                lockStatus,
                saleType,
                cleanStatus,
                status,
                sortNo,
                isDeleted
        );
    }
    private void insertRoomStatusDaily(long id, long roomCategoryId, String bizDate, int availabilityCount) {
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
                roomCategoryId,
                availabilityCount,
                0,
                0,
                0,
                0,
                0,
                0,
                availabilityCount,
                0,
                0,
                0,
                availabilityCount,
                0
        );
    }

    private void insertOccupiedOrder(
            long orderId,
            long roomCategoryId,
            long roomId,
            LocalDateTime startAt,
            LocalDateTime endAt
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
                            remark,
                            created_at,
                            updated_at,
                            created_by,
                            updated_by,
                            is_deleted,
                            version_no
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), ?, ?, 0, 0)
                        """,
                orderId,
                CAMP_ID,
                POI_ID,
                roomCategoryId,
                roomId,
                null,
                null,
                "ROOM-STATUS-" + orderId,
                "ROOM-STATUS-OUT-" + orderId,
                "daily_room",
                "booked",
                "Occupied Guest",
                "13910313100",
                Timestamp.valueOf(startAt),
                Timestamp.valueOf(endAt),
                1,
                28800,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                "unpaid",
                17101L,
                17202L,
                "frontdesk",
                "occupied room filter test",
                12001L,
                12001L
        );
    }
}

