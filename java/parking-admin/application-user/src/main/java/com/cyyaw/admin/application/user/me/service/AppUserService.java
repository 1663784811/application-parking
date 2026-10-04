package com.cyyaw.admin.application.user.me.service;

import com.cyyaw.admin.entity.dto.user.AppCouponVO;
import com.cyyaw.admin.entity.dto.user.AppOrderVO;
import com.cyyaw.admin.entity.dto.user.AppUserBoardVO;
import com.cyyaw.admin.entity.dto.user.AppVehicleVO;

import java.util.List;

/**
 * H5「我的」页面的后端逻辑。
 * <p>
 * 归属范围统一为：企业 enId + 我绑定的车牌集合。
 * 车牌来自「我的车辆」（app_default_vehicle），不来自订单的 user_id ——
 * 见 AppOrderDao#selectByPlates 的注释，历史订单的 user_id 全是空的。
 */
public interface AppUserService {

    /** 我的车辆列表，按默认车辆优先排序。 */
    List<AppVehicleVO> vehicleList(Long enId, Long userId, String phone);

    /** 优惠券列表。 */
    List<AppCouponVO> couponList(Long enId, Long appId);

    /** 订单列表（按车牌匹配）。 */
    List<AppOrderVO> orderList(Long enId, Long userId, String phone);

    /** 一次性取「我的」页面整页数据，前端一次请求拿全。 */
    AppUserBoardVO board(Long enId, Long appId, Long userId, String phone);
}
