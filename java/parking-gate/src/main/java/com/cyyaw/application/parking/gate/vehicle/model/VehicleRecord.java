package com.cyyaw.application.parking.gate.vehicle.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 车辆通行记录。
 * <p>字段与前端 VehicleRecords.vue 展示列一一对应；当前由后端返回静态数据，
 * 后续可替换为 MQTT 实时推送或数据库查询。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRecord {

    /** 记录 id。 */
    private Integer id;

    /** 车牌号码。 */
    private String plate;

    /** 出入口名称。 */
    private String gate;

    /** 方向：in=入场，out=出场。 */
    private String type;

    /** 方向文案：入场 / 出场。 */
    private String typeText;

    /** 通行时间（HH:mm:ss）。 */
    private String time;

    /** 通行状态文字：正常 / 人工放行 / 超时未缴费 / 无牌识别 / 拦截失败 / 黑名单车辆 / 识别异常。 */
    private String status;

    /** 状态样式：normal / warning / danger（前端据此渲染颜色）。 */
    private String statusClass;
}
