package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.service.RoomCategoryChannelService;
import com.jeez.zp.platform.vo.ChannelVO;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.RoomCategoryChannelOptionVO;
import com.jeez.zp.platform.vo.RoomCategoryChannelOptionsResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoomCategoryChannelServiceImpl implements RoomCategoryChannelService {

    private static final String AUTHORIZED_STATUS = "authorized";
    private static final long SELF_CHANNEL_ID = 0L;

    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public RoomCategoryChannelOptionsResponseVO getRoomCategoryChannels(Long campId, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);

        Map<Long, RoomCategoryChannelOptionVO> deduplicatedOptions = new LinkedHashMap<>();
        for (ChannelVO channel : platformBootstrapMapper.selectChannelsByCampId(resolvedCampId)) {
            if (!isAvailableChannel(channel)) {
                continue;
            }
            deduplicatedOptions.putIfAbsent(channel.getChannelId(), toOptionVO(channel));
        }

        List<RoomCategoryChannelOptionVO> options = deduplicatedOptions.values().stream().toList();

        RoomCategoryChannelOptionsResponseVO response = new RoomCategoryChannelOptionsResponseVO();
        response.setSelect(options);
        response.setList(options);
        return response;
    }

    private boolean isAvailableChannel(ChannelVO channel) {
        return channel != null
                && channel.getChannelId() != null
                && channel.getChannelId() != SELF_CHANNEL_ID
                && isAuthorized(channel.getStatus());
    }

    private boolean isAuthorized(String status) {
        return status != null && AUTHORIZED_STATUS.equalsIgnoreCase(status.trim());
    }

    private RoomCategoryChannelOptionVO toOptionVO(ChannelVO channel) {
        String channelId = String.valueOf(channel.getChannelId());
        String channelName = normalizeChannelName(channel);

        RoomCategoryChannelOptionVO option = new RoomCategoryChannelOptionVO();
        option.setChannelId(channelId);
        option.setChannelName(channelName);
        option.setId(channelId);
        option.setName(channelName);
        return option;
    }

    private String normalizeChannelName(ChannelVO channel) {
        if (channel.getChannelName() == null || channel.getChannelName().isBlank()) {
            return "渠道" + channel.getChannelId();
        }
        return channel.getChannelName().trim();
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
            throw new BusinessException(40301, "无权访问当前门店房型渠道数据");
        }
        return requestedCampId;
    }
}
