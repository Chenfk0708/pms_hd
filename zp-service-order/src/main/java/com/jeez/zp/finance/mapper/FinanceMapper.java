package com.jeez.zp.finance.mapper;

import com.jeez.zp.finance.vo.*;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface FinanceMapper {
    List<CustomChannelRowVO> selectCustomChannels(@Param("campId") Long campId);
    Long selectMaxCustomChannelId();
    int insertCustomChannel(@Param("customChannelId") Long customChannelId,
                            @Param("campId") Long campId,
                            @Param("channelName") String channelName,
                            @Param("channelCode") String channelCode,
                            @Param("colorHex") String colorHex,
                            @Param("colorName") String colorName,
                            @Param("status") Integer status,
                            @Param("operatorName") String operatorName,
                            @Param("note") String note);
    int updateCustomChannel(@Param("campId") Long campId,
                            @Param("customChannelId") Long customChannelId,
                            @Param("channelName") String channelName,
                            @Param("colorHex") String colorHex,
                            @Param("colorName") String colorName,
                            @Param("status") Integer status);
    int softDeleteCustomChannel(@Param("campId") Long campId, @Param("customChannelId") Long customChannelId);
    Long countCustomChannelName(@Param("campId") Long campId, @Param("channelName") String channelName, @Param("excludedId") Long excludedId);
    Long countCustomChannelCode(@Param("campId") Long campId, @Param("channelCode") String channelCode, @Param("excludedId") Long excludedId);
    CustomChannelRowVO selectCustomChannel(@Param("campId") Long campId, @Param("customChannelId") Long customChannelId);
    String selectSystemChannelStatesConfig(@Param("campId") Long campId, @Param("configKey") String configKey);
    int upsertSystemChannelStatesConfig(@Param("campId") Long campId,
                                        @Param("configKey") String configKey,
                                        @Param("configValue") String configValue,
                                        @Param("updatedBy") Long updatedBy,
                                        @Param("systemConfigId") Long systemConfigId);
    Long selectSystemConfigId(@Param("campId") Long campId, @Param("configKey") String configKey);
    List<PaymentWayVO> selectPaymentWays(@Param("campId") Long campId);
    List<PaymentTypeVO> selectPaymentTypes(@Param("campId") Long campId);
    List<PaymentGroupVO> selectPaymentGroups(@Param("campId") Long campId);
    PaymentGroupVO selectPaymentGroupByType(@Param("campId") Long campId, @Param("groupType") Integer groupType);
    Long selectMaxPaymentTypeId();
    int insertPaymentType(@Param("paymentTypeId") Long paymentTypeId, @Param("campId") Long campId, @Param("groupId") Long groupId, @Param("paymentTypeName") String paymentTypeName, @Param("groupType") Integer groupType, @Param("groupName") String groupName, @Param("bizType") Integer bizType, @Param("isIncome") Integer isIncome, @Param("ignoreOrderGetItem") Integer ignoreOrderGetItem, @Param("userId") Long userId);
    List<PaymentSettingVO> selectPaymentSettings(@Param("campId") Long campId);
    PaymentSettingVO selectPaymentSetting(@Param("campId") Long campId);
    PaymentWayVO selectPaymentWay(@Param("campId") Long campId, @Param("paymentWayId") Long paymentWayId);
    Long selectMaxPaymentWayId();
    Integer selectMaxPaymentWaySortNo(@Param("campId") Long campId);
    int insertPaymentWay(@Param("paymentWayId") Long paymentWayId, @Param("campId") Long campId, @Param("name") String name, @Param("code") String code, @Param("status") Integer status, @Param("sortNo") Integer sortNo);
    int updatePaymentWayStatus(@Param("campId") Long campId, @Param("paymentWayId") Long paymentWayId, @Param("status") Integer status);
    PaymentWayVO selectAdjacentPaymentWay(@Param("campId") Long campId, @Param("sortNo") Integer sortNo, @Param("direction") String direction);
    int updatePaymentWaySortNo(@Param("campId") Long campId, @Param("paymentWayId") Long paymentWayId, @Param("sortNo") Integer sortNo);
    List<OrderLedgerRecordVO> selectLedgerRecords(@Param("campId") Long campId, @Param("beginTime") LocalDateTime beginTime, @Param("endTime") LocalDateTime endTime, @Param("isIncome") Integer isIncome, @Param("roomCategoryId") Long roomCategoryId);
    Long countLedgerRecords(@Param("campId") Long campId, @Param("beginTime") LocalDateTime beginTime, @Param("endTime") LocalDateTime endTime, @Param("isIncome") Integer isIncome, @Param("roomCategoryId") Long roomCategoryId);
    OrderLedgerSummaryVO selectLedgerSummary(@Param("campId") Long campId, @Param("beginTime") LocalDateTime beginTime, @Param("endTime") LocalDateTime endTime, @Param("isIncome") Integer isIncome, @Param("roomCategoryId") Long roomCategoryId);
    List<LedgerEntryRowVO> selectLedgerEntryRows(@Param("campId") Long campId, @Param("beginTime") LocalDateTime beginTime, @Param("endTime") LocalDateTime endTime, @Param("isIncome") Integer isIncome, @Param("roomCategoryId") Long roomCategoryId, @Param("offset") long offset, @Param("pageSize") int pageSize);
    List<OtaAccountVO> selectOtaAccounts(@Param("campId") Long campId);
    Integer countOtaPois(@Param("campId") Long campId);
    Integer countOtaRoomCategories(@Param("campId") Long campId);
    OtaAccountVO selectOtaAccount(@Param("campId") Long campId, @Param("accountId") Long accountId);
    List<OtaPoiRelVO> selectOtaPois(@Param("campId") Long campId, @Param("accountId") Long accountId);
    List<OtaRoomCategoryRelVO> selectOtaRoomCategories(@Param("campId") Long campId, @Param("accountId") Long accountId);
    List<DistributionOrderVO> selectDistributionOrders(@Param("campId") Long campId, @Param("bookingStart") LocalDateTime bookingStart, @Param("bookingEndExclusive") LocalDateTime bookingEndExclusive, @Param("keyword") String keyword, @Param("settledState") String settledState, @Param("breakTemp") Boolean breakTemp, @Param("offset") long offset, @Param("pageSize") int pageSize);
    Long countDistributionOrders(@Param("campId") Long campId, @Param("bookingStart") LocalDateTime bookingStart, @Param("bookingEndExclusive") LocalDateTime bookingEndExclusive, @Param("keyword") String keyword, @Param("settledState") String settledState, @Param("breakTemp") Boolean breakTemp);
    String selectCampName(@Param("campId") Long campId);
    List<AccountBookPaymentWayQueryRowVO> selectAccountBookPaymentWayRows(@Param("campId") Long campId, @Param("beginDate") LocalDate beginDate, @Param("endDate") LocalDate endDate, @Param("poiIds") List<Long> poiIds);
    long countStatementOrders(@Param("campId") Long campId, @Param("poiIds") List<Long> poiIds, @Param("bookingStart") LocalDateTime bookingStart, @Param("bookingEndExclusive") LocalDateTime bookingEndExclusive, @Param("breakTemp") Boolean breakTemp);
    List<StatementOrderQueryRowVO> selectStatementOrders(@Param("campId") Long campId, @Param("poiIds") List<Long> poiIds, @Param("bookingStart") LocalDateTime bookingStart, @Param("bookingEndExclusive") LocalDateTime bookingEndExclusive, @Param("breakTemp") Boolean breakTemp, @Param("offset") long offset, @Param("pageSize") int pageSize);
    List<SocialAccountRowVO> selectSocialAccounts(@Param("campId") Long campId, @Param("keyword") String keyword);
    Integer countChannelRoomCategories(@Param("campId") Long campId, @Param("channelId") Long channelId);
    Integer countLinkedChannelRoomCategories(@Param("campId") Long campId, @Param("channelId") Long channelId);
    Integer countOrdersByChannelAndBizDate(@Param("campId") Long campId, @Param("channelId") Long channelId, @Param("bizDate") String bizDate);
    List<FullMarketingCommissionRowVO> selectFullMarketingCommissionRows(@Param("campId") Long campId, @Param("type") Integer type, @Param("keyword") String keyword);
    FullMarketingMetricVO selectFullMarketingMetric(@Param("campId") Long campId, @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("type") Integer type);
    List<FullMarketingProductSaleRowVO> selectFullMarketingProductSaleRows(@Param("campId") Long campId, @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("type") Integer type);
    List<ShiftWorkConfigRowVO> selectShiftWorkConfigs(@Param("campId") Long campId);
    List<ShiftWorkGoodsVO> selectShiftWorkGoods(@Param("campId") Long campId);
    Long selectMaxShiftWorkConfigId();
    Long selectMaxShiftWorkGoodsId();
    int disableMissingShiftWorkConfigs(@Param("campId") Long campId, @Param("ids") List<Long> ids);
    int disableMissingShiftWorkGoods(@Param("campId") Long campId, @Param("ids") List<Long> ids);
    int upsertShiftWorkConfig(@Param("shiftConfigId") Long shiftConfigId, @Param("campId") Long campId, @Param("name") String name, @Param("startTime") String startTime, @Param("endTime") String endTime, @Param("memberIdsJson") String memberIdsJson);
    int upsertShiftWorkGoods(@Param("shiftGoodsId") Long shiftGoodsId, @Param("campId") Long campId, @Param("name") String name, @Param("sortNo") Integer sortNo);
    Long countActiveShiftWorkConfigByName(@Param("campId") Long campId, @Param("name") String name, @Param("excludedId") Long excludedId);
    Long countActiveShiftWorkGoodsByName(@Param("campId") Long campId, @Param("name") String name, @Param("excludedId") Long excludedId);
    Long countActiveCampMemberByUserId(@Param("campId") Long campId, @Param("userId") Long userId);
    List<ShiftWorkReportRowVO> selectShiftWorkReports(@Param("campId") Long campId, @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("handoverUserId") Long handoverUserId, @Param("receiverUserId") Long receiverUserId);
}
