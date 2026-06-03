package com.jeez.zp.crm.controller;

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
class ScrmWechatServiceIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;
    private static final long POI_ID = 11001L;
    private static final long MEMBER_ID = 14001L;
    private static final long ROOM_CATEGORY_ID = 98601L;
    private static final long ROOM_ID = 98611L;
    private static final long CHANNEL_ACCOUNT_ID = 98621L;
    private static final long PHRASE_GROUP_ID = 98631L;
    private static final long PHRASE_ID = 98632L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void wxcpKfAccountPageGet_shouldReturnRealWecomMembersWithOrderMetrics() throws Exception {
        seedScrmData();

        mockMvc.perform(post("/wxcp/kfAccount/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":1,
                                  "pageSize":10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value("14001"))
                .andExpect(jsonPath("$.data.list[0].name").value("系统管理员"))
                .andExpect(jsonPath("$.data.list[0].status").value("online"))
                .andExpect(jsonPath("$.data.list[0].todaySessions").value(2))
                .andExpect(jsonPath("$.data.list[0].averageReplySeconds").value(96))
                .andExpect(jsonPath("$.data.list[0].serviceScore").value(98));
    }

    @Test
    @Timeout(60)
    void wxcpKfAccountReportGet_shouldReturnOrderBackedConversationsAndSupportFilters() throws Exception {
        seedScrmData();

        mockMvc.perform(post("/wxcp/kfAccount/report/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "startDate":"2026-05-18",
                                  "endDate":"2026-05-18",
                                  "channel":"meituan",
                                  "status":"pendingCheckIn",
                                  "keyword":"Alice",
                                  "pageNum":1,
                                  "pageSize":8
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.summary.todaySessions").value(1))
                .andExpect(jsonPath("$.data.summary.pendingSessions").value(1))
                .andExpect(jsonPath("$.data.summary.conversionLeads").value(0))
                .andExpect(jsonPath("$.data.summary.responseRate").value("100%"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.conversations.length()").value(1))
                .andExpect(jsonPath("$.data.conversations[0].id").value("WX-98641"))
                .andExpect(jsonPath("$.data.conversations[0].customerName").value("Alice PMS"))
                .andExpect(jsonPath("$.data.conversations[0].channel").value("meituan"))
                .andExpect(jsonPath("$.data.conversations[0].status").value("pendingCheckIn"))
                .andExpect(jsonPath("$.data.conversations[0].roomType").value("SCRM联调大床房"))
                .andExpect(jsonPath("$.data.conversations[0].assignee").value("系统管理员"))
                .andExpect(jsonPath("$.data.conversations[0].unread").value(1));
    }

    @Test
    @Timeout(60)
    void scrmSidebarDashboardAndExport_shouldReturnFrontendContractFromRealTables() throws Exception {
        seedScrmData();

        mockMvc.perform(post("/scrm/sidebarPreview/dashboard")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "poiId":"11001",
                                  "statDate":"2026-05-18",
                                  "channel":"meituan",
                                  "keyword":"Alice",
                                  "page":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.stores[0].id").value("11001"))
                .andExpect(jsonPath("$.data.channels[0].id").value("ALL"))
                .andExpect(jsonPath("$.data.metrics[0].id").value("sessions"))
                .andExpect(jsonPath("$.data.metrics[0].value").value("1"))
                .andExpect(jsonPath("$.data.conversations.length()").value(1))
                .andExpect(jsonPath("$.data.conversations[0].id").value("conv-98641"))
                .andExpect(jsonPath("$.data.conversations[0].guestName").value("Alice PMS"))
                .andExpect(jsonPath("$.data.conversations[0].channel").value("meituan"))
                .andExpect(jsonPath("$.data.conversations[0].orderNo").value("SCRM98641"))
                .andExpect(jsonPath("$.data.replyTemplates[0].id").value("98632"))
                .andExpect(jsonPath("$.data.replyTemplates[0].title").value("入住指引"))
                .andExpect(jsonPath("$.data.roomSuggestions[0].roomName").value("SCRM联调大床房 / SCRM-101"))
                .andExpect(jsonPath("$.data.pagination.total").value(1));

        mockMvc.perform(post("/scrm/sidebarPreview/export")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "poiId":"11001",
                                  "statDate":"2026-05-18",
                                  "channel":"meituan",
                                  "keyword":"Alice",
                                  "page":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.taskId").value("SCRM-SIDEBAR-EXPORT-10001-20260518"))
                .andExpect(jsonPath("$.data.fileName").value("scrm_sidebar_20260518.csv"))
                .andExpect(jsonPath("$.data.contentType").value("text/csv"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.rows[0].guestName").value("Alice PMS"));
    }

    @Test
    @Timeout(60)
    void scrmWechatEndpoints_shouldRejectForeignCamp() throws Exception {
        seedScrmData();

        mockMvc.perform(post("/wxcp/kfAccount/report/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "startDate":"2026-05-18",
                                  "endDate":"2026-05-18",
                                  "pageNum":1,
                                  "pageSize":8
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(40301));
    }

    private void seedScrmData() {
        resetScrmData();
        jdbcTemplate.update("""
                        UPDATE pms_member
                        SET wecom_user_id = ?, updated_at = NOW()
                        WHERE member_id = ?
                        """,
                "wecom-scrm-14001",
                MEMBER_ID
        );
        insertRoomCategory();
        insertRoom();
        insertChannelAccount();
        insertPhrase();
        insertOrder(98641L, "SCRM98641", "Alice PMS", "pending_checkin", "meituan咨询：客人询问入住指引", 28800, 0);
        insertOrder(98642L, "SCRM98642", "Bob PMS", "checked_in", "Bob已入住，确认门锁密码", 38800, 1);
    }

    private void resetScrmData() {
        jdbcTemplate.update("DELETE FROM order_main WHERE order_id BETWEEN 98641 AND 98642");
        jdbcTemplate.update("DELETE FROM im_phrase WHERE im_phrase_id = ?", PHRASE_ID);
        jdbcTemplate.update("DELETE FROM im_phrase_group WHERE im_phrase_group_id = ?", PHRASE_GROUP_ID);
        jdbcTemplate.update("DELETE FROM channel_account WHERE account_id = ?", CHANNEL_ACCOUNT_ID);
        jdbcTemplate.update("DELETE FROM room WHERE room_id = ?", ROOM_ID);
        jdbcTemplate.update("DELETE FROM room_category WHERE room_category_id = ?", ROOM_CATEGORY_ID);
        jdbcTemplate.update("UPDATE pms_member SET wecom_user_id = NULL WHERE member_id = ?", MEMBER_ID);
    }

    private void insertRoomCategory() {
        jdbcTemplate.update("""
                        INSERT INTO room_category (
                            room_category_id,
                            camp_id,
                            poi_id,
                            name,
                            display_name,
                            room_count,
                            rental_type,
                            property_type,
                            weekday_price_cent,
                            weekend_price_cent,
                            status,
                            sort_no,
                            created_at,
                            updated_at,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, 1, 'whole', 'hotel', 28800, 32800, 1, 1, NOW(), NOW(), 0)
                        """,
                ROOM_CATEGORY_ID,
                CAMP_ID,
                POI_ID,
                "SCRM联调大床房",
                "SCRM联调大床房"
        );
    }

    private void insertRoom() {
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
                            created_at,
                            updated_at,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, 'SCRM-101', 'SCRM-101', 'normal', 'available', 'clean', 1, 1, NOW(), NOW(), 0)
                        """,
                ROOM_ID,
                CAMP_ID,
                POI_ID,
                ROOM_CATEGORY_ID
        );
    }

    private void insertChannelAccount() {
        jdbcTemplate.update("""
                        INSERT INTO channel_account (
                            account_id,
                            camp_id,
                            channel_id,
                            channel_name,
                            account_name,
                            out_account_id,
                            status,
                            authorized_at,
                            config_json,
                            created_at,
                            updated_at
                        ) VALUES (?, ?, 986, '美团民宿', 'SCRM美团客服', 'scrm-meituan', 'authorized', NOW(), CAST('{}' AS JSON), NOW(), NOW())
                        """,
                CHANNEL_ACCOUNT_ID,
                CAMP_ID
        );
    }

    private void insertPhrase() {
        jdbcTemplate.update("""
                        INSERT INTO im_phrase_group (
                            im_phrase_group_id,
                            camp_id,
                            group_name,
                            parent_group_id,
                            sort_no,
                            status,
                            created_at,
                            updated_at
                        ) VALUES (?, ?, 'SCRM话术', NULL, 1, 1, NOW(), NOW())
                        """,
                PHRASE_GROUP_ID,
                CAMP_ID
        );
        jdbcTemplate.update("""
                        INSERT INTO im_phrase (
                            im_phrase_id,
                            camp_id,
                            im_phrase_group_id,
                            title,
                            content,
                            sort_no,
                            status,
                            created_at,
                            updated_at
                        ) VALUES (?, ?, ?, '入住指引', '稍后发送门锁密码和入住指引。', 1, 1, NOW(), NOW())
                        """,
                PHRASE_ID,
                CAMP_ID,
                PHRASE_GROUP_ID
        );
    }

    private void insertOrder(long orderId, String orderNo, String guestName, String status, String remark, long amount, int dayOffset) {
        LocalDateTime baseTime = LocalDateTime.of(2026, 5, 18, 9 + dayOffset, 30);
        jdbcTemplate.update("""
                        INSERT INTO order_main (
                            order_id,
                            camp_id,
                            poi_id,
                            room_category_id,
                            room_id,
                            channel_id,
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
                            total_pay_price_cent,
                            payment_status,
                            source_type,
                            remark,
                            created_at,
                            updated_at,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'hotel', ?, ?, '13998641001', ?, ?, 1, ?, ?, 'paid', 'wechat_service', ?, ?, ?, 0)
                        """,
                orderId,
                CAMP_ID,
                POI_ID,
                ROOM_CATEGORY_ID,
                ROOM_ID,
                CHANNEL_ACCOUNT_ID,
                orderNo,
                "OUT-" + orderNo,
                status,
                guestName,
                Timestamp.valueOf(LocalDateTime.of(2026, 5, 18, 15, 0)),
                Timestamp.valueOf(LocalDateTime.of(2026, 5, 19, 11, 0)),
                amount,
                amount,
                remark,
                Timestamp.valueOf(baseTime),
                Timestamp.valueOf(baseTime)
        );
    }
}
