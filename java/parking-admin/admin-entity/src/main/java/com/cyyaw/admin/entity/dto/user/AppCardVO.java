package com.cyyaw.admin.entity.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * H5 卡包列表项（me_member 的有效会员卡展示映射）。
 * <p>
 * 字段名对齐前端 CardPackage.vue 既有渲染结构（id/name/parkingName/expireTime/status/totalTimes/remainTimes/color）。
 * 本系统的卡都是时长卡（月卡/季卡/年卡），没有次卡，所以 totalTimes/remainTimes 恒为 null，前端据此隐藏次数行。
 */
@Data
@Schema(description = "H5 卡包")
public class AppCardVO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(description = "会员卡ID（me_member.id）")
    private Long id;

    @Schema(description = "卡片名称，如「月卡 · 京A12345」")
    private String name;

    @Schema(description = "卡类型：1 月卡，2 季卡，3 年卡")
    private Integer type;

    @Schema(description = "适用停车场名称（按 app_id 下可用停车场拼接），无停车场时为 null")
    private String parkingName;

    @Schema(description = "到期时间（yyyy-MM-dd HH:mm:ss）")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime expireTime;

    @Schema(description = "0 有效，1 即将过期（7 天内），2 已过期，3 已冻结")
    private Integer status;

    @Schema(description = "总次数，时长卡为 null")
    private Integer totalTimes;

    @Schema(description = "剩余次数，时长卡为 null")
    private Integer remainTimes;

    @Schema(description = "卡片主色，前端渐变底用；按卡类型区分")
    private String color;

    @Schema(description = "累计充值金额")
    private BigDecimal totalAmount;

}
