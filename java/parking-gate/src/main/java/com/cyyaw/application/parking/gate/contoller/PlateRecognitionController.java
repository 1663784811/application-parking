package com.cyyaw.application.parking.gate.contoller;

import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.application.parking.gate.common.GateConstants;
import com.cyyaw.application.parking.gate.common.JsonUtil;
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
        log.info("车牌识别回调: {}  {} {}", req.getDeviceCode(), req.getCarNumber(), req.getCarType());
        if (req == null || req.getCarNumber() == null || req.getCarNumber().isBlank()) {
            return BaseResult.fail("车牌号不能为空");
        }
        //将图片上传到服务器
        String childCode = req.getDeviceCode() != null && !req.getDeviceCode().isBlank() ? req.getDeviceCode() : "";
        String deviceCode = mqttClient.getBrokerObject().getClientId();
        String topic = GateConstants.MQTT_TOPIC_PREFIX + deviceCode + GateConstants.MQTT_TOPIC_RECOGNIZE;
        String payload = buildPlatePayload(deviceCode, childCode, req);
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
     */
    private String buildPlatePayload(String deviceCode, String childCode, PlateRecognitionRequest req) {
        PlateRecognitionPayload payload = new PlateRecognitionPayload();
        payload.setId(String.valueOf(GateConstants.MSG_ID.getAndIncrement()));
        PlateRecognitionPayload.Sys sys = new PlateRecognitionPayload.Sys();
        sys.setDeviceCode(deviceCode);
        sys.setChildCode(childCode);
        // childCode 暂置 null（单设备部署，无独立子设备层）
        payload.setSys(sys);
        PlateRecognitionPayload.Params params = new PlateRecognitionPayload.Params();
        params.setDeviceCode(deviceCode);
        params.setCarNumber(req.getCarNumber());
        params.setCarType(req.getCarType());
        payload.setParams(params);
        return JsonUtil.toJson(payload);
    }
}
