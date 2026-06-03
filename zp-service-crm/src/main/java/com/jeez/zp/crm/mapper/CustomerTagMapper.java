package com.jeez.zp.crm.mapper;

import com.jeez.zp.crm.vo.CustomerTagGroupQueryRowVO;
import com.jeez.zp.crm.vo.WeComAccountVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CustomerTagMapper {

    List<CustomerTagGroupQueryRowVO> selectTagGroups(
            @Param("campId") Long campId,
            @Param("keyword") String keyword
    );

    int upsertTagGroup(
            @Param("tagGroupId") Long tagGroupId,
            @Param("campId") Long campId,
            @Param("tagGroupName") String tagGroupName,
            @Param("sourceType") String sourceType
    );

    int upsertTag(
            @Param("tagId") Long tagId,
            @Param("tagGroupId") Long tagGroupId,
            @Param("tagName") String tagName,
            @Param("sortNo") int sortNo
    );

    List<WeComAccountVO> selectWeComAccounts(@Param("campId") Long campId);
}
