package com.cyyaw.netty.mqtt.client;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * 调用 admin 服务校验设备连接凭据。
 * <p>
 * broker 不连接数据库，连接校验时把 MQTT 的 username/password/clientId
 * 转发给 admin 的 /internal/mqtt/validate，由 admin 查 iot_device 表后
 * 返回 {allowConnect, role}。调用失败按 fail-closed 处理（拒绝连接）。
 */
@Slf4j
@Component
public class AdminDeviceClient {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private final String validateUrl;

    public AdminDeviceClient(@Value("${admin.api.base-url:http://127.0.0.1:10000/api}") String baseUrl) {
        this.validateUrl = baseUrl.replaceAll("/+$", "") + "/internal/mqtt/validate";
    }

    public ValidateResult validate(String username, String password, String clientId) {
        try {
            JSONObject body = new JSONObject();
            body.set("username", username);
            body.set("password", password);
            body.set("clientId", clientId);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(validateUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("admin 设备校验返回非 200: status={}, body={}", response.statusCode(), response.body());
                return new ValidateResult(false, null);
            }
            JSONObject json = JSONUtil.parseObj(response.body());
            JSONObject data = json.getJSONObject("data");
            if (data == null) {
                return new ValidateResult(false, null);
            }
            Boolean allow = data.getBool("allowConnect");
            String role = data.getStr("role");
            return new ValidateResult(Boolean.TRUE.equals(allow), role);
        } catch (Exception e) {
            log.warn("调用 admin 设备校验失败（拒绝连接）: {}", e.getMessage());
            return new ValidateResult(false, null);
        }
    }

    public record ValidateResult(Boolean allowConnect, String role) {
    }

}
