package com.jeez.zp.crm.mapper;

import com.jeez.zp.crm.vo.AuthorityItemVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AuthorityMapper {

    List<AuthorityItemVO> selectNotificationAuthorities(@Param("campId") Long campId, @Param("userId") Long userId);

    int upsertExclude(@Param("id") Long id, @Param("campId") Long campId, @Param("userId") Long userId, @Param("authorityId") Long authorityId);

    int restoreExclude(@Param("campId") Long campId, @Param("userId") Long userId, @Param("authorityId") Long authorityId);
}
