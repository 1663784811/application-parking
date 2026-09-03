package com.cyyaw.parking.camera.entity;

import com.cyyaw.parking.camera.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 车牌识别回调请求体（Android 端）。
 * <p>字段与 JSON 键名逐字对齐 parking-gate 的
 * {@code com.cyyaw.application.parking.gate.common.entity.PlateRecognitionRequest}，
 * 供 {@link MainActivity} 组装后经 HTTP 回调 POST 到 {@code /api/plate/recognize}。
 * 序列化用 Android 内置 {@link JSONObject}，无需引入额外依赖。</p>
 */
public class PlateRecognitionRequest {

    private final String deviceCode;   // 硬件编号（摄像头/设备）
    private final String carNumber;    // 车牌号
    private final String carType;       // 车辆类型
    private final String img;           // 原图 JPEG base64
    private final String numberImg;     // 车牌图 JPEG base64

    public PlateRecognitionRequest(String deviceCode, String carNumber, String carType,
                                   String img, String numberImg) {
        this.deviceCode = deviceCode;
        this.carNumber = carNumber;
        this.carType = carType;
        this.img = img;
        this.numberImg = numberImg;
    }

    /**
     * 序列化为 HTTP 请求体 JSON，键名对齐 gate 端 DTO：
     * {@code deviceCode/carNumber/carType/img/numberImg}。
     *
     * @throws JSONException {@link JSONObject#put} 的受检异常（仅放 String 值，正常不会抛）
     */
    public String toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("deviceCode", deviceCode);
        json.put("carNumber", carNumber);
        json.put("carType", carType);
        json.put("img", img);
        json.put("numberImg", numberImg);
        return json.toString();
    }
}
