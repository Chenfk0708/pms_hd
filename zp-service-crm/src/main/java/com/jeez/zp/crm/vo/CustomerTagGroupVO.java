package com.jeez.zp.crm.vo;

import lombok.Data;

import java.util.List;

@Data
public class CustomerTagGroupVO {

    private String tagGroupId;
    private String tagGroupName;
    private List<String> tagNames;
    private Integer memberCount;
    private Integer recentlyAddedCount;
    private String createdBy;
    private String createdAt;
    private String updatedAt;
    private String source;
    private String status;
    private String description;
}
