package com.jeez.zp.room.mapper;

import org.apache.ibatis.annotations.Param;

public interface RoomCategoryPhotoMapper {

    int countRoomCategory(@Param("campId") Long campId, @Param("roomCategoryId") Long roomCategoryId);

    int insertMediaResource(
            @Param("mediaResourceId") Long mediaResourceId,
            @Param("campId") Long campId,
            @Param("path") String path,
            @Param("name") String name,
            @Param("format") String format,
            @Param("sizeBytes") Long sizeBytes,
            @Param("url") String url,
            @Param("bizType") String bizType
    );

    Integer selectNextSortNo(
            @Param("campId") Long campId,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("sceneType") String sceneType
    );

    int insertRoomCategoryMedia(
            @Param("mediaId") Long mediaId,
            @Param("campId") Long campId,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("mediaResourceId") Long mediaResourceId,
            @Param("mediaUrl") String mediaUrl,
            @Param("sceneType") String sceneType,
            @Param("sortNo") Integer sortNo
    );
}
