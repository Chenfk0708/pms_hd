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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SortSettingMutationIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;
    private static final long USER_ID = 12001L;
    private static final long POI_ID = 11001L;
    private static final long GROUP_ID = 21001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void roomCategorySeqs_shouldPersistRoomCategorySortOrder() throws Exception {
        insertRoomCategory(92701L, "TDD排序房型A", 10);
        insertRoomCategory(92702L, "TDD排序房型B", 20);
        insertRoomCategory(92703L, "TDD排序房型C", 30);

        mockMvc.perform(post("/roomCategory/seqs")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "roomCategorySeqs":[
                                    {"roomCategoryId":"92703","seq":0},
                                    {"roomCategoryId":"92701","seq":1},
                                    {"roomCategoryId":"92702","seq":2}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.message").value("room category sort updated"))
                .andExpect(jsonPath("$.data.updatedCount").value(3))
                .andExpect(jsonPath("$.data.ids[0]").value("92703"))
                .andExpect(jsonPath("$.data.ids[1]").value("92701"))
                .andExpect(jsonPath("$.data.ids[2]").value("92702"));

        assertThat(selectRoomSortNo(92703L)).isEqualTo(1);
        assertThat(selectRoomSortNo(92701L)).isEqualTo(2);
        assertThat(selectRoomSortNo(92702L)).isEqualTo(3);
    }

    @Test
    @Timeout(60)
    void channelRoomCategorySeqs_shouldPersistGoodsSortOrder() throws Exception {
        insertGoods(92801L, "TDD排序商品A");
        insertGoods(92802L, "TDD排序商品B");
        insertGoods(92803L, "TDD排序商品C");

        mockMvc.perform(post("/channelRoomCategories/seqs")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "channelRoomCategorySeqs":[
                                    {"channelRoomCategoryId":"92802","seq":0},
                                    {"channelRoomCategoryId":"92803","seq":1},
                                    {"channelRoomCategoryId":"92801","seq":2}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.message").value("goods sort updated"))
                .andExpect(jsonPath("$.data.updatedCount").value(3))
                .andExpect(jsonPath("$.data.ids[0]").value("92802"))
                .andExpect(jsonPath("$.data.ids[1]").value("92803"))
                .andExpect(jsonPath("$.data.ids[2]").value("92801"));

        assertThat(selectGoodsRemark(92802L)).isEqualTo("sort:1");
        assertThat(selectGoodsRemark(92803L)).isEqualTo("sort:2");
        assertThat(selectGoodsRemark(92801L)).isEqualTo("sort:3");
    }

    @Test
    @Timeout(60)
    void roomCategorySeqs_shouldRejectForeignCamp() throws Exception {
        mockMvc.perform(post("/roomCategory/seqs")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "roomCategorySeqs":[{"roomCategoryId":"92701","seq":0}]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    private Integer selectRoomSortNo(long roomCategoryId) {
        return jdbcTemplate.queryForObject("SELECT sort_no FROM room_category WHERE room_category_id = ?", Integer.class, roomCategoryId);
    }

    private String selectGoodsRemark(long goodsId) {
        return jdbcTemplate.queryForObject("SELECT remark FROM goods_main WHERE goods_id = ?", String.class, goodsId);
    }

    private void insertRoomCategory(long roomCategoryId, String name, int sortNo) {
        jdbcTemplate.update("""
                        INSERT INTO room_category (
                            room_category_id,
                            camp_id,
                            poi_id,
                            group_id,
                            name,
                            display_name,
                            room_count,
                            status,
                            sort_no,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                roomCategoryId,
                CAMP_ID,
                POI_ID,
                GROUP_ID,
                name,
                name,
                1,
                1,
                sortNo,
                0
        );
    }

    private void insertGoods(long goodsId, String name) {
        jdbcTemplate.update("""
                        INSERT INTO goods_main (
                            goods_id,
                            camp_id,
                            goods_type,
                            name,
                            category_id,
                            category_name,
                            room_category_type,
                            selling_price_cent,
                            original_price_cent,
                            settlement_price_cent,
                            stock,
                            stock_mode,
                            shelf_status,
                            status,
                            remark,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                goodsId,
                CAMP_ID,
                "coupon",
                name,
                14L,
                "房券",
                1,
                10000L,
                12000L,
                9000L,
                10,
                "limited",
                "on_shelf",
                "published",
                null,
                0
        );
    }
}
