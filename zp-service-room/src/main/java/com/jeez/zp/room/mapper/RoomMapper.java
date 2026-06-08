package com.jeez.zp.room.mapper;

import com.jeez.zp.room.vo.RoomCategoryRoomsGroupVO;
import com.jeez.zp.room.vo.RoomItemVO;
import com.jeez.zp.room.vo.RoomPageItemVO;
import com.jeez.zp.room.vo.RoomStatusesRoomsCategoryVO;
import com.jeez.zp.room.vo.RoomStatusesRoomsRoomVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RoomMapper {

    List<RoomCategoryRoomsGroupVO> selectRoomCategoryGroups(
            @Param("campId") Long campId,
            @Param("roomCategoryIds") List<Long> roomCategoryIds
    );

    List<RoomItemVO> selectRooms(
            @Param("campId") Long campId,
            @Param("roomCategoryIds") List<Long> roomCategoryIds,
            @Param("saleType") Integer saleType
    );

    long countRoomsPage(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("roomCategoryIds") List<Long> roomCategoryIds,
            @Param("isAvailability") Integer isAvailability,
            @Param("saleType") Integer saleType,
            @Param("keyword") String keyword
    );

    List<RoomPageItemVO> selectRoomsPage(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("roomCategoryIds") List<Long> roomCategoryIds,
            @Param("isAvailability") Integer isAvailability,
            @Param("saleType") Integer saleType,
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("pageSize") int pageSize
    );

    long countRoomStatusesRoomCategories(
            @Param("campId") Long campId,
            @Param("roomCategoryIds") List<Long> roomCategoryIds,
            @Param("poiIds") List<Long> poiIds,
            @Param("keyword") String keyword,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );

    List<RoomStatusesRoomsCategoryVO> selectRoomStatusesRoomCategories(
            @Param("campId") Long campId,
            @Param("roomCategoryIds") List<Long> roomCategoryIds,
            @Param("poiIds") List<Long> poiIds,
            @Param("keyword") String keyword,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("offset") long offset,
            @Param("pageSize") int pageSize
    );

    List<RoomStatusesRoomsRoomVO> selectRoomStatusesRooms(
            @Param("campId") Long campId,
            @Param("roomCategoryIds") List<Long> roomCategoryIds,
            @Param("poiIds") List<Long> poiIds,
            @Param("keyword") String keyword,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );
}
