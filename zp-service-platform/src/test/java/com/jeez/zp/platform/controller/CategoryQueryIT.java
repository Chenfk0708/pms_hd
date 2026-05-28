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
class CategoryQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void categoriesGet_shouldReturnDistinctPresaleCategoriesForCurrentCamp() throws Exception {
        resetCategories();
        insertGoodsMain(78501L, "早餐券", 11L, "住宿套餐 / 早餐券", 1);
        insertGoodsMain(78502L, "抵扣券", 12L, "住宿套餐 / 房费抵扣券", 3);
        insertGoodsMain(78503L, "周末加购", 13L, "体验活动 / 周末加购", 2);
        insertGoodsMain(78504L, "早餐券-重复", 11L, "住宿套餐 / 早餐券", 1);
        insertGoodsMain(78505L, "酒店套餐", 21L, "酒店套餐 / 双晚套餐", 4);

        mockMvc.perform(post("/categories/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"parentId":0}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.categoryViews.length()").value(3))
                .andExpect(jsonPath("$.data.categoryViews[0].categoryId").value("11"))
                .andExpect(jsonPath("$.data.categoryViews[0].categoryName").value("住宿套餐 / 早餐券"))
                .andExpect(jsonPath("$.data.categoryViews[0].id").value("11"))
                .andExpect(jsonPath("$.data.categoryViews[0].name").value("住宿套餐 / 早餐券"))
                .andExpect(jsonPath("$.data.categoryViews[0].children.length()").value(0))
                .andExpect(jsonPath("$.data.categoryViews[2].categoryId").value("13"))
                .andExpect(jsonPath("$.data.categoryViews[2].categoryName").value("体验活动 / 周末加购"));
    }

    private void resetCategories() {
        jdbcTemplate.update("DELETE FROM goods_main WHERE goods_id IN (?, ?, ?, ?, ?)", 78501L, 78502L, 78503L, 78504L, 78505L);
    }

    private void insertGoodsMain(
            long goodsId,
            String name,
            long categoryId,
            String categoryName,
            int roomCategoryType
    ) {
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
                            effective_start_at,
                            effective_end_at,
                            description,
                            refund_rule,
                            reservation_phone,
                            reservation_note,
                            status,
                            remark,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                goodsId,
                10001L,
                "presale",
                name,
                categoryId,
                categoryName,
                roomCategoryType,
                1000L,
                1000L,
                1000L,
                100,
                "manual",
                "on_shelf",
                "2026-05-01 00:00:00",
                "2027-05-01 00:00:00",
                name + "描述",
                "购买后按门店规则退款",
                "13800009999",
                "请联系门店确认预约时间",
                "published",
                "integration-test",
                0
        );
    }
}
