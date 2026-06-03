package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.MemberSettingMemberVO;
import com.jeez.zp.platform.vo.MemberSettingRoleVO;
import com.jeez.zp.platform.vo.MemberSettingRoomCategoryVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MemberSettingMapper {

    List<MemberSettingRoleVO> selectRoles(@Param("campId") Long campId);

    List<MemberSettingRoomCategoryVO> selectRoomCategories(@Param("campId") Long campId);

    List<MemberSettingMemberVO> selectMembers(
            @Param("campId") Long campId,
            @Param("keyword") String keyword,
            @Param("roleName") String roleName
    );

    MemberSettingMemberVO selectMemberByUserId(@Param("campId") Long campId, @Param("userId") Long userId);
}
