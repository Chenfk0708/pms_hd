package com.jeez.zp.room.controller;

import com.jeez.common.service.ImageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RoomCategoryPhotoUploadIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;
    private static final long USER_ID = 12001L;
    private static final long POI_ID = 11001L;
    private static final long GROUP_ID = 21001L;
    private static final long ROOM_CATEGORY_ID = 118001L;
    private static final String UPLOADED_URL = "https://assets.localhome.cn/room-type/cover.png";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private ImageService imageService;

    @Test
    @Timeout(60)
    void roomCategoryPhotoUpload_shouldStoreMediaResourceAndLinkRoomCategory() throws Exception {
        insertRoomCategory();
        when(imageService.uploadImage(any(), eq("room-category/10001/cover"))).thenReturn(UPLOADED_URL);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cover.png",
                "image/png",
                new byte[]{1, 2, 3, 4}
        );

        mockMvc.perform(multipart("/roomCategory/photo/upload")
                        .file(file)
                        .param("campId", String.valueOf(CAMP_ID))
                        .param("roomCategoryId", String.valueOf(ROOM_CATEGORY_ID))
                        .param("sectionKey", "cover")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.mediaResourceId").exists())
                .andExpect(jsonPath("$.data.sectionKey").value("cover"))
                .andExpect(jsonPath("$.data.name").value("cover.png"))
                .andExpect(jsonPath("$.data.url").value(UPLOADED_URL))
                .andExpect(jsonPath("$.data.size").value(4))
                .andExpect(jsonPath("$.data.mimeType").value("image/png"))
                .andExpect(jsonPath("$.data.sortOrder").value(1));

        Long mediaResourceId = jdbcTemplate.queryForObject("""
                SELECT media_resource_id
                FROM media_resource
                WHERE camp_id = ? AND url = ? AND biz_type = 'room_category' AND is_deleted = 0
                """, Long.class, CAMP_ID, UPLOADED_URL);

        assertThat(mediaResourceId).isNotNull();
        assertThat(jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM room_category_media
                WHERE camp_id = ?
                  AND room_category_id = ?
                  AND media_resource_id = ?
                  AND media_url = ?
                  AND scene_type = 'cover'
                  AND status = 1
                """, Integer.class, CAMP_ID, ROOM_CATEGORY_ID, mediaResourceId, UPLOADED_URL)).isEqualTo(1);
    }

    private void insertRoomCategory() {
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
                ROOM_CATEGORY_ID,
                CAMP_ID,
                POI_ID,
                GROUP_ID,
                "TDD上传房型",
                "TDD上传房型",
                1,
                1,
                10,
                0
        );
    }
}
