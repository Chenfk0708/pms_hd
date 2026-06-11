package com.jeez.zp.crm.service.impl;

import com.jeez.common.utils.InputValidationUtils;
import com.jeez.zp.crm.dto.request.CustomerPageRequest;
import com.jeez.zp.crm.dto.request.CustomerSaveRequest;
import com.jeez.zp.crm.exception.BusinessException;
import com.jeez.zp.crm.mapper.CustomerMapper;
import com.jeez.zp.crm.service.CampAccessService;
import com.jeez.zp.crm.service.CustomerService;
import com.jeez.zp.crm.vo.CustomerItemVO;
import com.jeez.zp.crm.vo.CustomerPageResponseVO;
import com.jeez.zp.crm.vo.CustomerSaveResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final CustomerMapper customerMapper;
    private final CampAccessService campAccessService;

    @Override
    public CustomerPageResponseVO getPage(CustomerPageRequest request, Long userId) {
        Long campId = campAccessService.resolveAccessibleCampId(parseLong(request.getCampId()), userId);
        int pageNum = normalizePageNum(request.getPage(), request.getPageNum(), request.getCurrent());
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? DEFAULT_PAGE_SIZE : request.getPageSize();
        List<CustomerItemVO> rows = customerMapper.selectCustomers(campId, trimToNull(request.getKeyword()));
        int fromIndex = Math.min((pageNum - 1) * pageSize, rows.size());
        int toIndex = Math.min(fromIndex + pageSize, rows.size());

        CustomerPageResponseVO response = new CustomerPageResponseVO();
        response.setTotal((long) rows.size());
        response.setPageNum(pageNum);
        response.setPageSize(pageSize);
        response.setList(rows.subList(fromIndex, toIndex));
        return response;
    }

    @Override
    public CustomerItemVO getDetail(Long campId, Long customerId, Long userId) {
        if (customerId == null) {
            throw new BusinessException(40001, "customerId is required");
        }
        Long resolvedCampId = campAccessService.resolveAccessibleCampId(campId, userId);
        CustomerItemVO customer = customerMapper.selectCustomerDetail(resolvedCampId, customerId);
        if (customer == null) {
            throw new BusinessException(40401, "客户不存在");
        }
        return customer;
    }

    @Override
    @Transactional
    public CustomerSaveResponseVO save(CustomerSaveRequest request, Long userId) {
        Long campId = campAccessService.resolveAccessibleCampId(parseRequiredLong(request.getCampId(), "campId"), userId);
        Long customerId = parseRequiredLong(request.getCustomerId(), "customerId");
        customerMapper.upsertCustomer(
                customerId,
                campId,
                requirePersonName(request.getName()),
                requireMobile(request.getMobile()),
                defaultString(request.getProfileJson(), "{}")
        );

        CustomerSaveResponseVO response = new CustomerSaveResponseVO();
        response.setCustomerId(String.valueOf(customerId));
        response.setMessage("客户保存成功");
        return response;
    }

    private int normalizePageNum(Integer page, Integer pageNum, Integer current) {
        Integer candidate = page != null ? page : (pageNum != null ? pageNum : current);
        return candidate == null || candidate < 1 ? DEFAULT_PAGE_NUM : candidate;
    }

    private Long parseRequiredLong(String value, String fieldName) {
        Long parsed = parseLong(value);
        if (parsed == null) {
            throw new BusinessException(40001, fieldName + " is required");
        }
        return parsed;
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String requireText(String value, String fieldName) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new BusinessException(40001, fieldName + " is required");
        }
        return normalized;
    }

    private String requirePersonName(String value) {
        String normalized = requireText(value, "name");
        if (!InputValidationUtils.isValidPersonName(normalized)) {
            throw new BusinessException(40001, "姓名格式不正确，请输入 2-30 个中文或英文字母");
        }
        return normalized;
    }

    private String requireMobile(String value) {
        String normalized = requireText(value, "mobile");
        if (!InputValidationUtils.isValidMainlandMobile(normalized)) {
            throw new BusinessException(40001, "手机号格式不正确");
        }
        return normalized;
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
