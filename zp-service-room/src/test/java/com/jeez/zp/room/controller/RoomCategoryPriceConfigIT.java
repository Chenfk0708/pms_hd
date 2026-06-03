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
class RoomCategoryPriceConfigIT {

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
    void roomCategoryPricingsGet_shouldReturnOtherPriceFeeTableFromRoomService() throws Exception {
        seedPriceConfigData();

        mockMvc.perform(post("/roomCategoryPricings/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "roomCategoryIds":["127002"],
                                  "channelIds":["5"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.head.length()").value(5))
                .andExpect(jsonPath("$.data.head[0].cellName").value("\u62bc\u91d1"))
                .andExpect(jsonPath("$.data.head[4].cellName").value("\u4f63\u91d1\u7387(%)"))
                .andExpect(jsonPath("$.data.body.length()").value(1))
                .andExpect(jsonPath("$.data.body[0].roomCategoryId").value("127002"))
                .andExpect(jsonPath("$.data.body[0].roomCategoryName").value("TDD\u5176\u4ed6\u4ef7\u683c\u8c6a\u534e\u53cc\u5e8a\u623f"))
                .andExpect(jsonPath("$.data.body[0].channelId").value("5"))
                .andExpect(jsonPath("$.data.body[0].channelName").value("\u643a\u7a0b"))
                .andExpect(jsonPath("$.data.body[0].cells.length()").value(5))
                .andExpect(jsonPath("$.data.body[0].cells[4].value").value(12));
    }

    @Test
    @Timeout(60)
    void roomCategoryRulesGet_shouldReturnLongStayAndFlashSaleTablesFromRoomService() throws Exception {
        seedPriceConfigData();

        mockMvc.perform(post("/roomCategoryRules/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"",
                                  "discountType":1,
                                  "roomCategoryIds":["127001","127002"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.head.length()").value(7))
                .andExpect(jsonPath("$.data.body.length()").value(3))
                .andExpect(jsonPath("$.data.body[0].cells.length()").value(7));

        mockMvc.perform(post("/roomCategoryRules/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "discountType":2,
                                  "channelIds":["17"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.head.length()").value(2))
                .andExpect(jsonPath("$.data.body.length()").value(1))
                .andExpect(jsonPath("$.data.body[0].channelId").value("17"))
                .andExpect(jsonPath("$.data.body[0].cells.length()").value(2));
    }

    @Test
    @Timeout(60)
    void retailPriceConfigEndpoints_shouldReturnFrontendBootstrapShape() throws Exception {
        mockMvc.perform(post("/roomCategoryPrice/salePriceSetting/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.isInitPriceDisplay").value(0))
                .andExpect(jsonPath("$.data.pricePriceInterfaceDisplayType").value("2"))
                .andExpect(jsonPath("$.data.priceSalePriceSettings").isArray());

        mockMvc.perform(post("/systemConfig/price/storesPriceShow/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":""}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.displayMode").value("allStores"))
                .andExpect(jsonPath("$.data.isShowStoresPrice").value(1));
    }

    @Test
    @Timeout(60)
    void roomCategoryStatusesRoomCategoryGet_shouldReturnRetailStatusRowsAndRespectFilters() throws Exception {
        seedRetailSnapshotData();

        mockMvc.perform(post("/roomCategoryStatuses/roomCategory/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "roomCategoryIds":["127901"],
                                  "poiIds":["11001"],
                                  "date":"2026-05-18",
                                  "days":3,
                                  "pageNum":1,
                                  "pageSize":10,
                                  "isStores":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.pageX.total").value(1))
                .andExpect(jsonPath("$.data.roomStatusViews.length()").value(1))
                .andExpect(jsonPath("$.data.roomStatusViews[0].roomCategoryId").value("127901"))
                .andExpect(jsonPath("$.data.roomStatusViews[0].roomCategoryName").value("TDD\u95e8\u5e02\u4ef7\u65e5\u5386\u623f"))
                .andExpect(jsonPath("$.data.roomStatusViews[0].statusViews.length()").value(3))
                .andExpect(jsonPath("$.data.roomStatusViews[0].statusViews[0].date").value("2026-05-18"))
                .andExpect(jsonPath("$.data.roomStatusViews[0].statusViews[0].price").value(21800))
                .andExpect(jsonPath("$.data.roomStatusViews[0].statusViews[1].price").value(22800))
                .andExpect(jsonPath("$.data.roomStatusViews[0].statusViews[2].price").value(23800));
    }

    @Test
    @Timeout(60)
    void priceConfigEndpoints_shouldRejectForeignCampAccess() throws Exception {
        mockMvc.perform(post("/roomCategoryPricings/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10002"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301));

        mockMvc.perform(post("/roomCategoryPrice/salePriceSetting/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10002"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301));

        mockMvc.perform(post("/roomCategoryStatuses/roomCategory/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10002"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301));
    }

    private void seedPriceConfigData() {
        resetPriceConfigData();
        insertRoomCategory(127001L, "TDD\u5176\u4ed6\u4ef7\u683c\u6807\u51c6\u5927\u5e8a\u623f", 10, 19900L);
        insertRoomCategory(127002L, "TDD\u5176\u4ed6\u4ef7\u683c\u8c6a\u534e\u53cc\u5e8a\u623f", 20, 39900L);
        insertChannelAccount(127301L, 17L, "\u8def\u5ba2\u4e91\u805a\u5408", "tdd-other-localhome", "OUT-17-PRICE", "authorized");
        insertChannelAccount(127302L, 5L, "\u643a\u7a0b", "tdd-other-ctrip", "OUT-5-PRICE", "authorized");
        insertChannelRoomCategoryRel(127401L, 127301L, 127001L, "on_shelf", "approved");
        insertChannelRoomCategoryRel(127402L, 127302L, 127001L, "on_shelf", "approved");
        insertChannelRoomCategoryRel(127403L, 127302L, 127002L, "on_shelf", "approved");
        insertGoodsMain(127501L, "TDD\u6807\u51c6\u5927\u5e8a\u623f<\u65e0\u65e9>", 19900L);
        insertGoodsMain(127502L, "TDD\u8c6a\u534e\u53cc\u5e8a\u623f<\u53cc\u65e9>", 39900L);
        insertGoodsRoomCategoryRel(127601L, 127501L, 127001L);
        insertGoodsRoomCategoryRel(127602L, 127502L, 127002L);
        insertDistributionProduct(127701L, 127501L, 0.10d);
        insertDistributionProduct(127702L, 127502L, 0.12d);
    }

    private void seedRetailSnapshotData() {
        resetRetailSnapshotData();
        insertRoomCategory(127901L, "TDD\u95e8\u5e02\u4ef7\u65e5\u5386\u623f", 30, 21800L);
        insertPriceSnapshot(127981L, 127901L, "2026-05-18", 21800L);
        insertPriceSnapshot(127982L, 127901L, "2026-05-19", 22800L);
        insertPriceSnapshot(127983L, 127901L, "2026-05-20", 23800L);
    }

    private void resetPriceConfigData() {
        jdbcTemplate.update("""
                DELETE grr
                FROM goods_room_category_rel grr
                JOIN goods_main gm ON gm.goods_id = grr.goods_id
                WHERE gm.goods_id IN (?, ?)
                """, 127501L, 127502L);
        jdbcTemplate.update("DELETE FROM distribution_product WHERE distribution_product_id IN (?, ?)", 127701L, 127702L);
        jdbcTemplate.update("DELETE FROM goods_main WHERE goods_id IN (?, ?)", 127501L, 127502L);
        jdbcTemplate.update("DELETE FROM channel_room_category_rel WHERE id IN (?, ?, ?)", 127401L, 127402L, 127403L);
        jdbcTemplate.update("DELETE FROM channel_account WHERE account_id IN (?, ?)", 127301L, 127302L);
        jdbcTemplate.update("DELETE FROM room_category WHERE room_category_id IN (?, ?)", 127001L, 127002L);
    }

    private void resetRetailSnapshotData() {
        jdbcTemplate.update("DELETE FROM room_price_snapshot WHERE id IN (?, ?, ?)", 127981L, 127982L, 127983L);
        jdbcTemplate.update("DELETE FROM room_category WHERE room_category_id = ?", 127901L);
    }

    private void insertRoomCategory(long roomCategoryId, String name, int sortNo, long priceCent) {
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
                3,
                2,
                priceCent,
                priceCent,
                priceCent,
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

    private void insertChannelAccount(long accountId, long channelId, String channelName, String accountName, String outAccountId, String status) {
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

    private void insertGoodsMain(long goodsId, String name, long sellingPrice) {
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
                        ) VALUES (?, ?, 'package', ?, ?, ?, ?, ?, ?, ?, 'published', 0)
                        """,
                goodsId,
                CAMP_ID,
                name,
                sellingPrice,
                sellingPrice,
                sellingPrice,
                50,
                "13800000001",
                "auto"
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

    private void insertDistributionProduct(long distributionProductId, long goodsId, double rate) {
        jdbcTemplate.update("""
                        INSERT INTO distribution_product (
                            distribution_product_id,
                            camp_id,
                            goods_id,
                            room_category_id,
                            commission_rule_json,
                            status
                        ) VALUES (?, ?, ?, NULL, CAST(? AS JSON), 1)
                        """,
                distributionProductId,
                CAMP_ID,
                goodsId,
                "{\"rate\": " + rate + ", \"type\": \"fixed_rate\"}"
        );
    }

    private void insertPriceSnapshot(long id, long roomCategoryId, String bizDate, long priceCent) {
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
                        ) VALUES (?, ?, ?, ?, ?, 'retail', 0, ?, 'CNY', 'active', ?, ?)
                        """,
                id,
                CAMP_ID,
                POI_ID,
                roomCategoryId,
                bizDate,
                priceCent,
                bizDate + " 09:00:00",
                bizDate + " 09:00:00"
        );
    }
}
