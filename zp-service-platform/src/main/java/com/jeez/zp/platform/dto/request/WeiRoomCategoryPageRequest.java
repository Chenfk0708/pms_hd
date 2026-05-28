package com.jeez.zp.platform.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class WeiRoomCategoryPageRequest {

    private String campId;
    private String buyCampId;
    private List<Integer> roomCategoryTypes;
    private List<Integer> goodsTypes;
    private Integer pageNum;
    private Integer pageSize;
    private String keyword;
}
