package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.CampsResponseVO;
import com.jeez.zp.platform.vo.CampDetailVO;
import com.jeez.zp.platform.vo.ChannelsResponseVO;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.EditionResourceVO;
import com.jeez.zp.platform.vo.MenuOptionJsonsVO;
import com.jeez.zp.platform.vo.MenuProjectVO;
import com.jeez.zp.platform.vo.SystemConfigsResponseVO;
import com.jeez.zp.platform.vo.UserOwnVO;

import java.util.List;

public interface PlatformBootstrapService {

    CampsResponseVO getCamps();

    CurrentUserBundleVO getCurrentUserBundle(Long userId);

    UserOwnVO getOwnUser(Long userId);

    CampDetailVO getCamp(Long campId, Long userId);

    MenuProjectVO getProjectMenus(Long campId, Long projectMenuId);

    MenuOptionJsonsVO getMenuOptionJsons(List<String> menuIds);

    SystemConfigsResponseVO getSystemConfigs(Long campId, Long userId);

    ChannelsResponseVO getChannels(Long campId, Long userId);

    EditionResourceVO getEditionResource(Long campId, Long userId);
}
