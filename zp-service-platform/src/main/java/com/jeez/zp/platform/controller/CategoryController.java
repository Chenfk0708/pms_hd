package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.CategoryRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.CategoryService;
import com.jeez.zp.platform.vo.CategoriesResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/categories/get")
    public HudsonResponse<CategoriesResponseVO> getCategories(@RequestBody CategoryRequest request) {
        return HudsonResponse.success(
                categoryService.getCategories(parseLong(request.getCampId()), LoginUserContext.requiredUserId()),
                TraceIdFactory.next("categories-get")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
