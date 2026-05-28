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
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PresaleOrderQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void ordersPageGet_shouldReturnPresaleRowsWithCurrentFrontendFields() throws Exception {
        seedPresaleOrders();

        mockMvc.perform(post("/orders/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":"1",
                                  "pageSize":"20",
                                  "roomCategoryTypes":["1","2","3"],
                                  "orderStates":[],
                                  "categoryIds":[],
                                  "orderChannelIds":[],
                                  "paymentWayIds":[],
                                  "keyword":""
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.pages").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(3))
                .andExpect(jsonPath("$.data.list[0].orderId").value("69001"))
                .andExpect(jsonPath("$.data.list[0].orderChannelId").value("34"))
                .andExpect(jsonPath("$.data.list[0].orderChannelName").value("微信小程序"))
                .andExpect(jsonPath("$.data.list[0].paymentWayId").value("18302"))
                .andExpect(jsonPath("$.data.list[0].paymentWayName").value("TDD微信"))
                .andExpect(jsonPath("$.data.list[0].orderState").value(6))
                .andExpect(jsonPath("$.data.list[0].refundDisplayState").value(0))
                .andExpect(jsonPath("$.data.list[0].totalAmount").value(19900))
                .andExpect(jsonPath("$.data.list[0].paidAmount").value(0))
                .andExpect(jsonPath("$.data.list[0].buyerName").value("张三"))
                .andExpect(jsonPath("$.data.list[0].buyerMobile").value("13800000000"))
                .andExpect(jsonPath("$.data.list[0].createdAt").value("2026-05-18 10:12"))
                .andExpect(jsonPath("$.data.list[0].orderDetailViews[0].goodsName").value("早鸟预售券"))
                .andExpect(jsonPath("$.data.list[0].orderDetailViews[0].roomCategoryType").value(1))
                .andExpect(jsonPath("$.data.list[0].orderDetailViews[0].categoryId").value("11"))
                .andExpect(jsonPath("$.data.list[0].orderDetailViews[0].categoryName").value("住宿套餐 / 早餐券"))
                .andExpect(jsonPath("$.data.list[0].orderDetailViews[0].count").value(1))
                .andExpect(jsonPath("$.data.list[0].orderDetailViews[0].price").value(19900));
    }

    @Test
    @Timeout(60)
    void ordersPageGet_shouldSupportPresaleFiltersUsedByCurrentFrontend() throws Exception {
        seedPresaleOrders();

        long bookedStartDate = Timestamp.valueOf(LocalDateTime.of(2026, 5, 17, 0, 0)).getTime();
        long bookedEndDate = Timestamp.valueOf(LocalDateTime.of(2026, 5, 18, 0, 0)).getTime();

        mockMvc.perform(post("/orders/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":"1",
                                  "pageSize":"20",
                                  "roomCategoryTypes":["2"],
                                  "orderStates":[5,7,8,9,10],
                                  "categoryIds":["13"],
                                  "orderChannelIds":["36"],
                                  "paymentWayIds":["18308"],
                                  "refundDisplayState":3,
                                  "bookedStartDate":%d,
                                  "bookedEndDate":%d,
                                  "keyword":"下午茶"
                                }
                                """.formatted(bookedStartDate, bookedEndDate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].orderId").value("69003"))
                .andExpect(jsonPath("$.data.list[0].orderState").value(5))
                .andExpect(jsonPath("$.data.list[0].refundDisplayState").value(3))
                .andExpect(jsonPath("$.data.list[0].paymentWayId").value("18308"))
                .andExpect(jsonPath("$.data.list[0].orderDetailViews[0].roomCategoryType").value(2))
                .andExpect(jsonPath("$.data.list[0].orderDetailViews[0].categoryId").value("13"))
                .andExpect(jsonPath("$.data.list[0].orderDetailViews[0].goodsName").value("下午茶体验券"));
    }

    @Test
    @Timeout(60)
    void presaleOrdersPageGet_shouldFallbackCurrentCampAndRejectForeignCampAccess() throws Exception {
        seedPresaleOrders();

        mockMvc.perform(post("/orders/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"",
                                  "pageNum":"1",
                                  "pageSize":"20",
                                  "roomCategoryTypes":["1","2","3"],
                                  "orderStates":[],
                                  "categoryIds":[],
                                  "orderChannelIds":[],
                                  "paymentWayIds":[]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.list.length()").value(3));

        mockMvc.perform(post("/orders/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "pageNum":"1",
                                  "pageSize":"20",
                                  "roomCategoryTypes":["1","2","3"],
                                  "orderStates":[],
                                  "categoryIds":[],
                                  "orderChannelIds":[],
                                  "paymentWayIds":[]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301));
    }

    private void seedPresaleOrders() {
        resetPresaleOrders();

        insertChannelAccount(39401L, 34L, "微信小程序");
        insertChannelAccount(39402L, 33L, "抖音小程序");
        insertChannelAccount(39403L, 36L, "小红书");

        insertPaymentTypeGroup(99301L, 31, "预售支付", 3, 1, 10);
        insertPaymentType(19301L, 99301L, "预售支付类型", 31, "预售支付", 3, 1, 0, 0, 10);
        insertPaymentWay(18302L, "TDD微信");
        insertPaymentWay(18303L, "TDD支付宝");
        insertPaymentWay(18308L, "TDD储值余额");

        insertGoodsMain(68501L, "早鸟预售券", 11L, "住宿套餐 / 早餐券", 1, 19900L);
        insertGoodsMain(68502L, "连住抵扣券", 12L, "住宿套餐 / 房费抵扣券", 3, 8000L);
        insertGoodsMain(68503L, "下午茶体验券", 13L, "体验活动 / 周末加购", 2, 5900L);

        insertOrderMain(69001L, 68501L, 39401L, 18302L, "pending", "unpaid", "张三", "13800000000",
                LocalDateTime.of(2026, 5, 18, 10, 12), 1, 19900L, 0L, 0L);
        insertOrderMain(69002L, 68502L, 39402L, 18303L, "completed", "paid", "李四", "13900000000",
                LocalDateTime.of(2026, 5, 18, 9, 24), 2, 16000L, 16000L, 0L);
        insertOrderMain(69003L, 68503L, 39403L, 18308L, "cancelled", "refunded", "王五", "13700000000",
                LocalDateTime.of(2026, 5, 17, 18, 36), 3, 17700L, 17700L, 17700L);
    }

    private void resetPresaleOrders() {
        jdbcTemplate.update("DELETE FROM order_payment_record WHERE camp_id = ?", CAMP_ID);
        jdbcTemplate.update("""
                DELETE le
                FROM ledger_entry le
                JOIN order_main om ON om.order_id = le.order_id
                WHERE om.camp_id = ?
                """, CAMP_ID);
        jdbcTemplate.update("""
                DELETE og
                FROM order_guest og
                JOIN order_main om ON om.order_id = og.order_id
                WHERE om.camp_id = ?
                """, CAMP_ID);
        jdbcTemplate.update("DELETE FROM distribution_order WHERE camp_id = ?", CAMP_ID);
        jdbcTemplate.update("DELETE FROM order_main WHERE camp_id = ?", CAMP_ID);
        jdbcTemplate.update("DELETE FROM goods_main WHERE goods_id IN (?, ?, ?)", 68501L, 68502L, 68503L);
        jdbcTemplate.update("DELETE FROM payment_way WHERE payment_way_id IN (?, ?, ?)", 18302L, 18303L, 18308L);
        jdbcTemplate.update("DELETE FROM payment_type WHERE payment_type_id = ?", 19301L);
        jdbcTemplate.update("DELETE FROM payment_type_group WHERE payment_type_group_id = ?", 99301L);
        jdbcTemplate.update("DELETE FROM channel_account WHERE account_id IN (?, ?, ?)", 39401L, 39402L, 39403L);
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
                        ) VALUES (?, ?, ?, ?, ?, ?, 'authorized')
                        """,
                accountId,
                CAMP_ID,
                channelId,
                channelName,
                channelName + "账号",
                "OUT-" + accountId
        );
    }

    private void insertPaymentWay(long paymentWayId, String paymentWayName) {
        jdbcTemplate.update("""
                        INSERT INTO payment_way (
                            payment_way_id,
                            camp_id,
                            payment_way_name,
                            payment_way_code,
                            way_type,
                            sort_no,
                            status,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                paymentWayId,
                CAMP_ID,
                paymentWayName,
                "WAY-" + paymentWayId,
                "online",
                1,
                1,
                0
        );
    }

    private void insertPaymentTypeGroup(
            long paymentTypeGroupId,
            int groupType,
            String groupName,
            int bizType,
            int isIncome,
            int sortNo
    ) {
        jdbcTemplate.update("""
                        INSERT INTO payment_type_group (
                            payment_type_group_id,
                            camp_id,
                            group_type,
                            group_name,
                            biz_type,
                            is_income,
                            sort_no,
                            status,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                paymentTypeGroupId,
                CAMP_ID,
                groupType,
                groupName,
                bizType,
                isIncome,
                sortNo,
                1,
                0
        );
    }

    private void insertPaymentType(
            long paymentTypeId,
            long paymentTypeGroupId,
            String paymentTypeName,
            int groupType,
            String groupName,
            int bizType,
            int isIncome,
            int isCustom,
            int ignoreOrderGetItem,
            int sortNo
    ) {
        jdbcTemplate.update("""
                        INSERT INTO payment_type (
                            payment_type_id,
                            camp_id,
                            payment_type_group_id,
                            payment_type_name,
                            group_type,
                            group_name,
                            biz_type,
                            is_income,
                            is_custom,
                            ignore_order_get_item,
                            status,
                            sort_no,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                paymentTypeId,
                CAMP_ID,
                paymentTypeGroupId,
                paymentTypeName,
                groupType,
                groupName,
                bizType,
                isIncome,
                isCustom,
                ignoreOrderGetItem,
                1,
                sortNo,
                0
        );
    }

    private void insertGoodsMain(
            long goodsId,
            String name,
            long categoryId,
            String categoryName,
            int roomCategoryType,
            long sellingPriceCent
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
                CAMP_ID,
                "presale",
                name,
                categoryId,
                categoryName,
                roomCategoryType,
                sellingPriceCent,
                sellingPriceCent,
                sellingPriceCent,
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

    private void insertOrderMain(
            long orderId,
            long goodsId,
            long channelAccountId,
            long paymentWayId,
            String status,
            String paymentStatus,
            String guestName,
            String guestMobile,
            LocalDateTime createdAt,
            int count,
            long totalPriceCent,
            long totalPayPriceCent,
            long refundPriceCent
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
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 0)
                        """,
                orderId,
                CAMP_ID,
                11001L,
                22001L,
                null,
                channelAccountId,
                goodsId,
                "ORDER-" + orderId,
                "OUT-" + orderId,
                "goods_order",
                status,
                guestName,
                guestMobile,
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt.plusHours(2)),
                count,
                totalPriceCent,
                0L,
                totalPayPriceCent,
                refundPriceCent,
                0L,
                0L,
                0L,
                0L,
                totalPayPriceCent - refundPriceCent,
                paymentStatus,
                19301L,
                paymentWayId,
                "mini_program",
                "presale-order",
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt.plusMinutes(5)),
                12001L,
                12001L
        );
    }
}
