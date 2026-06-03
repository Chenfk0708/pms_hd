package com.jeez.zp.crm.service;

import com.jeez.zp.crm.dto.request.CampRequest;
import com.jeez.zp.crm.dto.request.ImPhrasePageRequest;
import com.jeez.zp.crm.vo.ImWordsGroupTreeResponseVO;
import com.jeez.zp.crm.vo.ImWordsPageResponseVO;
import com.jeez.zp.crm.vo.ImYunxinUserVO;

public interface ImSettingService {

    ImWordsGroupTreeResponseVO getPhraseGroupTree(CampRequest request, Long userId);

    ImWordsPageResponseVO getPhrasePage(ImPhrasePageRequest request, Long userId);

    ImYunxinUserVO getYunxinUser(CampRequest request, Long userId);
}
