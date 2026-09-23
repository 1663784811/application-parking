package com.cyyaw.admin.entity.dto.parking;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 在场车辆看板响应：一次请求返回列表 + 在场总数 + 总车位。
 * <p>
 * 该看板只读：费用为实时预估，不生成订单、不写任何表。
 */
@Data
@Schema(description = "在场车辆看板")
public class InLotBoardVO {

    @Schema(description = "总车位（pk_parking.capacity）")
    private Integer capacity;

    @Schema(description = "在场车辆总数（全场口径，不受车牌筛选与列表截断影响）")
    private Integer inLotTotal;

    @Schema(description = "命中车牌筛选的车辆数；未筛选时等于 inLotTotal")
    private Integer matchedCount;

    @Schema(description = "列表是否被截断：命中车辆超过单次返回上限时为 true")
    private Boolean truncated;

    @Schema(description = "该停车场是否配置过收费规则；false 时全部车辆都无法预估费用")
    private Boolean ruleConfigured;

    @Schema(description = "在场车辆列表（按入场时间升序，停得最久的在前）")
    private List<InLotCarVO> list;
}
