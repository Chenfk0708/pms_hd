package com.jeez.zp.room.service;

import com.jeez.zp.room.dto.request.RoomTypeUtilityRequest;
import com.jeez.zp.room.vo.RoomTypeFloorResponseVO;
import com.jeez.zp.room.vo.RoomTypeTagResponseVO;

public interface RoomTypeUtilityService {

    RoomTypeFloorResponseVO getFloors(RoomTypeUtilityRequest request, Long userId);

    RoomTypeTagResponseVO getTags(RoomTypeUtilityRequest request, Long userId);
}
