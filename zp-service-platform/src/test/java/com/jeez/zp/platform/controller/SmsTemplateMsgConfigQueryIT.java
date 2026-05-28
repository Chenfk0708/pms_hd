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
class SmsTemplateMsgConfigQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String CAMP_ID = "10001";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void smsTemplateMsgConfigPageGet_shouldReturnPagedTemplateRows() throws Exception {
        jdbcTemplate.update("""
                        INSERT INTO sms_template (
                            sms_template_id,
                            camp_id,
                            provider_code,
                            template_title,
                            template_content,
                            audit_status,
                            send_status,
                            sign_name,
                            created_at,
                            updated_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                20001L,
                10001L,
                "aliyun",
                "获得密码（智能入住）",
                "您的房间密码为 {password}",
                "approved",
                "enabled",
                "路客云",
                "2026-05-19 09:00:00",
                "2026-05-19 09:00:00"
        );
        jdbcTemplate.update("""
                        INSERT INTO sms_template (
                            sms_template_id,
                            camp_id,
                            provider_code,
                            template_title,
                            template_content,
                            audit_status,
                            send_status,
                            sign_name,
                            created_at,
                            updated_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                20002L,
                10001L,
                "aliyun",
                "实名认证（智能入住）",
                "请先完成实名认证",
                "pending",
                "disabled",
                "路客云",
                "2026-05-20 09:00:00",
                "2026-05-20 09:00:00"
        );

        mockMvc.perform(post("/smsTemplateMsgConfig/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"%s","sendType":0,"pageNum":1,"pageSize":100}
                                """.formatted(CAMP_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.size").value(100))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.hasNextPage").value(false))
                .andExpect(jsonPath("$.data.pages").value(1))
                .andExpect(jsonPath("$.data.extraInfo").isEmpty())
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].smsTemplateMsgConfigId").value("20002"))
                .andExpect(jsonPath("$.data.list[0].name").value("实名认证（智能入住）"))
                .andExpect(jsonPath("$.data.list[0].passContent").value("请先完成实名认证"))
                .andExpect(jsonPath("$.data.list[0].sendStatus").value(0))
                .andExpect(jsonPath("$.data.list[0].isEnabled").value(0))
                .andExpect(jsonPath("$.data.list[0].signName").value("路客云"))
                .andExpect(jsonPath("$.data.list[0].auditStatus").value(1))
                .andExpect(jsonPath("$.data.list[1].smsTemplateMsgConfigId").value("20001"))
                .andExpect(jsonPath("$.data.list[1].sendStatus").value(1))
                .andExpect(jsonPath("$.data.list[1].isEnabled").value(1))
                .andExpect(jsonPath("$.data.list[1].auditStatus").value(3));
    }

    @Test
    @Timeout(60)
    void smsTemplateMsgConfigPageGet_shouldFallbackToCurrentCampAndReturnEmptyPage() throws Exception {
        mockMvc.perform(post("/smsTemplateMsgConfig/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"","sendType":0,"pageNum":1,"pageSize":100}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.size").value(100))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.hasNextPage").value(false))
                .andExpect(jsonPath("$.data.pages").value(0))
                .andExpect(jsonPath("$.data.list.length()").value(0));
    }

    @Test
    @Timeout(60)
    void smsTemplateMsgConfigPageGet_shouldRejectForeignCampAccess() throws Exception {
        mockMvc.perform(post("/smsTemplateMsgConfig/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10002","sendType":0,"pageNum":1,"pageSize":100}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
