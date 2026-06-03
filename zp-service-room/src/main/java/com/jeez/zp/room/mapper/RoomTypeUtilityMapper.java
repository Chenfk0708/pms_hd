package com.jeez.zp.room.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RoomTypeUtilityMapper {

    List<RoomTypeUtilityRow> selectFloors(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("keyword") String keyword
    );

    List<RoomTypeUtilityRow> selectTags(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("keyword") String keyword
    );

    List<RoomTypeOptionRow> selectRoomTypeOptions(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId
    );
}
