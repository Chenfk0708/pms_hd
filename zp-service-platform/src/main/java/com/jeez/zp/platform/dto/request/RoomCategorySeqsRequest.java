package com.jeez.zp.platform.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class RoomCategorySeqsRequest {

    private String campId;
    private List<RoomCategorySeqItem> roomCategorySeqs;

    @Data
    public static class RoomCategorySeqItem {
        private String roomCategoryId;
        private Integer seq;
    }
}
