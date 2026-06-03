package com.jeez.zp.crm.service;

import com.jeez.zp.crm.dto.request.CustomerTagPageRequest;
import com.jeez.zp.crm.dto.request.CustomerTagSaveRequest;
import com.jeez.zp.crm.vo.CustomerTagExportResponseVO;
import com.jeez.zp.crm.vo.CustomerTagPageResponseVO;
import com.jeez.zp.crm.vo.CustomerTagSaveResponseVO;
import com.jeez.zp.crm.vo.WeComAccountsResponseVO;

public interface CustomerTagService {

    CustomerTagPageResponseVO getPage(CustomerTagPageRequest request, Long userId);

    CustomerTagSaveResponseVO save(CustomerTagSaveRequest request, Long userId);

    CustomerTagExportResponseVO export(CustomerTagPageRequest request, Long userId);

    WeComAccountsResponseVO getWeComAccounts(Long campId, Long userId);
}
