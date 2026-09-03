package com.cyyaw.application.parking.gate.common.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 车辆通行记录。
 * <p>字段与前端 VehicleRecords.vue 展示列一一对应；由
 * {@link com.cyyaw.application.parking.gate.vehicle.VehicleRecordStore} 持有，
 * 车牌识别回调实时写入、/api/vehicle/records 读出。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRecord {

    @Schema(description = "记录id", example = "11")
    private Integer id;

    @Schema(description = "车牌号码", example = "粤B·8K321")
    private String plate;

    @Schema(description = "出入口名称", example = "#G01 主入口")
    private String gate;

    @Schema(description = "方向：in=入场，out=出场", example = "in")
    private String type;

    @Schema(description = "方向文案：入场 / 出场", example = "入场")
    private String typeText;

    @Schema(description = "通行时间（HH:mm:ss）", example = "14:32:05")
    private String time;

    @Schema(description = "通行状态文字：正常 / 人工放行 / 超时未缴费 / 无牌识别 / 拦截失败 / 黑名单车辆 / 识别异常", example = "正常")
    private String status;

    @Schema(description = "状态样式：normal / warning / danger（前端据此渲染颜色）", example = "normal")
    private String statusClass;
}
