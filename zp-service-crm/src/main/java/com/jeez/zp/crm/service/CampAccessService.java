package com.jeez.zp.crm.service;

public interface CampAccessService {

    Long resolveAccessibleCampId(Long requestedCampId, Long userId);
}
