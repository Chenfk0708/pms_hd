package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.CampSummaryVO;
import com.jeez.zp.platform.vo.CampDetailVO;
import com.jeez.zp.platform.vo.ChannelVO;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.RoleSummaryVO;
import com.jeez.zp.platform.vo.SystemConfigItemVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PlatformBootstrapMapper {

    List<CampSummaryVO> selectAvailableCamps();

    CurrentUserBundleVO selectCurrentUserBundle(@Param("userId") Long userId);

    CurrentUserBundleVO selectUserByAccount(@Param("username") String username,
                                            @Param("mobile") String mobile,
                                            @Param("email") String email);

    int countUsersByAccount(@Param("username") String username,
                            @Param("mobile") String mobile,
                            @Param("email") String email);

    int countUsersByUsername(@Param("username") String username);

    int countUsersByMobile(@Param("mobile") String mobile);

    int countUsersByEmail(@Param("email") String email);

    RoleSummaryVO selectActiveRole(@Param("campId") Long campId, @Param("roleId") Long roleId);

    CampSummaryVO selectCampSummary(@Param("campId") Long campId);

    List<RoleSummaryVO> selectPublicRegisterRoles(@Param("campId") Long campId);

    int updateAccountProfile(@Param("userId") Long userId,
                             @Param("nickName") String nickName,
                             @Param("email") String email,
                             @Param("wechat") String wechat,
                             @Param("avatarUrl") String avatarUrl,
                             @Param("passwordHash") String passwordHash);

    int updateLinkedMemberProfile(@Param("userId") Long userId,
                                  @Param("name") String name,
                                  @Param("email") String email);

    List<String> selectAuthorityCodesByRoleId(@Param("roleId") Long roleId);

    CampDetailVO selectCampDetail(@Param("campId") Long campId, @Param("poiId") Long poiId);

    List<SystemConfigItemVO> selectSystemConfigs(@Param("campId") Long campId, @Param("userId") Long userId);

    List<ChannelVO> selectChannelsByCampId(@Param("campId") Long campId);
}
