package com.jeez.zp.room.controller;

import com.jeez.zp.room.api.HudsonResponse;
import com.jeez.zp.room.api.TraceIdFactory;
import com.jeez.zp.room.security.LoginUserContext;
import com.jeez.zp.room.service.RoomCategoryPhotoService;
import com.jeez.zp.room.vo.RoomCategoryPhotoUploadVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class RoomCategoryPhotoController {

    private final RoomCategoryPhotoService roomCategoryPhotoService;

    @PostMapping("/roomCategory/photo/upload")
    public HudsonResponse<RoomCategoryPhotoUploadVO> uploadPhoto(
            @RequestParam(value = "campId", required = false) String campId,
            @RequestParam(value = "roomCategoryId", required = false) String roomCategoryId,
            @RequestParam(value = "sectionKey", required = false) String sectionKey,
            @RequestParam("file") MultipartFile file
    ) {
        return HudsonResponse.success(
                roomCategoryPhotoService.uploadPhoto(
                        parseLong(campId),
                        LoginUserContext.requiredUserId(),
                        parseLong(roomCategoryId),
                        sectionKey,
                        file
                ),
                TraceIdFactory.next("room-category-photo-upload")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
