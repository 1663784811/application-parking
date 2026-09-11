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
import java.nio.charset.StandardCharsets;
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

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();

    private final String validateUrl;
    private final String statusUrl;

    public AdminDeviceClient(@Value("${admin.api.base-url:http://127.0.0.1:10000/api}") String baseUrl) {
        String base = baseUrl.replaceAll("/+$", "");
        this.validateUrl = base + "/internal/mqtt/validate";
        this.statusUrl = base + "/internal/mqtt/status";
    }

    public ValidateResult validate(String username, String password, String clientId) {
        try {
            JSONObject body = new JSONObject();
            body.set("username", username);
            body.set("password", password);
            body.set("clientId", clientId);

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(validateUrl)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body.toString())).timeout(Duration.ofSeconds(5)).build();

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

    /**
     * 转发设备上线/下线报文到 admin 的 /internal/mqtt/status。
     * <p>
     * 报文为物模型 thing.event.online.post 信封，admin 解析 params.{deviceCode, online}
     * 后更新 iot_device.online_status。deviceCode 取自 MQTT topic（路由真值），仅用于日志。
     * 转发用 sendAsync 异步执行，不阻塞 netty I/O 线程；失败仅记日志、不抛异常。
     */
    public void updateOnlineStatus(String deviceCode, byte[] payload) {
        forwardOnline(deviceCode, new String(payload, StandardCharsets.UTF_8));
    }

    /**
     * gate 直连云 broker 的上线/下线（无 MQTT 报文）：按 thing.event.online.post
     * 报文格式构造 params.{deviceCode, online} 后转发给 admin。
     */
    public void updateOnlineStatus(String deviceCode, boolean online) {
        JSONObject params = new JSONObject();
        params.set("deviceCode", deviceCode);
        params.set("online", online);
        JSONObject body = new JSONObject();
        body.set("params", params);
        forwardOnline(deviceCode, body.toString());
    }

    private void forwardOnline(String deviceCode, String body) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(statusUrl)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8)).timeout(Duration.ofSeconds(5)).build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(r -> {
            if (r.statusCode() != 200) {
                log.warn("admin 更新设备在线状态返回非 200: deviceCode={}, status={}, body={}", deviceCode, r.statusCode(), r.body());
            } else {
                log.info("admin 更新设备在线状态完成: deviceCode={}, resp={}", deviceCode, r.body());
            }
        }).exceptionally(e -> {
            log.warn("转发设备在线状态报文到 admin 失败: deviceCode={}, {}", deviceCode, e.getMessage());
            return null;
        });
    }

    public record ValidateResult(Boolean allowConnect, String role) {
    }

}
