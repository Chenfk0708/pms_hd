package com.jeez.zp.room.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.jeez.common.service.ImageService;
import com.jeez.zp.room.exception.BusinessException;
import com.jeez.zp.room.mapper.RoomCategoryPhotoMapper;
import com.jeez.zp.room.mapper.UserCampMapper;
import com.jeez.zp.room.service.RoomCategoryPhotoService;
import com.jeez.zp.room.vo.RoomCategoryPhotoUploadVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoomCategoryPhotoServiceImpl implements RoomCategoryPhotoService {

    private static final String BIZ_TYPE = "room_category";
    private static final Set<String> PHOTO_SECTION_KEYS = Set.of(
            "cover",
            "livingRoom",
            "kitchen",
            "other",
            "bathroom",
            "building",
            "entertainment",
            "uncategorized"
    );

    private final RoomCategoryPhotoMapper roomCategoryPhotoMapper;
    private final UserCampMapper userCampMapper;
    private final ObjectProvider<ImageService> imageServices;

    @Override
    @Transactional
    public RoomCategoryPhotoUploadVO uploadPhoto(
            Long campId,
            Long userId,
            Long roomCategoryId,
            String sectionKey,
            MultipartFile file
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        validateFile(file);

        String resolvedSectionKey = normalizeSectionKey(sectionKey);
        if (roomCategoryId != null && roomCategoryPhotoMapper.countRoomCategory(resolvedCampId, roomCategoryId) == 0) {
            throw new BusinessException(40404, "房型不存在");
        }

        ImageService imageService = imageServices.getIfAvailable();
        if (imageService == null) {
            throw new BusinessException(503, "图片上传服务未启用，请配置 image.storage.type");
        }

        String folderPath = "room-category/" + resolvedCampId + "/" + resolvedSectionKey;
        String url = imageService.uploadImage(file, folderPath);
        if (url == null || url.isBlank()) {
            throw new BusinessException(500, "图片上传服务未返回URL");
        }

        Long mediaResourceId = IdWorker.getId();
        String filename = normalizeFilename(file.getOriginalFilename());
        String format = extractExtension(filename);
        roomCategoryPhotoMapper.insertMediaResource(
                mediaResourceId,
                resolvedCampId,
                buildMediaPath(folderPath, mediaResourceId, format),
                filename,
                format,
                file.getSize(),
                url,
                BIZ_TYPE
        );

        Long mediaId = null;
        Integer sortOrder = 0;
        if (roomCategoryId != null) {
            sortOrder = roomCategoryPhotoMapper.selectNextSortNo(resolvedCampId, roomCategoryId, resolvedSectionKey);
            mediaId = IdWorker.getId();
            roomCategoryPhotoMapper.insertRoomCategoryMedia(
                    mediaId,
                    resolvedCampId,
                    roomCategoryId,
                    mediaResourceId,
                    url,
                    resolvedSectionKey,
                    sortOrder
            );
        }

        RoomCategoryPhotoUploadVO result = new RoomCategoryPhotoUploadVO();
        result.setId(String.valueOf(mediaResourceId));
        result.setMediaResourceId(String.valueOf(mediaResourceId));
        result.setMediaId(mediaId == null ? "" : String.valueOf(mediaId));
        result.setSectionKey(resolvedSectionKey);
        result.setName(filename);
        result.setUrl(url);
        result.setSize(file.getSize());
        result.setMimeType(file.getContentType() == null ? "image/*" : file.getContentType());
        result.setSortOrder(sortOrder);
        return result;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(40001, "图片文件不能为空");
        }
        String contentType = file.getContentType();
        if (contentType != null && !contentType.startsWith("image/")) {
            throw new BusinessException(40001, "只能上传图片文件");
        }
    }

    private Long resolveAccessibleCampId(Long requestedCampId, Long userId) {
        Long currentCampId = userCampMapper.selectCurrentCampId(userId);
        if (currentCampId == null) {
            throw new BusinessException(40401, "未找到当前用户门店");
        }
        if (requestedCampId == null) {
            return currentCampId;
        }
        if (!requestedCampId.equals(currentCampId)) {
            throw new BusinessException(40301, "无权访问当前门店房型数据");
        }
        return requestedCampId;
    }

    private String normalizeSectionKey(String sectionKey) {
        if (sectionKey == null || sectionKey.isBlank()) {
            return "uncategorized";
        }
        String trimmed = sectionKey.trim();
        return PHOTO_SECTION_KEYS.contains(trimmed) ? trimmed : "uncategorized";
    }

    private String normalizeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "room-photo";
        }
        String filename = originalFilename.replace('\\', '/');
        int slashIndex = filename.lastIndexOf('/');
        if (slashIndex >= 0) {
            filename = filename.substring(slashIndex + 1);
        }
        filename = filename.replaceAll("[^A-Za-z0-9._-]", "_");
        return filename.isBlank() ? "room-photo" : filename;
    }

    private String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase();
    }

    private String buildMediaPath(String folderPath, Long mediaResourceId, String format) {
        String suffix = format == null || format.isBlank() ? "" : "." + format;
        return folderPath + "/" + mediaResourceId + suffix;
    }
}
