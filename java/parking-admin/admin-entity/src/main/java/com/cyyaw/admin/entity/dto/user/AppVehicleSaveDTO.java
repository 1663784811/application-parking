package com.cyyaw.admin.entity.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * H5「我的车辆」保存入参。
 * <p>
 * 单独一个 DTO 而不是直接收 {@code AppDefaultVehicle}：
 * 前端传的是 JSON boolean（{@code isDefault: true}），实体里是 Integer 1/0，
 * 中间这层显式转一下，省得依赖 Jackson 的类型强转行为。
 * enId / appId / userId / phone 一律不收 —— 服务端从登录态取，信前端就等于越权。
 */
@Data
@Schema(description = "我的车辆保存入参")
public class AppVehicleSaveDTO {

    @Schema(description = "车辆记录ID；为空表示新增，有值表示更新该条")
    private Long id;

    @Schema(description = "车牌号")
    private String plateNumber;

    @Schema(description = "车辆类型，默认小型汽车")
    private String vehicleType;

    @Schema(description = "是否默认车辆")
    private Boolean isDefault;

}
