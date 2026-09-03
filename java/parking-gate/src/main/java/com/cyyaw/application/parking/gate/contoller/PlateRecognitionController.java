package com.cyyaw.application.parking.gate.contoller;

import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.application.parking.gate.common.GateConstants;
import com.cyyaw.application.parking.gate.common.entity.PlateRecognitionPayload;
import com.cyyaw.application.parking.gate.common.entity.PlateRecognitionRequest;
import com.cyyaw.application.parking.gate.common.entity.VehicleRecord;
import com.cyyaw.application.parking.gate.vehicle.VehicleRecordStore;
import com.cyyaw.mqtt.client.MqttApplicationClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.core.JacksonException;

import java.time.LocalTime;

/**
 * 车牌识别回调接口。
 * <ul>
 *   <li>POST /api/plate/recognize —— 摄像头识别到车牌后回调</li>
 * </ul>
 * <p>流程：① 下载图片到本地 → ② 上传到服务器（TODO，admin 侧暂无上传接口）→
 * ③ 发 MQTT 车牌识别事件到 broker（报文对齐 MQTT停车场与云平台通信.md「上报 车牌识别」：
 * 主题 /server/parking/${设备编码}/thing/event/property/post，物模型事件 thing.event.property.post）。</p>
 * <p>该端点放行 JWT（见 JwtAuthFilter 白名单），供无 token 的设备回调；
 * 后续应加设备级共享密钥校验。</p>
 */
@Slf4j
@Tag(name = "车牌识别回调")
@RestController
@RequestMapping("/api/plate")
@RequiredArgsConstructor
public class PlateRecognitionController {

    private final VehicleRecordStore store;
    private final MqttApplicationClient mqttClient;


    @Operation(summary = "车牌识别回调", description = "摄像头识别到车牌后回调：① 下载图片到本地 ② 上传到服务器（TODO）③ 发 MQTT 车牌识别消息；该端点放行 JWT，供无 token 设备回调")
    @PostMapping("/recognize")
    public BaseResult<VehicleRecord> recognize(@RequestBody PlateRecognitionRequest req) {
        if (req == null || req.getCarNumber() == null || req.getCarNumber().isBlank()) {
            return BaseResult.fail("车牌号不能为空");
        }
        //将图片上传到服务器


        //
        String deviceCode = req.getDeviceCode() != null && !req.getDeviceCode().isBlank() ? req.getDeviceCode() : "unknown";
        String topic = GateConstants.MQTT_TOPIC_PREFIX + deviceCode + GateConstants.MQTT_TOPIC_SUFFIX;
        String payload = buildPlatePayload(deviceCode, req);
        if (payload == null) {
            log.warn("车牌识别 MQTT 报文序列化失败，跳过发布：deviceCode={}", deviceCode);
        } else {
            mqttClient.publish(topic, payload);
        }
        VehicleRecord record = new VehicleRecord();
        record.setPlate(req.getCarNumber());
        record.setTime(LocalTime.now().format(GateConstants.TIME_FMT));
        record.setStatus("正常");
        record.setStatusClass("normal");

        store.add(record);
        return BaseResult.ok(record);
    }

    /**
     * 构造车牌识别 MQTT 报文并序列化为 JSON 字符串，对齐 MQTT停车场与云平台通信.md
     * 「上报 车牌识别」。结构见 {@link PlateRecognitionPayload}：
     * <pre>{@code
     * {
     *   "id": "10000",
     *   "version": "1.0",
     *   "sys": { "ack": 0, "deviceCode": "<边设备Code>", "childCode": null },
     *   "method": "thing.event.property.post",
     *   "params": { "deviceCode": "...", "carNumber": "...", "carType": "..." }
     * }
     * }</pre>
     * 用 Jackson 3（SB4 默认）的 ObjectMapper 序列化；手动 new 而非注入，避免误注入
     * jackson2 ObjectMapper 导致启动崩（见 memory spring-boot-4-jackson-3）。序列化失败
     * 记日志并返回 null，由调用方跳过发布。
     * <p>边/子设备映射：本回调仅携带一个 deviceCode（识别相机），按单设备部署处理——
     * 同时充当 sys.deviceCode（边设备）与 params.deviceCode；childCode 暂置 null。
     * 若后续为「网关(边设备) 下挂多相机(子设备)」拓扑，应在 gate 配置增加边设备编码，
     * sys.deviceCode 取边设备、sys.childCode/params.deviceCode 取相机。
     */
    private String buildPlatePayload(String deviceCode, PlateRecognitionRequest req) {
        PlateRecognitionPayload payload = new PlateRecognitionPayload();
        payload.setId(String.valueOf(GateConstants.MSG_ID.getAndIncrement()));

        PlateRecognitionPayload.Sys sys = new PlateRecognitionPayload.Sys();
        sys.setDeviceCode(deviceCode);
        // childCode 暂置 null（单设备部署，无独立子设备层）
        payload.setSys(sys);

        PlateRecognitionPayload.Params params = new PlateRecognitionPayload.Params();
        params.setDeviceCode(deviceCode);
        params.setCarNumber(req.getCarNumber());
        params.setCarType(req.getCarType());
        payload.setParams(params);

        try {
            return GateConstants.MAPPER.writeValueAsString(payload);
        } catch (JacksonException e) {
            log.error("车牌识别 MQTT 报文序列化失败：{}", payload, e);
            return null;
        }
    }
}
