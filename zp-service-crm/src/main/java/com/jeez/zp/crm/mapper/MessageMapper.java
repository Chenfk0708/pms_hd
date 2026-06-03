package com.jeez.zp.crm.mapper;

import com.jeez.zp.crm.vo.MessageItemVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MessageMapper {

    List<MessageItemVO> selectMessages(
            @Param("campId") Long campId,
            @Param("userId") Long userId,
            @Param("groupType") String groupType
    );

    int countUnread(
            @Param("campId") Long campId,
            @Param("userId") Long userId,
            @Param("groupType") String groupType
    );

    int markRead(@Param("id") Long id, @Param("messageId") Long messageId, @Param("userId") Long userId);

    List<Long> selectUnreadMessageIds(
            @Param("campId") Long campId,
            @Param("userId") Long userId,
            @Param("groupType") String groupType
    );
}
