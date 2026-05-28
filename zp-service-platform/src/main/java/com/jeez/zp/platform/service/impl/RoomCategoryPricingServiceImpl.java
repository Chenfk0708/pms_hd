package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.mapper.RoomCategoryPricingMapper;
import com.jeez.zp.platform.service.RoomCategoryPricingService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.RoomCategoryPricingBodyRowVO;
import com.jeez.zp.platform.vo.RoomCategoryPricingCellVO;
import com.jeez.zp.platform.vo.RoomCategoryPricingHeadCellVO;
import com.jeez.zp.platform.vo.RoomCategoryPricingRowVO;
import com.jeez.zp.platform.vo.RoomCategoryPricingTableResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomCategoryPricingServiceImpl implements RoomCategoryPricingService {

    private static final List<String> FEE_COLUMNS = List.of(
            "押金",
            "可加客人数",
            "加人费(每人)",
            "餐食数量",
            "佣金率(%)"
    );

    private static final List<String> LONG_STAY_COLUMNS = List.of(
            "连住2天以上",
            "连住3天以上",
            "连住4天以上",
            "连住5天以上",
            "连住7天以上",
            "连住30天以上",
            "连住35天以上"
    );

    private static final List<String> FLASH_SALE_COLUMNS = List.of(
            "甩卖第一阶段",
            "甩卖第二阶段"
    );

    private final RoomCategoryPricingMapper roomCategoryPricingMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public RoomCategoryPricingTableResponseVO getPricings(
            Long campId,
            Long userId,
            List<String> roomCategoryIds,
            List<String> channelIds
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        List<RoomCategoryPricingRowVO> rows = roomCategoryPricingMapper.selectRows(
                resolvedCampId,
                parseLongList(roomCategoryIds),
                parseLongList(channelIds)
        );

        RoomCategoryPricingTableResponseVO response = new RoomCategoryPricingTableResponseVO();
        response.setHead(buildHead(FEE_COLUMNS));
        response.setBody(rows.stream().map(this::toFeeBodyRow).toList());
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
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        List<RoomCategoryPricingRowVO> rows = roomCategoryPricingMapper.selectRows(
                resolvedCampId,
                parseLongList(roomCategoryIds),
                parseLongList(channelIds)
        );

        List<String> columns = resolveRuleColumns(discountType);

        RoomCategoryPricingTableResponseVO response = new RoomCategoryPricingTableResponseVO();
        response.setHead(buildHead(columns));
        response.setBody(rows.stream().map(row -> toRuleBodyRow(row, columns)).toList());
        return response;
    }

    private List<String> resolveRuleColumns(Integer discountType) {
        if (discountType != null && discountType == 2) {
            return FLASH_SALE_COLUMNS;
        }
        return LONG_STAY_COLUMNS;
    }

    private List<RoomCategoryPricingHeadCellVO> buildHead(List<String> columns) {
        return columns.stream()
                .map(this::toHeadCell)
                .toList();
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
        bodyRow.setCells(columns.stream()
                .map(column -> toCell(column, null))
                .toList());
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

    private Long resolveAccessibleCampId(Long requestedCampId, Long userId) {
        CurrentUserBundleVO bundle = platformBootstrapMapper.selectCurrentUserBundle(userId);
        if (bundle == null) {
            throw new BusinessException(40401, "当前用户上下文不存在");
        }
        if (requestedCampId == null) {
            return bundle.getCampId();
        }
        if (!requestedCampId.equals(bundle.getCampId())) {
            throw new BusinessException(40301, "无权访问当前门店其他价格数据");
        }
        return requestedCampId;
    }
}
