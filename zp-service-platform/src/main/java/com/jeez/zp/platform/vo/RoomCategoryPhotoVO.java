package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class RoomCategoryPhotoVO {

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
