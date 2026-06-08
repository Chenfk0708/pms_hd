package com.jeez.zp.finance.service;

public interface CampAccessService {
    Long resolveAccessibleCampId(Long requestedCampId, Long userId);
}
