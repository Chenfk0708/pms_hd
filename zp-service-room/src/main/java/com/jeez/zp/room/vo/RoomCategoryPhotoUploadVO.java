package com.jeez.zp.room.vo;

import lombok.Data;

@Data
public class RoomCategoryPhotoUploadVO {

    private String id;
    private String mediaResourceId;
    private String mediaId;
    private String sectionKey;
    private String name;
    private String url;
    private Long size;
    private String mimeType;
    private Integer sortOrder;
}
