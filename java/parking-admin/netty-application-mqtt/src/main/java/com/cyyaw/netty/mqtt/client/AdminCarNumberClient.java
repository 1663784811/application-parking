package com.cyyaw.netty.mqtt.client;

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
 * 转发车牌识别报文到 admin 的 CarNumberController。
 * <p>
 * broker 不连库，识别处理（查设备/判方向/落库/开闸）由 admin 完成。broker 收到
 * /server/parking/{deviceCode}/thing/event/recognize/post 后，把 topic 里的
 * deviceCode 注入报文 params，POST 到 admin 的 /internal/parking/carNumber/recognize
 * （/internal/** 在 admin SecurityConfig 放行，仅供内部服务调用）。
 * 转发用 sendAsync 异步执行，不阻塞 netty I/O 线程；失败仅记日志、不抛异常。
 */
@Slf4j
@Component
public class AdminCarNumberClient {

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();

    private final String recognizeUrl;

    public AdminCarNumberClient(@Value("${admin.api.base-url:http://127.0.0.1:10000/api}") String baseUrl) {
        this.recognizeUrl = baseUrl.replaceAll("/+$", "") + "/internal/parking/carNumber/recognize";
    }

    /**
     * 把识别报文转发给 admin。deviceCode 取自 MQTT topic（路由真值），
     * 注入 params 后整体 POST；报文内缺失 params 时补建。
     */
    public void forwardRecognize(String deviceCode, byte[] payload) {
        String body = new String(payload, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(recognizeUrl)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8)).timeout(Duration.ofSeconds(5)).build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(r -> {
            if (r.statusCode() != 200) {
                log.warn("admin 车牌识别转发返回非 200: deviceCode={}, status={}, body={}", deviceCode, r.statusCode(), r.body());
            } else {
                log.info("admin 车牌识别处理完成: deviceCode={}, resp={}", deviceCode, r.body());
            }
        }).exceptionally(e -> {
            log.warn("转发车牌识别报文到 admin 失败: deviceCode={}, {}", deviceCode, e.getMessage());
            return null;
        });
    }
}
