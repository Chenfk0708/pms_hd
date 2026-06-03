package com.jeez.zp.crm.service;

import com.jeez.zp.crm.dto.request.CustomerPageRequest;
import com.jeez.zp.crm.dto.request.CustomerSaveRequest;
import com.jeez.zp.crm.vo.CustomerItemVO;
import com.jeez.zp.crm.vo.CustomerPageResponseVO;
import com.jeez.zp.crm.vo.CustomerSaveResponseVO;

public interface CustomerService {

    CustomerPageResponseVO getPage(CustomerPageRequest request, Long userId);

    CustomerItemVO getDetail(Long campId, Long customerId, Long userId);

    CustomerSaveResponseVO save(CustomerSaveRequest request, Long userId);
}
