package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.RoomCategoryPageItemVO;
import com.jeez.zp.platform.vo.RoomCategoryPhotoVO;
import com.jeez.zp.platform.vo.RoomCategoryDetailChannelPriceRowVO;
import com.jeez.zp.platform.vo.RoomCategoryDetailRowVO;
import com.jeez.zp.platform.vo.RoomCategoryRoomRowVO;
import com.jeez.zp.platform.vo.RoomCategoryRoomViewVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RoomCategoryMapper {

    long countPage(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("roomCategoryGroupId") Long roomCategoryGroupId,
            @Param("roomCategoryName") String roomCategoryName,
            @Param("keyword") String keyword,
            @Param("channelId") Long channelId
    );

    List<RoomCategoryPageItemVO> selectPage(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("roomCategoryGroupId") Long roomCategoryGroupId,
            @Param("roomCategoryName") String roomCategoryName,
            @Param("keyword") String keyword,
            @Param("channelId") Long channelId,
            @Param("offset") long offset,
            @Param("pageSize") int pageSize
    );

    List<RoomCategoryRoomViewVO> selectRoomViews(@Param("roomCategoryIds") List<Long> roomCategoryIds);

    RoomCategoryDetailRowVO selectDetail(@Param("campId") Long campId, @Param("roomCategoryId") Long roomCategoryId);

    List<RoomCategoryDetailChannelPriceRowVO> selectDetailChannelPriceRows(
            @Param("campId") Long campId,
            @Param("roomCategoryId") Long roomCategoryId
    );

    RoomCategoryPageItemVO selectEditDetail(@Param("campId") Long campId, @Param("roomCategoryId") Long roomCategoryId);

    List<RoomCategoryPageItemVO> selectLinkageCandidates(@Param("campId") Long campId, @Param("roomCategoryId") Long roomCategoryId);

    List<Long> selectLinkedRoomCategoryIds(@Param("campId") Long campId, @Param("roomCategoryId") Long roomCategoryId);

    List<RoomCategoryPhotoVO> selectPhotos(@Param("campId") Long campId, @Param("roomCategoryId") Long roomCategoryId);

    List<String> selectExistingActiveRoomNames(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("roomNames") List<String> roomNames
    );

    List<RoomCategoryRoomRowVO> selectActiveRoomsByCategory(
            @Param("campId") Long campId,
            @Param("roomCategoryId") Long roomCategoryId
    );

    int disableLinkages(@Param("campId") Long campId, @Param("roomCategoryId") Long roomCategoryId);

    int disableRoomCategoryMedia(@Param("campId") Long campId, @Param("roomCategoryId") Long roomCategoryId);

    int countMediaResource(@Param("campId") Long campId, @Param("mediaResourceId") Long mediaResourceId);

    Long selectMediaResourceIdByUrl(@Param("campId") Long campId, @Param("url") String url);

    int insertLinkage(
            @Param("id") Long id,
            @Param("campId") Long campId,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("linkedRoomCategoryId") Long linkedRoomCategoryId
    );

    int insertRoomCategoryMedia(
            @Param("mediaId") Long mediaId,
            @Param("campId") Long campId,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("mediaResourceId") Long mediaResourceId,
            @Param("mediaUrl") String mediaUrl,
            @Param("sceneType") String sceneType,
            @Param("sortNo") Integer sortNo
    );

    int insertRoomCategory(
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("groupId") Long groupId,
            @Param("name") String name,
            @Param("displayName") String displayName,
            @Param("roomCount") Integer roomCount,
            @Param("rentalType") String rentalType,
            @Param("propertyType") String propertyType,
            @Param("guestCount") Integer guestCount,
            @Param("suiteArea") java.math.BigDecimal suiteArea,
            @Param("bedroomCount") Integer bedroomCount,
            @Param("livingRoomCount") Integer livingRoomCount,
            @Param("kitchenCount") Integer kitchenCount,
            @Param("bathroomCount") Integer bathroomCount,
            @Param("bathroomType") String bathroomType,
            @Param("weekdayPriceCent") Long weekdayPriceCent,
            @Param("weekendPriceCent") Long weekendPriceCent,
            @Param("holidayPriceCent") Long holidayPriceCent,
            @Param("locationMode") String locationMode,
            @Param("locationProvinceCode") String locationProvinceCode,
            @Param("locationProvinceName") String locationProvinceName,
            @Param("locationCityCode") String locationCityCode,
            @Param("locationCityName") String locationCityName,
            @Param("locationDistrictCode") String locationDistrictCode,
            @Param("locationDistrictName") String locationDistrictName,
            @Param("streetAddress") String streetAddress,
            @Param("communityName") String communityName,
            @Param("buildingUnit") String buildingUnit,
            @Param("doorNumber") String doorNumber,
            @Param("locationLatitude") java.math.BigDecimal locationLatitude,
            @Param("locationLongitude") java.math.BigDecimal locationLongitude,
            @Param("earliestCheckInHour") Integer earliestCheckInHour,
            @Param("latestCheckInHour") Integer latestCheckInHour,
            @Param("latestCheckOutHour") Integer latestCheckOutHour,
            @Param("selectedFacilityIds") String selectedFacilityIds,
            @Param("bedSheetChangePolicy") String bedSheetChangePolicy,
            @Param("decorationStyle") String decorationStyle,
            @Param("highlightDescription") String highlightDescription,
            @Param("nearbyDescription") String nearbyDescription,
            @Param("articleDescription") String articleDescription,
            @Param("userId") Long userId
    );

    int updateRoomCategory(
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("groupId") Long groupId,
            @Param("name") String name,
            @Param("displayName") String displayName,
            @Param("roomCount") Integer roomCount,
            @Param("rentalType") String rentalType,
            @Param("propertyType") String propertyType,
            @Param("guestCount") Integer guestCount,
            @Param("suiteArea") java.math.BigDecimal suiteArea,
            @Param("bedroomCount") Integer bedroomCount,
            @Param("livingRoomCount") Integer livingRoomCount,
            @Param("kitchenCount") Integer kitchenCount,
            @Param("bathroomCount") Integer bathroomCount,
            @Param("bathroomType") String bathroomType,
            @Param("weekdayPriceCent") Long weekdayPriceCent,
            @Param("weekendPriceCent") Long weekendPriceCent,
            @Param("holidayPriceCent") Long holidayPriceCent,
            @Param("locationMode") String locationMode,
            @Param("locationProvinceCode") String locationProvinceCode,
            @Param("locationProvinceName") String locationProvinceName,
            @Param("locationCityCode") String locationCityCode,
            @Param("locationCityName") String locationCityName,
            @Param("locationDistrictCode") String locationDistrictCode,
            @Param("locationDistrictName") String locationDistrictName,
            @Param("streetAddress") String streetAddress,
            @Param("communityName") String communityName,
            @Param("buildingUnit") String buildingUnit,
            @Param("doorNumber") String doorNumber,
            @Param("locationLatitude") java.math.BigDecimal locationLatitude,
            @Param("locationLongitude") java.math.BigDecimal locationLongitude,
            @Param("earliestCheckInHour") Integer earliestCheckInHour,
            @Param("latestCheckInHour") Integer latestCheckInHour,
            @Param("latestCheckOutHour") Integer latestCheckOutHour,
            @Param("selectedFacilityIds") String selectedFacilityIds,
            @Param("bedSheetChangePolicy") String bedSheetChangePolicy,
            @Param("decorationStyle") String decorationStyle,
            @Param("highlightDescription") String highlightDescription,
            @Param("nearbyDescription") String nearbyDescription,
            @Param("articleDescription") String articleDescription,
            @Param("userId") Long userId
    );

    int deleteRoomsByCategory(@Param("campId") Long campId, @Param("roomCategoryId") Long roomCategoryId, @Param("userId") Long userId);

    int deleteRoomsByIds(
            @Param("campId") Long campId,
            @Param("roomIds") List<Long> roomIds,
            @Param("userId") Long userId
    );

    int countCurrentOrFutureOrdersByCategory(@Param("campId") Long campId, @Param("roomCategoryId") Long roomCategoryId);

    int countCurrentOrFutureOrdersByRooms(@Param("campId") Long campId, @Param("roomIds") List<Long> roomIds);

    int deleteCleanTasksByCategory(@Param("campId") Long campId, @Param("roomCategoryId") Long roomCategoryId);

    int deleteRoomCategory(@Param("campId") Long campId, @Param("roomCategoryId") Long roomCategoryId, @Param("userId") Long userId);

    int insertRoom(
            @Param("roomId") Long roomId,
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("roomName") String roomName,
            @Param("sortNo") Integer sortNo,
            @Param("userId") Long userId
    );

    int updateRoom(
            @Param("roomId") Long roomId,
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("roomName") String roomName,
            @Param("sortNo") Integer sortNo,
            @Param("userId") Long userId
    );
}
