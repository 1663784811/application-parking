package com.cyyaw.admin.entity.dto.parking;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * H5 首页「附近停车场」列表项。
 * <p>
 * 只读展示用途：不含价格 —— 费率是首段/时段/封顶的阶梯组合，没有单一单价可展示，
 * 真实金额以出场查询（{@code /app/parking/exit/order}）为准。
 */
@Data
@Schema(description = "H5 附近停车场")
public class AppParkingVO {

    /** 雪花 ID 转字符串，避免 JS Number 精度丢失（本仓库既有约定） */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(description = "停车场ID")
    private Long id;

    @Schema(description = "停车场名称")
    private String name;

    @Schema(description = "位置")
    private String address;

    @Schema(description = "停车场图片URL")
    private String image;

    @Schema(description = "车位容量（pk_parking.capacity）")
    private Integer capacity;

    @Schema(description = "剩余车位 = capacity - 在场车辆数（status=0 且未逻辑删除）")
    private Integer remainSpaces;

    @Schema(description = "距当前位置的展示用距离，如 500m / 1.2km；未传坐标或该场无有效坐标时为 null")
    private String distance;

    @Schema(description = "距当前位置的米数，供前端自行排序；未传坐标或该场无有效坐标时为 null")
    private BigDecimal distanceMeters;

}