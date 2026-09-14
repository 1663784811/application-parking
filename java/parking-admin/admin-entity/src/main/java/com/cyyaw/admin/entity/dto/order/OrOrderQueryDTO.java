package com.cyyaw.admin.entity.dto.order;

import com.cyyaw.admin.entity.dto.iot.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "订单列表查询参数")
public class OrOrderQueryDTO extends PageDTO {

    @Schema(description = "订单号模糊关键词")
    private String orderNo;

    @Schema(description = "订单状态")
    private Integer orderStatus;

    @Schema(description = "支付状态")
    private Integer payStatus;

    @Schema(description = "创建起始日期(yyyy-MM-dd)")
    private String startDate;

    @Schema(description = "创建结束日期(yyyy-MM-dd)")
    private String endDate;
}
