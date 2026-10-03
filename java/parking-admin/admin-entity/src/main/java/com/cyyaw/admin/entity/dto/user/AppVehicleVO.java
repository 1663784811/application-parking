package com.cyyaw.admin.entity.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * H5 我的车辆列表项（me_member 的只读展示映射）。
 * <p>
 * 字段名对齐前端 Vehicle.vue 既有渲染结构（id/plateNumber/vehicleType/isDefault）。
 * isDefault 取 me_member.default 标记位：设为默认时清掉同车牌下其他记录的标记。
 */
@Data
@Schema(description = "H5 我的车辆")
public class AppVehicleVO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(description = "车辆ID（me_member.id）")
    private Long id;

    @Schema(description = "车牌号")
    private String plateNumber;

    @Schema(description = "车辆类型；未登记时按车牌前缀推测，见服务端的类型判定")
    private String vehicleType;

    @Schema(description = "是否默认车辆")
    private Boolean isDefault;

    @Schema(description = "该车牌累计停车次数（已出场记录数）")
    private Long parkingTimes;

}
