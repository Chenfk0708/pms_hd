package com.jeez.zp.room.controller;

import org.springframework.jdbc.core.JdbcTemplate;

final class RoomCleanTestCatalogFixture {

    static final long CAMP_ID = 10001L;
    static final long POI_ID = 11001L;
    static final long STANDARD_ROOM_CATEGORY_ID = 22001L;
    static final long DELUXE_ROOM_CATEGORY_ID = 22002L;
    static final long ROOM_102_ID = 23002L;
    static final long ROOM_201_ID = 23003L;
    static final long ROOM_203_ID = 23005L;
    static final String STANDARD_ROOM_CATEGORY_NAME = "标准大床房";
    static final String DELUXE_ROOM_CATEGORY_NAME = "豪华双床房";
    static final String ROOM_102_NAME = "TDD-CLEAN-102";
    static final String ROOM_201_NAME = "TDD-CLEAN-201";
    static final String ROOM_203_NAME = "TDD-CLEAN-203";

    private RoomCleanTestCatalogFixture() {
    }

    static void ensureBaseCatalog(JdbcTemplate jdbcTemplate) {
        upsertRoomCategory(jdbcTemplate, STANDARD_ROOM_CATEGORY_ID, STANDARD_ROOM_CATEGORY_NAME, 10, 2);
        upsertRoomCategory(jdbcTemplate, DELUXE_ROOM_CATEGORY_ID, DELUXE_ROOM_CATEGORY_NAME, 20, 1);
        upsertRoom(jdbcTemplate, ROOM_102_ID, STANDARD_ROOM_CATEGORY_ID, ROOM_102_NAME, 10);
        upsertRoom(jdbcTemplate, ROOM_201_ID, STANDARD_ROOM_CATEGORY_ID, ROOM_201_NAME, 20);
        upsertRoom(jdbcTemplate, ROOM_203_ID, DELUXE_ROOM_CATEGORY_ID, ROOM_203_NAME, 30);
    }

    private static void upsertRoomCategory(
            JdbcTemplate jdbcTemplate,
            long roomCategoryId,
            String name,
            int sortNo,
            int roomCount
    ) {
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
