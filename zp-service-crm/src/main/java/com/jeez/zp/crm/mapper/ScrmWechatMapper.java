package com.jeez.zp.crm.mapper;

import com.jeez.zp.crm.vo.ScrmLookupOptionVO;
import com.jeez.zp.crm.vo.ScrmReplyTemplateVO;
import com.jeez.zp.crm.vo.ScrmRoomSuggestionRowVO;
import com.jeez.zp.crm.vo.ScrmWechatConversationRowVO;
import com.jeez.zp.crm.vo.WechatKfAccountVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ScrmWechatMapper {

    List<WechatKfAccountVO> selectKfAccounts(@Param("campId") Long campId);

    List<ScrmWechatConversationRowVO> selectConversationRows(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEndExclusive") LocalDateTime rangeEndExclusive
    );

    List<ScrmLookupOptionVO> selectStores(@Param("campId") Long campId);

    List<ScrmLookupOptionVO> selectChannels(@Param("campId") Long campId);

    List<ScrmReplyTemplateVO> selectReplyTemplates(@Param("campId") Long campId);

    List<ScrmRoomSuggestionRowVO> selectRoomSuggestions(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId
    );
}
