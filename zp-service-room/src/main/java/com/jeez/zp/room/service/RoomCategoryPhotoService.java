package com.jeez.zp.room.service;

import com.jeez.zp.room.vo.RoomCategoryPhotoUploadVO;
import org.springframework.web.multipart.MultipartFile;

public interface RoomCategoryPhotoService {

    RoomCategoryPhotoUploadVO uploadPhoto(
            Long campId,
            Long userId,
            Long roomCategoryId,
            String sectionKey,
            MultipartFile file
    );
}
