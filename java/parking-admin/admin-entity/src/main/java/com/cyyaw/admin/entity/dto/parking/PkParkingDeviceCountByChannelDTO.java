package com.cyyaw.admin.entity.dto.parking;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;


@Data
@Schema(description = "按通道统计设备数查询参数")
public class PkParkingDeviceCountByChannelDTO {

    @Schema(description = "通道ID集合")
    private List<Long> channelIds;
}
