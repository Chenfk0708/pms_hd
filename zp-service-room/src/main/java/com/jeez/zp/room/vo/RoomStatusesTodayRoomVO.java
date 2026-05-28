package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomStatusesTodayRoomVO {

    private String roomId;
    private String roomName;
    private Integer roomSeq;
    private Integer isDirty;
    private String roomCategoryId;
    private String roomCategoryName;
    private String floorId;
    private Integer isOcc;
    private Integer isLive;
    private Integer isIdle;
    private Integer isPreCome;
    private Integer isPreLeave;
    private Integer isLinkCardDevice;
    private Integer isLinkPasswordDevice;
    private String passwordDeviceId;
    private Integer isArrangeSameRoom;
    private Integer isOrderRemark;
    private Integer isLt;
    private Integer isDebt;
    private Integer isHourRoomOrder;
    private Integer isExtendStay;
    private Integer isInvitationExtendStay;
    private Integer occupationType;
    private String occupationRemark;
    private String guestName;
    private List<RoomStatusesTodayOrderVO> orders;
    private Integer roomCategorySeq;
}
