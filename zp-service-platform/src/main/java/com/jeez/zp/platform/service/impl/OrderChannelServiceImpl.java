package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.service.OrderChannelService;
import com.jeez.zp.platform.vo.ChannelVO;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.OrderChannelOptionVO;
import com.jeez.zp.platform.vo.OrderChannelOptionsResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderChannelServiceImpl implements OrderChannelService {

    private static final String AUTHORIZED_STATUS = "authorized";
    private static final String SELF_CHANNEL_ID = "0";
    private static final String DEFAULT_IMAGE =
            "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/Bitmap/Bitmap.png";

    private static final Map<String, OrderChannelCatalogItem> CHANNEL_CATALOG = createChannelCatalog();

    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public OrderChannelOptionsResponseVO getOrderChannels(Long campId, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);

        List<OrderChannelOptionVO> options = new ArrayList<>();
        options.add(toOptionVO(CHANNEL_CATALOG.get(SELF_CHANNEL_ID)));

        Map<Long, ChannelVO> authorizedChannels = new LinkedHashMap<>();
        for (ChannelVO channel : platformBootstrapMapper.selectChannelsByCampId(resolvedCampId)) {
            if (channel.getChannelId() == null || !isAuthorized(channel.getStatus())) {
                continue;
            }
            if (channel.getChannelId() == 0L || authorizedChannels.containsKey(channel.getChannelId())) {
                continue;
            }
            authorizedChannels.put(channel.getChannelId(), channel);
        }

        for (ChannelVO channel : authorizedChannels.values()) {
            options.add(toOptionVO(resolveCatalogItem(channel)));
        }

        OrderChannelOptionsResponseVO response = new OrderChannelOptionsResponseVO();
        response.setSelect(options);
        response.setList(options);
        return response;
    }

    private boolean isAuthorized(String status) {
        return status != null && AUTHORIZED_STATUS.equalsIgnoreCase(status.trim());
    }

    private OrderChannelCatalogItem resolveCatalogItem(ChannelVO channel) {
        String channelId = String.valueOf(channel.getChannelId());
        OrderChannelCatalogItem catalogItem = CHANNEL_CATALOG.get(channelId);
        if (catalogItem != null) {
            return catalogItem;
        }

        String channelName = channel.getChannelName() == null || channel.getChannelName().isBlank()
                ? "渠道" + channelId
                : channel.getChannelName().trim();
        String shortChannelName = channelName.length() <= 4 ? channelName : channelName.substring(0, 4);
        return new OrderChannelCatalogItem(
                channelId,
                channelName,
                shortChannelName,
                "#6f89d1",
                DEFAULT_IMAGE,
                DEFAULT_IMAGE,
                DEFAULT_IMAGE,
                0,
                0,
                1
        );
    }

    private OrderChannelOptionVO toOptionVO(OrderChannelCatalogItem item) {
        OrderChannelOptionVO option = new OrderChannelOptionVO();
        option.setChannelId(item.channelId());
        option.setChannelName(item.channelName());
        option.setShortChannelName(item.shortChannelName());
        option.setColor(item.color());
        option.setImageLogo(item.imageLogo());
        option.setImageOpen(item.imageOpen());
        option.setImageClose(item.imageClose());
        option.setIsSupportIcs(item.isSupportIcs());
        option.setIsLongRent(item.isLongRent());
        option.setIsOpen(item.isOpen());
        return option;
    }

    private Long resolveAccessibleCampId(Long requestedCampId, Long userId) {
        CurrentUserBundleVO bundle = platformBootstrapMapper.selectCurrentUserBundle(userId);
        if (bundle == null) {
            throw new BusinessException(40401, "褰撳墠鐢ㄦ埛涓婁笅鏂囦笉瀛樺湪");
        }
        if (requestedCampId == null) {
            return bundle.getCampId();
        }
        if (!requestedCampId.equals(bundle.getCampId())) {
            throw new BusinessException(40301, "鏃犳潈璁块棶褰撳墠闂ㄥ簵娓犻亾鏁版嵁");
        }
        return requestedCampId;
    }

    private static Map<String, OrderChannelCatalogItem> createChannelCatalog() {
        Map<String, OrderChannelCatalogItem> catalog = new LinkedHashMap<>();
        catalog.put("0", new OrderChannelCatalogItem(
                "0",
                "自来客",
                "自来客",
                "#6f89d1",
                DEFAULT_IMAGE,
                DEFAULT_IMAGE,
                DEFAULT_IMAGE,
                0,
                0,
                1
        ));
        catalog.put("1", new OrderChannelCatalogItem(
                "1",
                "爱彼迎",
                "爱彼迎",
                "#FF5A5F",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/bnb002.png",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/airbnb-blue.png",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/airbnb02.png",
                1,
                0,
                1
        ));
        catalog.put("2", new OrderChannelCatalogItem(
                "2",
                "途家",
                "途家",
                "#ff6d23",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/tujia001.png",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/tujia-blue.png",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/tujia02.png",
                1,
                0,
                1
        ));
        catalog.put("3", new OrderChannelCatalogItem(
                "3",
                "美团民宿",
                "美团",
                "#ffc20c",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/hudson/3575905411796521.png",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/zhenguo-blue.png",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/zhenguo02.png",
                1,
                0,
                1
        ));
        catalog.put("4", new OrderChannelCatalogItem(
                "4",
                "小猪",
                "小猪",
                "#ff4268",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/xiaozhu006.png",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/xiaozhu-blue.png",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/xiaozhu02.png",
                1,
                0,
                1
        ));
        catalog.put("5", new OrderChannelCatalogItem(
                "5",
                "携程",
                "携程",
                "#0868e5",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/hudson/8981066208472485.png",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/hudson/4384501156886349.png",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/hudson/5997150602615926.png",
                1,
                0,
                1
        ));
        catalog.put("6", new OrderChannelCatalogItem(
                "6",
                "美团酒店",
                "美团",
                "#08a6c8",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/WechatIMG671.png",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/meituanjiudian/16b2e6c6d734d13f7721da9fd993043415845.png",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/meituanjiudian/16b2e6c6d734d13f7721da9fd9930434158452.png",
                1,
                0,
                1
        ));
        catalog.put("8", new OrderChannelCatalogItem(
                "8",
                "飞猪淘酒店",
                "飞猪",
                "#edc36b",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/localhomeqy/channel/feizhu001.png",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/localhomeqy/channel/feizhu002.png",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/localhomeqy/channel/feizhu003.png",
                1,
                0,
                1
        ));
        catalog.put("9", new OrderChannelCatalogItem(
                "9",
                "Booking",
                "Booking",
                "#d3471d",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/booking004.png",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/booking-blue.png",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/booking02.png",
                1,
                0,
                1
        ));
        catalog.put("17", new OrderChannelCatalogItem(
                "17",
                "路客云聚合",
                "路客云",
                "#263f86",
                DEFAULT_IMAGE,
                DEFAULT_IMAGE,
                DEFAULT_IMAGE,
                1,
                0,
                1
        ));
        catalog.put("18", new OrderChannelCatalogItem(
                "18",
                "携程国际",
                "携程国际",
                "#24c2df",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/hudson/0842875468894826.png",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/hudson/5596654871754908.png",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/hudson/9359488859736707.png",
                1,
                0,
                1
        ));
        catalog.put("21", new OrderChannelCatalogItem(
                "21",
                "木鸟",
                "木鸟",
                "#ff792b",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/hudson_app_channels/muniao3.png",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/hudson_app_channels/muniao-blue.png",
                "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/hudson_app_channels/muniao-gray.png",
                1,
                0,
                1
        ));
        return catalog;
    }

    private record OrderChannelCatalogItem(
            String channelId,
            String channelName,
            String shortChannelName,
            String color,
            String imageLogo,
            String imageOpen,
            String imageClose,
            Integer isSupportIcs,
            Integer isLongRent,
            Integer isOpen
    ) {
    }
}
