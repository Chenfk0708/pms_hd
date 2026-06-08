package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.CampSummaryVO;
import com.jeez.zp.platform.vo.CampDetailVO;
import com.jeez.zp.platform.vo.ChannelVO;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.SystemConfigItemVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PlatformBootstrapMapper {

    List<CampSummaryVO> selectAvailableCamps();

    CurrentUserBundleVO selectCurrentUserBundle(@Param("userId") Long userId);

    CurrentUserBundleVO selectUserByMobileOrEmail(@Param("mobile") String mobile, @Param("email") String email);

    List<String> selectAuthorityCodesByRoleId(@Param("roleId") Long roleId);

    CampDetailVO selectCampDetail(@Param("campId") Long campId, @Param("poiId") Long poiId);

    List<SystemConfigItemVO> selectSystemConfigs(@Param("campId") Long campId, @Param("userId") Long userId);

    List<ChannelVO> selectChannelsByCampId(@Param("campId") Long campId);
}
