package com.jeez.zp.crm.vo;

import lombok.Data;

@Data
public class CustomerTagGroupQueryRowVO {

    private String tagGroupId;
    private String tagGroupName;
    private String tagNamesCsv;
    private Integer memberCount;
    private Integer recentlyAddedCount;
    private String createdBy;
    private String createdAt;
    private String updatedAt;
    private String source;
    private String status;
    private String description;
}
