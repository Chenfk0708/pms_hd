package com.jeez.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PMS 首页聚合响应
 *
 * @author Jeez
 */
@Data
@Schema(description = "PMS 首页聚合响应")
public class PmsDashboardResponse {

    private List<Metric> metrics = new ArrayList<>();

    private Map<String, Object> roomStatusSummary = new LinkedHashMap<>();

    private Map<String, Object> cleaningSummary = new LinkedHashMap<>();

    private Map<String, Object> channelSyncSummary = new LinkedHashMap<>();

    private List<Map<String, Object>> pendingActions = new ArrayList<>();

    @Data
    @Schema(description = "首页指标项")
    public static class Metric {

        private String key;

        private String label;

        private Object value;

        private String unit;

        public static Metric of(String key, String label, Object value, String unit) {
            Metric metric = new Metric();
            metric.setKey(key);
            metric.setLabel(label);
            metric.setValue(value);
            metric.setUnit(unit);
            return metric;
        }
    }
}
