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

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ImSettingControllerIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;
    private static final String YUNXIN_APP_KEY = "hudson.im.yunxin.appKey";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void imWordsGroupTreeAndPageGet_shouldReturnRealPhraseTreeAndFilteredPhrases() throws Exception {
        seedImPhrases();

        mockMvc.perform(post("/imWordsGroup/tree/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.imWordsGroupGetViews.length()").value(1))
                .andExpect(jsonPath("$.data.imWordsGroupGetViews[0].imWordsGroupId").value("43101"))
                .andExpect(jsonPath("$.data.imWordsGroupGetViews[0].name").value("IM Checkin"))
                .andExpect(jsonPath("$.data.imWordsGroupGetViews[0].children.length()").value(1))
                .andExpect(jsonPath("$.data.imWordsGroupGetViews[0].children[0].imWordsGroupId").value("43102"))
                .andExpect(jsonPath("$.data.imWordsGroupGetViews[0].children[0].name").value("IM Checkin Child"));

        mockMvc.perform(post("/imWords/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "keyword":"Checkout",
                                  "imWordsGroupId":"43101",
                                  "pageNum":1,
                                  "pageSize":10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].imWordsId").value("43111"))
                .andExpect(jsonPath("$.data.list[0].title").value("Checkout reminder"))
                .andExpect(jsonPath("$.data.list[0].content").value("Please check out before noon."))
                .andExpect(jsonPath("$.data.list[0].groupName").value("IM Checkin"))
                .andExpect(jsonPath("$.data.list[0].imWordsGroupId").value("43101"))
                .andExpect(jsonPath("$.data.list[0].updatedAt").exists());

        mockMvc.perform(post("/imWords/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10002","pageNum":1,"pageSize":10}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Timeout(60)
    void imYunxinUserGet_shouldReturnStoredAppKeyAndCurrentUserAccid() throws Exception {
        resetYunxinConfig();
        jdbcTemplate.update("""
                        INSERT INTO system_config (
                            system_config_id,
                            camp_id,
                            config_key,
                            config_scope,
                            config_value,
                            value_type,
                            source,
                            updated_by,
                            updated_at
                        ) VALUES (?, ?, ?, 'camp', ?, 'json', 'platform', ?, NOW())
                        """,
                43131L,
                CAMP_ID,
                YUNXIN_APP_KEY,
                "\"yunxin-app-key-431\"",
                12001L
        );

        mockMvc.perform(post("/imYunxinUser/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.appKey").value("yunxin-app-key-431"))
                .andExpect(jsonPath("$.data.accid").value("pms_10001_12001"));
    }

    private void seedImPhrases() {
        resetImPhrases();
        insertPhraseGroup(43101L, "IM Checkin", null, 1);
        insertPhraseGroup(43102L, "IM Checkin Child", 43101L, 2);
        insertPhrase(43111L, 43101L, "Checkout reminder", "Please check out before noon.", 1);
        insertPhrase(43112L, 43102L, "Welcome note", "Welcome to the hotel.", 2);
    }

    private void resetImPhrases() {
        jdbcTemplate.update("DELETE FROM im_phrase WHERE im_phrase_id BETWEEN 43111 AND 43119");
        jdbcTemplate.update("DELETE FROM im_phrase_group WHERE im_phrase_group_id BETWEEN 43101 AND 43109");
    }

    private void resetYunxinConfig() {
        jdbcTemplate.update(
                "DELETE FROM system_config WHERE camp_id = ? AND config_key = ? AND config_scope = 'camp'",
                CAMP_ID,
                YUNXIN_APP_KEY
        );
    }

    private void insertPhraseGroup(long groupId, String groupName, Long parentGroupId, int sortNo) {
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
                        ) VALUES (?, ?, ?, ?, ?, 1, NOW(), NOW())
                        """,
                groupId,
                CAMP_ID,
                groupName,
                parentGroupId,
                sortNo
        );
    }

    private void insertPhrase(long phraseId, long groupId, String title, String content, int sortNo) {
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
                        ) VALUES (?, ?, ?, ?, ?, ?, 1, NOW(), NOW())
                        """,
                phraseId,
                CAMP_ID,
                groupId,
                title,
                content,
                sortNo
        );
    }
}
