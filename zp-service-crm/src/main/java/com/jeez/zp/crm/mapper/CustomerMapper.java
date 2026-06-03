package com.jeez.zp.crm.mapper;

import com.jeez.zp.crm.vo.CustomerItemVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CustomerMapper {

    List<CustomerItemVO> selectCustomers(@Param("campId") Long campId, @Param("keyword") String keyword);

    CustomerItemVO selectCustomerDetail(@Param("campId") Long campId, @Param("customerId") Long customerId);

    int upsertCustomer(
            @Param("customerId") Long customerId,
            @Param("campId") Long campId,
            @Param("name") String name,
            @Param("mobile") String mobile,
            @Param("profileJson") String profileJson
    );
}
