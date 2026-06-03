package com.jeez.zp.room.service.impl;

import com.jeez.zp.room.exception.BusinessException;
import com.jeez.zp.room.mapper.RoomCategoryPricingMapper;
import com.jeez.zp.room.mapper.UserCampMapper;
import com.jeez.zp.room.service.RoomCategoryPricingService;
import com.jeez.zp.room.vo.RoomCategoryPricingBodyRowVO;
import com.jeez.zp.room.vo.RoomCategoryPricingCellVO;
import com.jeez.zp.room.vo.RoomCategoryPricingHeadCellVO;
import com.jeez.zp.room.vo.RoomCategoryPricingRowVO;
import com.jeez.zp.room.vo.RoomCategoryPricingTableResponseVO;
import com.jeez.zp.room.vo.RetailSalePriceSettingVO;
import com.jeez.zp.room.vo.StoresPriceShowVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomCategoryPricingServiceImpl implements RoomCategoryPricingService {

    private static final List<String> FEE_COLUMNS = List.of(
            "\u62bc\u91d1",
            "\u53ef\u52a0\u5ba2\u4eba\u6570",
            "\u52a0\u4eba\u8d39(\u6bcf\u4eba)",
            "\u9910\u98df\u6570\u91cf",
            "\u4f63\u91d1\u7387(%)"
    );

    private static final List<String> LONG_STAY_COLUMNS = List.of(
            "\u8fde\u4f4f2\u5929\u4ee5\u4e0a",
            "\u8fde\u4f4f3\u5929\u4ee5\u4e0a",
            "\u8fde\u4f4f4\u5929\u4ee5\u4e0a",
            "\u8fde\u4f4f5\u5929\u4ee5\u4e0a",
            "\u8fde\u4f4f7\u5929\u4ee5\u4e0a",
            "\u8fde\u4f4f30\u5929\u4ee5\u4e0a",
            "\u8fde\u4f4f35\u5929\u4ee5\u4e0a"
    );

    private static final List<String> FLASH_SALE_COLUMNS = List.of(
            "\u7529\u5356\u7b2c\u4e00\u9636\u6bb5",
            "\u7529\u5356\u7b2c\u4e8c\u9636\u6bb5"
    );

    private final RoomCategoryPricingMapper roomCategoryPricingMapper;
    private final UserCampMapper userCampMapper;

    @Override
    public RoomCategoryPricingTableResponseVO getPricings(
            Long campId,
            Long userId,
            List<String> roomCategoryIds,
            List<String> channelIds
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId, "\u5176\u4ed6\u4ef7\u683c\u6570\u636e");
        List<RoomCategoryPricingRowVO> rows = roomCategoryPricingMapper.selectRows(
                resolvedCampId,
                parseLongList(roomCategoryIds),
                parseLongList(channelIds)
        );

        RoomCategoryPricingTableResponseVO response = new RoomCategoryPricingTableResponseVO();
        response.setHead(buildHead(FEE_COLUMNS));
        response.setBody(rows.stream().filter(this::hasChannel).map(this::toFeeBodyRow).toList());
        return response;
    }

    @Override
    public RoomCategoryPricingTableResponseVO getRules(
            Long campId,
            Long userId,
            List<String> roomCategoryIds,
            List<String> channelIds,
            Integer discountType
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId, "\u5176\u4ed6\u4ef7\u683c\u89c4\u5219");
        List<String> columns = discountType != null && discountType == 2 ? FLASH_SALE_COLUMNS : LONG_STAY_COLUMNS;
        List<RoomCategoryPricingRowVO> rows = roomCategoryPricingMapper.selectRows(
                resolvedCampId,
                parseLongList(roomCategoryIds),
                parseLongList(channelIds)
        );

        RoomCategoryPricingTableResponseVO response = new RoomCategoryPricingTableResponseVO();
        response.setHead(buildHead(columns));
        response.setBody(rows.stream().filter(this::hasChannel).map(row -> toRuleBodyRow(row, columns)).toList());
        return response;
    }

    @Override
    public RetailSalePriceSettingVO getSalePriceSetting(Long campId, Long userId) {
        resolveAccessibleCampId(campId, userId, "\u95e8\u5e02\u4ef7\u8bbe\u7f6e");
        RetailSalePriceSettingVO response = new RetailSalePriceSettingVO();
        response.setIsInitPriceDisplay(0);
        response.setPricePriceInterfaceDisplayType("2");
        response.setPriceSalePriceSettings(List.of());
        return response;
    }

    @Override
    public StoresPriceShowVO getStoresPriceShow(Long campId, Long userId) {
        resolveAccessibleCampId(campId, userId, "\u95e8\u5e02\u4ef7\u95e8\u5e97\u5c55\u793a\u8bbe\u7f6e");
        StoresPriceShowVO response = new StoresPriceShowVO();
        response.setDisplayMode("allStores");
        response.setIsShowStoresPrice(1);
        return response;
    }

    private List<RoomCategoryPricingHeadCellVO> buildHead(List<String> columns) {
        return columns.stream().map(this::toHeadCell).toList();
    }

    private RoomCategoryPricingHeadCellVO toHeadCell(String column) {
        RoomCategoryPricingHeadCellVO headCell = new RoomCategoryPricingHeadCellVO();
        headCell.setCellName(column);
        headCell.setTn(column);
        return headCell;
    }

    private RoomCategoryPricingBodyRowVO toFeeBodyRow(RoomCategoryPricingRowVO row) {
        RoomCategoryPricingBodyRowVO bodyRow = initBodyRow(row);
        List<RoomCategoryPricingCellVO> cells = new ArrayList<>();
        cells.add(toCell(FEE_COLUMNS.get(0), null));
        cells.add(toCell(FEE_COLUMNS.get(1), null));
        cells.add(toCell(FEE_COLUMNS.get(2), null));
        cells.add(toCell(FEE_COLUMNS.get(3), null));
        cells.add(toCell(FEE_COLUMNS.get(4), row.getCommissionRate()));
        bodyRow.setCells(cells);
        return bodyRow;
    }

    private RoomCategoryPricingBodyRowVO toRuleBodyRow(RoomCategoryPricingRowVO row, List<String> columns) {
        RoomCategoryPricingBodyRowVO bodyRow = initBodyRow(row);
        bodyRow.setCells(columns.stream().map(column -> toCell(column, null)).toList());
        return bodyRow;
    }

    private RoomCategoryPricingBodyRowVO initBodyRow(RoomCategoryPricingRowVO row) {
        RoomCategoryPricingBodyRowVO bodyRow = new RoomCategoryPricingBodyRowVO();
        bodyRow.setRoomCategoryId(row.getRoomCategoryId());
        bodyRow.setRcpi(row.getRoomCategoryId());
        bodyRow.setRoomCategoryName(row.getRoomCategoryName());
        bodyRow.setRcn(row.getRoomCategoryName());
        bodyRow.setChannelId(row.getChannelId());
        bodyRow.setChannelName(row.getChannelName());
        bodyRow.setCn(row.getChannelName());
        return bodyRow;
    }

    private boolean hasChannel(RoomCategoryPricingRowVO row) {
        return row.getChannelId() != null && !row.getChannelId().isBlank();
    }

    private RoomCategoryPricingCellVO toCell(String column, Object value) {
        RoomCategoryPricingCellVO cell = new RoomCategoryPricingCellVO();
        cell.setCellName(column);
        cell.setKey(column);
        cell.setValue(value);
        return cell;
    }

    private List<Long> parseLongList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        List<Long> parsed = values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(Long::valueOf)
                .toList();
        return parsed.isEmpty() ? null : parsed;
    }

    private Long resolveAccessibleCampId(Long requestedCampId, Long userId, String dataName) {
        Long currentCampId = userCampMapper.selectCurrentCampId(userId);
        if (currentCampId == null) {
            throw new BusinessException(40401, "\u672a\u627e\u5230\u5f53\u524d\u7528\u6237\u95e8\u5e97");
        }
        if (requestedCampId == null) {
            return currentCampId;
        }
        if (!requestedCampId.equals(currentCampId)) {
            throw new BusinessException(40301, "\u65e0\u6743\u8bbf\u95ee\u5f53\u524d\u95e8\u5e97" + dataName);
        }
        return requestedCampId;
    }
}
