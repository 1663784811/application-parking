package com.cyyaw.admin.entity.dto.parking;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 接口2响应：某车牌在当前停车场的待缴停车费订单。
 * 金额由服务端按 pk_cost_rules 实时计算（前端只做展示）。
 */
@Data
@Schema(description = "停车费订单")
public class ExitOrderVO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderNo;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(description = "停车场ID")
    private Long parkingId;

    @Schema(description = "停车场名称")
    private String parkingName;

    @Schema(description = "车牌号码")
    private String carNumber;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "入场时间")
    private LocalDateTime entryTime;

    @Schema(description = "停车时长文案，如「2小时30分」")
    private String duration;

    @Schema(description = "停车时长（分钟）")
    private Long durationMinutes;

    @Schema(description = "应付金额")
    private BigDecimal amount;

    @Schema(description = "支付状态{0:未支付,1:支付部分,2:已支付}")
    private Integer payStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "支付截止时间（查询时刻 + 30 分钟）")
    private LocalDateTime expireTime;

}
