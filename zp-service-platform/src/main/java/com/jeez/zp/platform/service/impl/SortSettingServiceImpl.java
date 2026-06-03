package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.dto.request.ChannelRoomCategorySeqsRequest;
import com.jeez.zp.platform.dto.request.RoomCategorySeqsRequest;
import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.mapper.SortSettingMapper;
import com.jeez.zp.platform.service.SortSettingService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.SortSettingMutationResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SortSettingServiceImpl implements SortSettingService {

    private final SortSettingMapper sortSettingMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    @Transactional
    public SortSettingMutationResultVO updateRoomCategorySeqs(Long campId, Long userId, RoomCategorySeqsRequest request) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        List<RoomCategorySeqsRequest.RoomCategorySeqItem> items = request == null ? List.of() : request.getRoomCategorySeqs();
        if (items == null || items.isEmpty()) {
            throw new BusinessException(40002, "roomCategorySeqs is required");
        }

        List<SeqItem> normalized = normalizeSeqItems(items.stream()
                .map(item -> new SeqItem(parseLong(item.getRoomCategoryId()), item.getSeq()))
                .toList(), "roomCategorySeqs");

        for (SeqItem item : normalized) {
            int affected = sortSettingMapper.updateRoomCategorySortNo(resolvedCampId, item.id(), item.sortNo(), userId);
            if (affected == 0) {
                throw new BusinessException(40404, "room category not found in current camp");
            }
        }
        return SortSettingMutationResultVO.of("room category sort updated", normalized.stream().map(item -> String.valueOf(item.id())).toList());
    }

    @Override
    @Transactional
    public SortSettingMutationResultVO updateChannelRoomCategorySeqs(Long campId, Long userId, ChannelRoomCategorySeqsRequest request) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        List<ChannelRoomCategorySeqsRequest.ChannelRoomCategorySeqItem> items = request == null ? List.of() : request.getChannelRoomCategorySeqs();
        if (items == null || items.isEmpty()) {
            throw new BusinessException(40002, "channelRoomCategorySeqs is required");
        }

        List<SeqItem> normalized = normalizeSeqItems(items.stream()
                .map(item -> new SeqItem(parseLong(item.getChannelRoomCategoryId()), item.getSeq()))
                .toList(), "channelRoomCategorySeqs");

        for (SeqItem item : normalized) {
            int affected = sortSettingMapper.updateGoodsSortRemark(resolvedCampId, item.id(), item.sortNo(), userId);
            if (affected == 0) {
                throw new BusinessException(40404, "goods not found in current camp");
            }
        }
        return SortSettingMutationResultVO.of("goods sort updated", normalized.stream().map(item -> String.valueOf(item.id())).toList());
    }

    private List<SeqItem> normalizeSeqItems(List<SeqItem> items, String fieldName) {
        List<SeqItem> normalized = items.stream()
                .filter(item -> item.id() != null)
                .map(item -> new SeqItem(item.id(), item.seq() == null || item.seq() < 0 ? 1 : item.seq() + 1))
                .sorted((left, right) -> {
                    int compare = Integer.compare(left.sortNo(), right.sortNo());
                    return compare != 0 ? compare : Long.compare(left.id(), right.id());
                })
                .toList();

        if (normalized.isEmpty()) {
            throw new BusinessException(40002, fieldName + " must contain valid ids");
        }

        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        for (SeqItem item : normalized) {
            if (!ids.add(item.id())) {
                throw new BusinessException(40002, fieldName + " contains duplicate ids");
            }
        }
        return normalized;
    }

    private Long resolveAccessibleCampId(Long requestedCampId, Long userId) {
        CurrentUserBundleVO bundle = platformBootstrapMapper.selectCurrentUserBundle(userId);
        if (bundle == null) {
            throw new BusinessException(40401, "current user context not found");
        }
        if (requestedCampId == null) {
            return bundle.getCampId();
        }
        if (!Objects.equals(requestedCampId, bundle.getCampId())) {
            throw new BusinessException(40301, "no permission to access current camp sort data");
        }
        return requestedCampId;
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private record SeqItem(Long id, Integer seq) {
        private Integer sortNo() {
            return seq;
        }
    }
}
