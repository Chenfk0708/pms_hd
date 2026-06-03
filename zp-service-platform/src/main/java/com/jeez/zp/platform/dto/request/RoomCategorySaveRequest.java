package com.jeez.zp.platform.dto.request;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class RoomCategorySaveRequest {

    private String campId;
    private Form form;

    @Data
    public static class Form {
        private String roomTypeId;
        private String roomCategoryId;
        private String roomTypeName;
        private String roomCategoryName;
        private String storeId;
        private String poiId;
        private String groupId;
        private String roomCategoryGroupId;
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
        private List<Photo> photos;
        private Map<String, Integer> photoCounts;
    }

    @Data
    public static class Photo {
        private String id;
        private String mediaResourceId;
        private String mediaId;
        private String photoId;
        private String fileId;
        private String sectionKey;
        private String type;
        private String category;
        private String name;
        private String fileName;
        private String originalName;
        private String url;
        private String fileUrl;
        private String imageUrl;
        private String path;
        private String src;
        private Long size;
        private Long fileSize;
        private String mimeType;
        private String contentType;
        private Integer sortOrder;
        private Integer sort;
    }
}
