package com.jeez.gateway.controller;

import com.jeez.common.Result;
import com.jeez.gateway.dto.PmsDashboardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PMS 首页聚合控制器
 *
 * @author Jeez
 */
@Slf4j
@RestController
@RequestMapping("/api/pms")
@Tag(name = "PMS dashboard")
public class PmsDashboardController {

    private final WebClient webClient;
    private final String orderServiceUrl;
    private final String inventoryServiceUrl;
    private final String cleaningServiceUrl;
    private final String channelServiceUrl;

    public PmsDashboardController(WebClient.Builder webClientBuilder,
                                  @Value("${jeez.gateway.services.zp-order}") String orderServiceUrl,
                                  @Value("${jeez.gateway.services.zp-inventory}") String inventoryServiceUrl,
                                  @Value("${jeez.gateway.services.zp-cleaning}") String cleaningServiceUrl,
                                  @Value("${jeez.gateway.services.zp-channel}") String channelServiceUrl) {
        this.webClient = webClientBuilder.build();
        this.orderServiceUrl = orderServiceUrl;
        this.inventoryServiceUrl = inventoryServiceUrl;
        this.cleaningServiceUrl = cleaningServiceUrl;
        this.channelServiceUrl = channelServiceUrl;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "PMS dashboard aggregation")
    public Mono<Result<PmsDashboardResponse>> dashboard(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String userId = request.getHeaders().getFirst("X-User-Id");
        Mono<Map<String, Object>> order = getData(orderServiceUrl + "/api/orders/statistics", authorization, userId);
        Mono<Map<String, Object>> occupancy = getData(orderServiceUrl + "/api/orders/occupancy-statistics", authorization, userId);
        Mono<Map<String, Object>> roomBoard = getData(inventoryServiceUrl + "/api/room-board?pageNo=1&pageSize=1", authorization, userId);
        Mono<Map<String, Object>> cleaning = getData(cleaningServiceUrl + "/api/cleaning/statistics", authorization, userId);
        Mono<Map<String, Object>> channel = getData(channelServiceUrl + "/api/channel-sync-tasks?pageNo=1&pageSize=5", authorization, userId);

        return Mono.zip(order, occupancy, roomBoard, cleaning, channel)
                .map(tuple -> Result.success(buildDashboard(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4(), tuple.getT5())));
    }

    private Mono<Map<String, Object>> getData(String url, String authorization, String userId) {
        return webClient.get()
                .uri(url)
                .headers(headers -> {
                    if (authorization != null && !authorization.isBlank()) {
                        headers.set(HttpHeaders.AUTHORIZATION, authorization);
                    }
                    if (userId != null && !userId.isBlank()) {
                        headers.set("X-User-Id", userId);
                    }
                    headers.set("X-Auth-Verified", "true");
                })
                .retrieve()
                .bodyToMono(Map.class)
                .map(this::extractData)
                .onErrorResume(error -> {
                    log.warn("PMS 首页聚合下游接口失败，url：{}，原因：{}", url, error.getMessage());
                    return Mono.just(new LinkedHashMap<>());
                });
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractData(Map<?, ?> result) {
        if (result == null) {
            return new LinkedHashMap<>();
        }
        Object data = result.get("data");
        if (data instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return new LinkedHashMap<>();
    }

    private PmsDashboardResponse buildDashboard(Map<String, Object> order,
                                                Map<String, Object> occupancy,
                                                Map<String, Object> roomBoard,
                                                Map<String, Object> cleaning,
                                                Map<String, Object> channel) {
        PmsDashboardResponse response = new PmsDashboardResponse();
        Map<String, Object> orderSummary = mapValue(order, "summary");
        Map<String, Object> occupancySummary = mapValue(occupancy, "summary");
        Map<String, Object> cleaningSummary = mapValue(cleaning, "summary");

        response.setMetrics(List.of(
                PmsDashboardResponse.Metric.of("orderCount", "订单数", firstNonNull(orderSummary.get("orderCount"), orderSummary.get("totalOrders")), null),
                PmsDashboardResponse.Metric.of("revenueAmount", "营收", firstNonNull(orderSummary.get("revenue"), firstNonNull(orderSummary.get("revenueAmount"), orderSummary.get("totalAmount"))), "CNY"),
                PmsDashboardResponse.Metric.of("occupancyRate", "入住率", occupancySummary.get("occupancyRate"), "%"),
                PmsDashboardResponse.Metric.of("cleaningTaskCount", "保洁任务", firstNonNull(cleaningSummary.get("taskCount"), firstNonNull(cleaningSummary.get("totalTasks"), cleaningSummary.get("orderCount"))), null)
        ));
        response.setRoomStatusSummary(roomBoard);
        response.setCleaningSummary(cleaningSummary.isEmpty() ? cleaning : cleaningSummary);
        response.setChannelSyncSummary(channel);
        response.setPendingActions(List.of());
        return response;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mapValue(Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return new LinkedHashMap<>();
    }

    private Object firstNonNull(Object first, Object second) {
        return first != null ? first : second;
    }
}
