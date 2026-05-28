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
class RoomCategoryPricingQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void roomCategoryPricingsGet_shouldReturnFeeTableWithFilters() throws Exception {
        seedPricingData();

        mockMvc.perform(post("/roomCategoryPricings/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "roomCategoryIds":["22002"],
                                  "channelIds":["5"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.head.length()").value(5))
                .andExpect(jsonPath("$.data.head[0].cellName").value("押金"))
                .andExpect(jsonPath("$.data.head[4].cellName").value("佣金率(%)"))
                .andExpect(jsonPath("$.data.body.length()").value(1))
                .andExpect(jsonPath("$.data.body[0].roomCategoryId").value("22002"))
                .andExpect(jsonPath("$.data.body[0].roomCategoryName").value("豪华双床房"))
                .andExpect(jsonPath("$.data.body[0].channelName").value("携程"))
                .andExpect(jsonPath("$.data.body[0].cells.length()").value(5))
                .andExpect(jsonPath("$.data.body[0].cells[4].cellName").value("佣金率(%)"))
                .andExpect(jsonPath("$.data.body[0].cells[4].value").value(12));
    }

    @Test
    @Timeout(60)
    void roomCategoryRulesGet_shouldReturnActivityTablesForBothDiscountTypes() throws Exception {
        seedPricingData();

        mockMvc.perform(post("/roomCategoryRules/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "discountType":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.head.length()").value(7))
                .andExpect(jsonPath("$.data.body.length()").value(3))
                .andExpect(jsonPath("$.data.body[0].roomCategoryId").value("22001"))
                .andExpect(jsonPath("$.data.body[0].channelName").value("路客云聚合"))
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
                .andExpect(jsonPath("$.data.body[0].roomCategoryId").value("22001"))
                .andExpect(jsonPath("$.data.body[0].channelName").value("路客云聚合"))
                .andExpect(jsonPath("$.data.body[0].cells.length()").value(2));
    }

    @Test
    @Timeout(60)
    void roomCategoryPricingEndpoints_shouldFallbackCurrentCampWhenCampIdBlank() throws Exception {
        seedPricingData();

        mockMvc.perform(post("/roomCategoryPricings/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"",
                                  "roomCategoryIds":[],
                                  "channelIds":[]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.body.length()").value(3));

        mockMvc.perform(post("/roomCategoryRules/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"",
                                  "discountType":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.body.length()").value(3));
    }

    @Test
    @Timeout(60)
    void roomCategoryPricingEndpoints_shouldRejectForeignCampAccess() throws Exception {
        mockMvc.perform(post("/roomCategoryPricings/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301));

        mockMvc.perform(post("/roomCategoryRules/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "discountType":2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301));
    }

    private void seedPricingData() {
        resetPricingData();

        insertChannelAccount(28301L, 17L, "路客云聚合", "tdd-localhome", "OUT-17-A", "authorized");
        insertChannelAccount(28302L, 5L, "携程", "tdd-ctrip", "OUT-5-A", "authorized");
        insertChannelAccount(28303L, 8L, "飞猪酒店", "tdd-fliggy", "OUT-8-A", "expired");

        insertChannelRoomCategoryRel(28401L, 28301L, 22001L, "on_shelf", "approved");
        insertChannelRoomCategoryRel(28402L, 28302L, 22001L, "on_shelf", "approved");
        insertChannelRoomCategoryRel(28403L, 28302L, 22002L, "on_shelf", "approved");
        insertChannelRoomCategoryRel(28404L, 28303L, 22003L, "on_shelf", "approved");
        insertChannelRoomCategoryRel(28405L, 28302L, 22003L, "off_shelf", "approved");

        insertGoodsMain(28501L, "标准大床房预售券", 19900, "published");
        insertGoodsMain(28502L, "豪华双床房早餐套餐", 39900, "published");
        insertGoodsRoomCategoryRel(28601L, 28501L, 22001L);
        insertGoodsRoomCategoryRel(28602L, 28502L, 22002L);
        insertDistributionProduct(28701L, 28501L, 1, 0.10d);
        insertDistributionProduct(28702L, 28502L, 1, 0.12d);
    }

    private void resetPricingData() {
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
                10001L,
                name,
                sellingPrice,
                sellingPrice + 5000,
                Math.max(sellingPrice - 2000, 0),
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
                10001L,
                goodsId,
                "{\"rate\": " + rate + ", \"type\": \"fixed_rate\"}",
                status
        );
    }
}
