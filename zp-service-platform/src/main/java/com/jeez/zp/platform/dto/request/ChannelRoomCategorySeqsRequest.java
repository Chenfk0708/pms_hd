package com.jeez.zp.platform.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ChannelRoomCategorySeqsRequest {

    private String campId;
    private List<ChannelRoomCategorySeqItem> channelRoomCategorySeqs;

    @Data
    public static class ChannelRoomCategorySeqItem {
        private String channelRoomCategoryId;
        private Integer seq;
    }
}
