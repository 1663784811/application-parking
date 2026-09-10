package com.cyyaw.admin.entity.dto.iot;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 车牌识别处理结果（recognize 接口返回）。
 * <p>雪花 ID（deviceId/parkingId/carLogId）用 Long 承载，序列化时
 * @JsonFormat(shape = STRING) 转字符串，规避 JS Long 精度丢失（与 BaseEntity/PkParkingDevice 一致）。
 */
@Data
@Schema(description = "车牌识别处理结果")
public class RecognizeVo {

    @Schema(description = "识别设备编码")
    private String deviceCode;

    @Schema(description = "车牌号")
    private String carNumber;

    @Schema(description = "车辆类型")
    private String carType;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(description = "设备ID（雪花，序列化为字符串）")
    private Long deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "出入方向：in(入口)/out(出口)/inout(出入口)/unknown(无法判定)")
    private String direction;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(description = "停车场ID（雪花，序列化为字符串）")
    private Long parkingId;

    @Schema(description = "处理动作：entry(入场放行)/exit(出场放行)/reject(不放行)")
    private String action;

    @Schema(description = "处理结果说明")
    private String message;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(description = "停车记录ID（雪花，序列化为字符串；入场/出场放行时返回）")
    private Long carLogId;

}
