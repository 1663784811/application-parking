package com.cyyaw.admin.entity.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * H5「我的」页面一屏数据。
 * <p>
 * 一次请求把头部三格统计 + 列表三格统计 + 各列表接口共用取数全带回来，
 * 省掉页面挂载时连发 6 个请求。
 */
@Data
@Schema(description = "H5 我的页面数据")
public class AppUserBoardVO {

    // ================= 会员状态 =================

    @Schema(description = "是否月卡/季卡/年卡会员：有有效期内的 me_member 记录且未冻结")
    private Boolean member;

    @Schema(description = "会员类型文案（VIP会员），无有效会员卡时为 null")
    private String memberType;

    @Schema(description="当前有效的会员卡到期时间；无会员时为 null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime memberExpireTime;

    // ================= 头部统计 =================

    @Schema(description = "累计停车次数 = 该车主已出场的停车记录数（pk_car_log.status=1）")
    private Long parkingTimes;

    @Schema(description = "累计停车时长小时数（保留 1 位小数），前端自行拼「小时」")
    private BigDecimal parkingHours;

    // ================= 列表统计 =================

    @Schema(description = "优惠券数量")
    private Long couponCount;

    @Schema(description = "订单数量")
    private Long orderCount;

    @Schema(description = "车辆数量")
    private Long vehicleCount;

    @Schema(description = "卡包数量 = 有效会员卡数")
    private Long cardCount;

    // ================= 列表数据 =================

    @Schema(description = "优惠券列表")
    private List<AppCouponVO> couponList;

    @Schema(description = "订单列表（按入场时间倒序，最多 20 条）")
    private List<AppOrderVO> orderList;

    @Schema(description = "我的车辆列表")
    private List<AppVehicleVO> vehicleList;

    @Schema(description = "卡包列表")
    private List<AppCardVO> cardList;

}
