package com.cyyaw.application.parking.gate.vehicle;

import com.cyyaw.application.parking.gate.common.R;
import com.cyyaw.application.parking.gate.vehicle.model.PlateRecognitionRequest;
import com.cyyaw.application.parking.gate.vehicle.model.VehicleRecord;
import com.cyyaw.mqtt.client.MqttApplicationClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 车牌识别回调接口。
 * <ul>
 *   <li>POST /api/plate/recognize —— 摄像头识别到车牌后回调</li>
 * </ul>
 * <p>流程：① 下载图片到本地 → ② 上传到服务器（TODO，admin 侧暂无上传接口）→
 * ③ 发 MQTT 车牌识别消息到 broker（镜像 admin 侧 MqttBaseEntity&lt;CarInfo&gt; 结构）。</p>
 * <p>该端点放行 JWT（见 JwtAuthFilter 白名单），供无 token 的设备回调；
 * 后续应加设备级共享密钥校验。</p>
 */
@Slf4j
@Tag(name = "车牌识别回调")
@RestController
@RequestMapping("/api/plate")
@RequiredArgsConstructor
public class PlateRecognitionController {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * MQTT 主题前缀，匹配 admin 侧 ParkingMessageHandle 订阅的 /server/parking/#
     */
    private static final String MQTT_TOPIC_PREFIX = "/server/parking/";

    /**
     * admin 侧目前未定义「车牌识别」的 method 值，此处占位，待 admin 接收侧约定后对齐
     */
    private static final String MQTT_METHOD = "车牌识别";

    private static final String MQTT_VERSION = "0.0.1";

    private final VehicleRecordStore store;
    private final PlateImageService imageService;


    private final MqttApplicationClient mqttClient;


    @Operation(summary = "车牌识别回调", description = "摄像头识别到车牌后回调：① 下载图片到本地 ② 上传到服务器（TODO）③ 发 MQTT 车牌识别消息；该端点放行 JWT，供无 token 设备回调")
    @PostMapping("/recognize")
    public R<VehicleRecord> recognize(@RequestBody PlateRecognitionRequest req) {
        if (req == null || req.getCarNumber() == null || req.getCarNumber().isBlank()) {
            return R.fail(400, "车牌号不能为空");
        }

        // 第一步: 下载图片到本地
        Path imgPath = imageService.saveImage(req.getImg(), "img");
        Path numberImgPath = imageService.saveImage(req.getNumberImg(), "number");
        if (imgPath != null) {
            log.info("车辆图片已落地：{}", imgPath);
        }
        if (numberImgPath != null) {
            log.info("车牌图片已落地：{}", numberImgPath);
        }

        // 第二步：将图片上传到服务器
        // TODO: admin 侧暂无图片上传接口（已全仓确认），待其提供后，
        //       将上面落地的本地文件 multipart 上传，拿到服务器 URL 再回填到 MQTT 的 img/numberImg。

        // 第三步: 发送mqtt消息到服务器（ 车牌识别 ）
        // img/numberImg 暂透传原始值（理想应为第二步上传后的服务器 URL）
        String topic = MQTT_TOPIC_PREFIX + (req.getCode() != null && !req.getCode().isBlank() ? req.getCode() : "unknown");
        mqttClient.publish(topic, buildPlatePayload(req));
        VehicleRecord record = new VehicleRecord();
        record.setPlate(req.getCarNumber());
        record.setTime(LocalTime.now().format(TIME_FMT));
        record.setStatus("正常");
        record.setStatusClass("normal");

        store.add(record);
        return R.ok(record);
    }

    /**
     * 构造车牌识别 MQTT 报文，镜像 admin 侧 MqttBaseEntity&lt;CarInfo&gt; 结构
     * （不同模块不可直接引用，故本地手写 JSON；SB4 下注入 jackson2 ObjectMapper 会启动崩，
     * 故不依赖序列化器）。字段：code / method / version / params{code,carNumber,carType,img,numberImg}。
     */
    private String buildPlatePayload(PlateRecognitionRequest req) {
        return "{" + "\"code\":" + q(req.getCode()) + "," + "\"method\":" + q(MQTT_METHOD) + "," + "\"version\":" + q(MQTT_VERSION) + "," + "\"params\":{" + "\"code\":" + q(req.getCode()) + "," + "\"carNumber\":" + q(req.getCarNumber()) + "," + "\"carType\":" + q(req.getCarType()) + "," + "\"img\":" + q(req.getImg()) + "," + "\"numberImg\":" + q(req.getNumberImg()) + "}" + "}";
    }

    /**
     * JSON 字符串字面量：null → null 字面量；非空 → 转义并加引号。
     */
    private static String q(String s) {
        return s == null ? "null" : "\"" + esc(s) + "\"";
    }

    private static String esc(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}
