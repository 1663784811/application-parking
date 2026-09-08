package com.cyyaw.application.parking.gate.client;

import com.cyyaw.application.parking.gate.common.JsonUtil;
import com.cyyaw.application.parking.gate.common.entity.VehicleRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 调用 admin 服务的车辆通行记录接口。
 * <p>
 * 门口端 parking-gate 不直接持有停车记录数据，改为调用 admin 的
 * /internal/parking/carLog/list（/internal/** 在 admin SecurityConfig 放行，
 * 仅供内部服务调用）获取最近通行记录。调用失败按降级处理：返回空列表，
 * 仅记日志，使门口端首页「车辆通行记录」面板不至于因 admin 不可达而报错。
 */
@Slf4j
@Component
public class AdminCarLogClient {

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();

    private final String listUrl;

    public AdminCarLogClient(@Value("${admin.api.base-url:http://127.0.0.1:10000/api}") String baseUrl) {
        String base = baseUrl.replaceAll("/+$", "");
        this.listUrl = base + "/internal/parking/carLog/list";
    }

    /**
     * 拉取最近通行记录并映射为门口端展示用 {@link VehicleRecord}。
     *
     * @param page      页码（从 1 开始）
     * @param size      每页条数
     * @param parkingId 停车场 id，null 表示不限
     * @return 映射后的记录列表（最新在前）；admin 不可达或异常时返回空列表
     */
    public List<VehicleRecord> listRecords(Integer page, Integer size, Long parkingId) {
        int p = page == null ? 1 : page;
        int s = size == null ? 10 : size;
        StringBuilder qs = new StringBuilder("?page=").append(p).append("&size=").append(s);
        if (parkingId != null) {
            qs.append("&parkingId=").append(parkingId);
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(listUrl + qs))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("admin 通行记录接口返回非 200: status={}, body={}", response.statusCode(), response.body());
                return List.of();
            }
            return parseRecords(response.body());
        } catch (Exception e) {
            log.warn("调用 admin 通行记录接口失败: {}", e.getMessage());
            return List.of();
        }
    }

    private List<VehicleRecord> parseRecords(String body) {
        List<VehicleRecord> result = new ArrayList<>();
        try {
            JsonNode root = JsonUtil.readTree(body);
            JsonNode data = root.path("data");
            if (!data.isArray()) {
                return result;
            }
            int id = 0;
            for (JsonNode node : data) {
                id++;
                String carNumber = node.path("carNumber").asText("");
                String entryTime = node.path("entryTime").asText("");
                String outTime = node.path("outTime").asText("");
                boolean isOut = !outTime.isBlank();
                String time = parseTime(isOut ? outTime : entryTime);
                String type = isOut ? "out" : "in";
                String typeText = isOut ? "出场" : "入场";
                // pk_car_log.status: 0=在场, 1=已出场；通行记录视角均为正常通行
                result.add(new VehicleRecord(
                        id,
                        carNumber,
                        "",        // 出入口名称，admin 未记录设备/闸机，留空
                        type,
                        typeText,
                        time,
                        "正常",
                        "normal"
                ));
            }
        } catch (Exception e) {
            log.warn("解析 admin 通行记录响应失败: {}", e.getMessage());
        }
        return result;
    }

    /** "yyyy-MM-dd HH:mm:ss" → "HH:mm:ss" */
    private String parseTime(String datetime) {
        if (datetime == null || datetime.isBlank()) {
            return "";
        }
        int idx = datetime.indexOf(' ');
        return idx >= 0 && idx + 1 < datetime.length() ? datetime.substring(idx + 1) : datetime;
    }

}
