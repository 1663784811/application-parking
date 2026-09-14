package com.cyyaw.admin.entity.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "按订单号查询参数")
public class OrOrderFindByOrderNoDTO {

    @Schema(description = "订单编号")
    private String orderNo;
}
