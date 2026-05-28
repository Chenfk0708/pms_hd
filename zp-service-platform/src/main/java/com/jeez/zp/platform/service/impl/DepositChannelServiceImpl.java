package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.service.DepositChannelService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.DepositChannelOptionVO;
import com.jeez.zp.platform.vo.DepositChannelOptionsResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepositChannelServiceImpl implements DepositChannelService {

    private static final List<DepositChannelCatalogItem> SUPPORTED_CHANNELS = List.of(
            new DepositChannelCatalogItem("1", "爱彼迎", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/bnb002.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/airbnb-blue.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/airbnb02.png", 1),
            new DepositChannelCatalogItem("2", "途家", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/tujia001.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/tujia-blue.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/tujia02.png", 1),
            new DepositChannelCatalogItem("3", "美团民宿", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/hudson/3575905411796521.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/zhenguo-blue.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/zhenguo02.png", 1),
            new DepositChannelCatalogItem("4", "小猪", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/xiaozhu006.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/xiaozhu-blue.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/xiaozhu02.png", 1),
            new DepositChannelCatalogItem("5", "携程", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/hudson/8981066208472485.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/hudson/4384501156886349.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/hudson/5997150602615926.png", 1),
            new DepositChannelCatalogItem("6", "美团酒店", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/WechatIMG671.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/meituanjiudian/16b2e6c6d734d13f7721da9fd993043415845.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/meituanjiudian/16b2e6c6d734d13f7721da9fd9930434158452.png", 1),
            new DepositChannelCatalogItem("8", "飞猪淘酒店", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/localhomeqy/channel/feizhu001.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/localhomeqy/channel/feizhu002.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/localhomeqy/channel/feizhu003.png", 1),
            new DepositChannelCatalogItem("9", "Booking", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/booking004.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/booking-blue.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/booking02.png", 1),
            new DepositChannelCatalogItem("18", "携程国际", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/hudson/0842875468894826.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/hudson/5596654871754908.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/hudson/9359488859736707.png", 1),
            new DepositChannelCatalogItem("21", "木鸟", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/hudson_app_channels/muniao3.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/hudson_app_channels/muniao-blue.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/hudson_app_channels/muniao-gray.png", 1),
            new DepositChannelCatalogItem("34", "品牌小程序", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/wxma.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/hudson/8355758831771595.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/channel/wxma_close.png", 1),
            new DepositChannelCatalogItem("36", "小红书", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/localhomeqy/xhsLogo.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/localhomeqy/xhsOpen.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/localhomeqy/xhsClose.png", 1),
            new DepositChannelCatalogItem("44", "携程玩乐", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/hudson/8981066208472485.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/hudson/4384501156886349.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/hudson/5997150602615926.png", 1),
            new DepositChannelCatalogItem("58", "抖音来客", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/localhomeqy/dy3.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/localhomeqy/dy.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/localhomeqy/dy2.png", 1),
            new DepositChannelCatalogItem("71", "同程民宿", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/hudson/8346444282052181.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/hudson/8346444282052181.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com/hudson/6263442834974589.png", 0),
            new DepositChannelCatalogItem("86", "视频号", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/8323990495541343.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/8323990495541343.png", "https://locals-house-prod.oss-cn-shenzhen.aliyuncs.com//localhomeqy/8323990495541343.png", 0)
    );

    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public DepositChannelOptionsResponseVO getDepositChannels(Long campId, Long userId) {
        resolveAccessibleCampId(campId, userId);

        DepositChannelOptionsResponseVO response = new DepositChannelOptionsResponseVO();
        response.setSelect(SUPPORTED_CHANNELS.stream().map(this::toOptionVO).toList());
        return response;
    }

    private DepositChannelOptionVO toOptionVO(DepositChannelCatalogItem item) {
        DepositChannelOptionVO option = new DepositChannelOptionVO();
        option.setChannelId(item.channelId());
        option.setChannelName(item.channelName());
        option.setChannelImageLogo(item.channelImageLogo());
        option.setChannelImageOpen(item.channelImageOpen());
        option.setChannelImageClose(item.channelImageClose());
        option.setIsOpen(item.isOpen());
        return option;
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
            throw new BusinessException(40301, "无权访问当前门店押金渠道数据");
        }
        return requestedCampId;
    }

    private record DepositChannelCatalogItem(
            String channelId,
            String channelName,
            String channelImageLogo,
            String channelImageOpen,
            String channelImageClose,
            Integer isOpen
    ) {
    }
}
