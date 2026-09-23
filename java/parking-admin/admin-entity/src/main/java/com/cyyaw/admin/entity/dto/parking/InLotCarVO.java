package com.cyyaw.admin.entity.dto.parking;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 在场车辆看板的单车信息。
 * <p>
 * 金额是服务端按 pk_cost_rules 实时算出来的**预估值**（入场时刻 → 请求时刻），
 * 不是 pk_car_log 的字段，也不代表已生成的订单金额。
 */
@Data
@Schema(description = "在场车辆看板-单车")
public class InLotCarVO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(description = "停车记录ID")
    private Long id;

    @Schema(description = "车牌号码")
    private String carNumber;

    @Schema(description = "车辆类型{0:小型汽车,1:中型汽车,2:大型汽车}")
    private String carType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "入场时间")
    private LocalDateTime entryTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "出场识别时间（待缴费车辆才有）")
    private LocalDateTime outRecognizeTime;

    @Schema(description = "已停车时长文案，如「2小时30分」")
    private String duration;

    @Schema(description = "已停车时长（分钟）")
    private Long durationMinutes;

    @Schema(description = "预计费用（元）；无适用费率时无意义，见 ruleMatched")
    private BigDecimal amount;

    @Schema(description = "是否待缴费：出场摄像头已识别、缴费放行前 status 仍为 0")
    private Boolean waiting;

    @Schema(description = "是否有适用费率：false 表示该车无匹配规则，预估不可用（不是 0 元）")
    private Boolean ruleMatched;
}
