package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class RoomCategoryMutationResultVO {

    private String roomCategoryId;
    private String message;

    public static RoomCategoryMutationResultVO of(Long roomCategoryId, String message) {
        RoomCategoryMutationResultVO result = new RoomCategoryMutationResultVO();
        result.setRoomCategoryId(roomCategoryId == null ? null : String.valueOf(roomCategoryId));
        result.setMessage(message);
        return result;
    }
}
