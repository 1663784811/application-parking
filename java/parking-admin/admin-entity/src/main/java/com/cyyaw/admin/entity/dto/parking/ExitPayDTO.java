package com.cyyaw.admin.entity.dto.parking;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 接口3请求：支付停车费。
 */
@Data
@Schema(description = "停车费支付参数")
public class ExitPayDTO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "支付方式{1:支付宝,2:微信支付}")
    private Integer payType;

}
