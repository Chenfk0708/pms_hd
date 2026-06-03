package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class RoomCategoryEditFormVO {

    private String roomTypeId;
    private String roomTypeName;
    private String storeId;
    private String groupId;
    private String roomCount;
    private List<String> roomNos;
    private String weekdayPrice;
    private String weekendPrice;
    private String holidayPrice;
    private String locationMode;
    private String locationProvinceCode;
    private String locationProvinceName;
    private String locationCityCode;
    private String locationCityName;
    private String locationDistrictCode;
    private String locationDistrictName;
    private String streetAddress;
    private String communityName;
    private String buildingUnit;
    private String doorNumber;
    private String locationLatitude;
    private String locationLongitude;
    private String rentalType;
    private String propertyType;
    private String suiteArea;
    private String guestCount;
    private String bedroomCount;
    private String livingRoomCount;
    private String kitchenCount;
    private String bathroomCount;
    private String bathroomType;
    private List<String> selectedFacilityIds;
    private String bedSheetChangePolicy;
    private String decorationStyle;
    private String displayName;
    private String earliestCheckIn;
    private String latestCheckOut;
    private String latestCheckIn;
    private String highlightDescription;
    private String nearbyDescription;
    private String articleDescription;
    private List<RoomCategoryPhotoVO> photos;
    private Map<String, Integer> photoCounts;
}
