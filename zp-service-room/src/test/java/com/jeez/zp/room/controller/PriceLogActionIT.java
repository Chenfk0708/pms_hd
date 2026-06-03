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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PriceLogActionIT {

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
    void houseManagePriceList_shouldReturnHudsonEnvelopeAndSupportCurrentFrontendFilters() throws Exception {
        seedPriceLogs();

        mockMvc.perform(post("/houseManage/logs/price/list")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "keyword":"TDD-PriceLog",
                                  "adjustType":"manual",
                                  "channelId":"0",
                                  "adjustmentStart":"2026-05-14",
                                  "adjustmentEnd":"2026-05-15",
                                  "operationStart":"2026-05-14",
                                  "operationEnd":"2026-05-15",
                                  "operator":"\u7cfb\u7edf",
                                  "page":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pagination.page").value(1))
                .andExpect(jsonPath("$.data.pagination.pageSize").value(20))
                .andExpect(jsonPath("$.data.pagination.total").value(2))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].logId").value("PL989902"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryName").value("TDD-PriceLog-A-989901"))
                .andExpect(jsonPath("$.data.list[0].priceDate").value("2026-05-15"))
                .andExpect(jsonPath("$.data.list[0].actionContent").value("\u96f6\u552e\u4ef7\u4ece 268.00 \u8c03\u6574\u4e3a 298.00"))
                .andExpect(jsonPath("$.data.list[0].adjustTypeName").value("\u624b\u52a8\u8c03\u6574"))
                .andExpect(jsonPath("$.data.list[0].channelName").value("\u81ea\u6765\u5ba2"))
                .andExpect(jsonPath("$.data.list[0].channelSalePrice").value(29800))
                .andExpect(jsonPath("$.data.list[0].operatorName").value("\u7cfb\u7edf\u540c\u6b65"))
                .andExpect(jsonPath("$.data.list[0].operationTime").value("2026-05-15 10:04:12"))
                .andExpect(jsonPath("$.data.dictionaries.channels[0].label").value("\u81ea\u6765\u5ba2"))
                .andExpect(jsonPath("$.data.dictionaries.adjustmentModes[0].value").value("manual"));
    }

    @Test
    @Timeout(60)
    void houseManagePriceExport_shouldReturnCsvMetadataAndFilteredRows() throws Exception {
        seedPriceLogs();

        mockMvc.perform(post("/houseManage/logs/price/export")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"",
                                  "keyword":"TDD-PriceLog",
                                  "adjustType":"system",
                                  "channelId":"17",
                                  "adjustmentStart":"2026-05-15",
                                  "adjustmentEnd":"2026-05-15",
                                  "page":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.fileName").value("price_logs_2026-05-15.csv"))
                .andExpect(jsonPath("$.data.contentType").value("text/csv"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.rows[0].logId").value("PL989903"))
                .andExpect(jsonPath("$.data.rows[0].adjustTypeName").value("\u7cfb\u7edf\u8c03\u6574"))
                .andExpect(jsonPath("$.data.rows[0].channelName").value("\u8def\u5ba2\u4e91\u805a\u5408"));
    }

    @Test
    @Timeout(60)
    void houseManagePriceList_shouldRejectForeignCampAccess() throws Exception {
        seedPriceLogs();

        mockMvc.perform(post("/houseManage/logs/price/list")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "page":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.errorMsg").value("\u65e0\u6743\u8bbf\u95ee\u5f53\u524d\u95e8\u5e97\u8c03\u4ef7\u65e5\u5fd7"));
    }

    private void seedPriceLogs() {
        resetSeedData();
        insertRoomCategory(229901L, "TDD-PriceLog-A-989901", 11);
        insertRoomCategory(229902L, "TDD-PriceLog-B-989903", 12);
        insertChannelAccount(299901L, 17L, "\u8def\u5ba2\u4e91\u805a\u5408", "tdd-price-log-channel", "PRICE-LOG-CHANNEL-17");
        insertPriceSnapshot(989901L, 229901L, "2026-05-14", "retail", 0L, 26800L, "2026-05-14 09:18:26");
        insertPriceSnapshot(989902L, 229901L, "2026-05-15", "retail", 0L, 29800L, "2026-05-15 10:04:12");
        insertPriceSnapshot(989903L, 229902L, "2026-05-15", "channel", 17L, 38800L, "2026-05-15 11:20:33");
    }

    private void resetSeedData() {
        jdbcTemplate.update("DELETE FROM room_price_snapshot WHERE id IN (?, ?, ?)", 989901L, 989902L, 989903L);
        jdbcTemplate.update("DELETE FROM channel_account WHERE account_id = ?", 299901L);
        jdbcTemplate.update("DELETE FROM room_category WHERE room_category_id IN (?, ?)", 229901L, 229902L);
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
                POI_ID,
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
                name + "-highlight",
                name + "-nearby",
                name + "-article",
                sortNo,
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

    private void insertPriceSnapshot(
            long id,
            long roomCategoryId,
            String bizDate,
            String priceType,
            long channelId,
            long priceCent,
            String updatedAt
    ) {
        jdbcTemplate.update("""
                        INSERT INTO room_price_snapshot (
                            id,
                            camp_id,
                            poi_id,
                            room_category_id,
                            biz_date,
                            price_type,
                            channel_id,
                            price_cent,
                            currency,
                            status,
                            created_at,
                            updated_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                id,
                CAMP_ID,
                POI_ID,
                roomCategoryId,
                bizDate,
                priceType,
                channelId,
                priceCent,
                "CNY",
                "active",
                updatedAt,
                updatedAt
        );
    }
}
