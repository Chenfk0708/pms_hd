package com.jeez.zp.order.controller;

import org.springframework.jdbc.core.JdbcTemplate;

final class OrderTestCatalogFixture {

    static final long CAMP_ID = 10001L;
    static final long POI_ID = 11001L;
    static final long STANDARD_ROOM_CATEGORY_ID = 22001L;
    static final long DELUXE_ROOM_CATEGORY_ID = 22002L;
    static final long STANDARD_ROOM_ID = 23001L;
    static final long STANDARD_CHANGE_ROOM_ID = 23002L;
    static final long STANDARD_OCCUPIED_ROOM_ID = 23003L;
    static final long DELUXE_ROOM_ID = 23004L;
    static final String STANDARD_ROOM_CATEGORY_NAME = "标准大床房";
    static final String DELUXE_ROOM_CATEGORY_NAME = "豪华双床房";
    static final String STANDARD_ROOM_NAME = "TDD-ORDER-101";
    static final String STANDARD_CHANGE_ROOM_NAME = "TDD-ORDER-102";
    static final String STANDARD_OCCUPIED_ROOM_NAME = "TDD-ORDER-103";
    static final String DELUXE_ROOM_NAME = "TDD-ORDER-201";

    private OrderTestCatalogFixture() {
    }

    static void ensureBaseCatalog(JdbcTemplate jdbcTemplate) {
        upsertRoomCategory(jdbcTemplate, STANDARD_ROOM_CATEGORY_ID, STANDARD_ROOM_CATEGORY_NAME, 10, 3);
        upsertRoomCategory(jdbcTemplate, DELUXE_ROOM_CATEGORY_ID, DELUXE_ROOM_CATEGORY_NAME, 20);
        upsertRoom(jdbcTemplate, STANDARD_ROOM_ID, STANDARD_ROOM_CATEGORY_ID, STANDARD_ROOM_NAME, 10);
        upsertRoom(jdbcTemplate, STANDARD_CHANGE_ROOM_ID, STANDARD_ROOM_CATEGORY_ID, STANDARD_CHANGE_ROOM_NAME, 20);
        upsertRoom(jdbcTemplate, STANDARD_OCCUPIED_ROOM_ID, STANDARD_ROOM_CATEGORY_ID, STANDARD_OCCUPIED_ROOM_NAME, 30);
        upsertRoom(jdbcTemplate, DELUXE_ROOM_ID, DELUXE_ROOM_CATEGORY_ID, DELUXE_ROOM_NAME, 40);
    }

    private static void upsertRoomCategory(JdbcTemplate jdbcTemplate, long roomCategoryId, String name, int sortNo) {
        upsertRoomCategory(jdbcTemplate, roomCategoryId, name, sortNo, 1);
    }

    private static void upsertRoomCategory(JdbcTemplate jdbcTemplate, long roomCategoryId, String name, int sortNo, int roomCount) {
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
                CAMP_ID,
                POI_ID,
                name,
                name,
                roomCount,
                1,
                sortNo,
                0
        );
    }

    private static void upsertRoom(JdbcTemplate jdbcTemplate, long roomId, long roomCategoryId, String roomName, int sortNo) {
        jdbcTemplate.update("""
                        INSERT INTO room (
                            room_id,
                            camp_id,
                            poi_id,
                            room_category_id,
                            room_name,
                            room_no,
                            lock_status,
                            sale_type,
                            clean_status,
                            status,
                            sort_no,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE
                            camp_id = VALUES(camp_id),
                            poi_id = VALUES(poi_id),
                            room_category_id = VALUES(room_category_id),
                            room_name = VALUES(room_name),
                            room_no = VALUES(room_no),
                            lock_status = VALUES(lock_status),
                            sale_type = VALUES(sale_type),
                            clean_status = VALUES(clean_status),
                            status = VALUES(status),
                            sort_no = VALUES(sort_no),
                            is_deleted = VALUES(is_deleted)
                        """,
                roomId,
                CAMP_ID,
                POI_ID,
                roomCategoryId,
                roomName,
                roomName,
                "online",
                "normal",
                "clean",
                1,
                sortNo,
                0
        );
    }
}
