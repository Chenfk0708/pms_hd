package com.jeez.zp.crm.mapper;

import com.jeez.zp.crm.vo.ImWordsGroupRowVO;
import com.jeez.zp.crm.vo.ImWordsRowVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ImSettingMapper {

    List<ImWordsGroupRowVO> selectPhraseGroups(@Param("campId") Long campId);

    List<ImWordsRowVO> selectPhrases(
            @Param("campId") Long campId,
            @Param("keyword") String keyword,
            @Param("groupId") Long groupId
    );

    String selectCampConfigValue(
            @Param("campId") Long campId,
            @Param("configKey") String configKey
    );
}
