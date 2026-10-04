package com.cyyaw.admin.application.user.me.controller;

import com.cyyaw.admin.application.user.me.service.AppUserService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.user.AppCouponVO;
import com.cyyaw.admin.entity.dto.user.AppOrderVO;
import com.cyyaw.admin.entity.dto.user.AppUserBoardVO;
import com.cyyaw.admin.entity.utils.LoginInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * H5「我的」页面接口：一屏数据、优惠券、订单。
 * <p>
 * 归属范围 = 企业 enId + 我绑定的车牌集合。历史订单的 user_id / app_id 全是空的
 * （入场建单时没有登录态），所以不能按 user_id 过滤，只能按车牌。
 */
@Tag(name = "APP-用户-我的")
@RestController
@RequestMapping("/app/user/me")
public class AppUserController extends MeControllerBase {

    @Autowired
    private AppUserService appUserService;

    @Operation(summary = "我的页面一屏数据", description = "头部统计 + 列表统计 + 优惠券/订单/车辆/卡包列表")
    @GetMapping("/board")
    public BaseResult<AppUserBoardVO> board() {
        LoginInfo loginInfo = requireUser();
        if (loginInfo == null) {
            return BaseResult.fail("角色不对");
        }
        return BaseResult.ok(appUserService.board(loginInfo.getEnId(), loginInfo.getAppId(),
                loginInfo.getId(), phone(loginInfo.getId())));
    }

    @Operation(summary = "优惠券列表", description = "我的优惠券列表")
    @GetMapping("/coupon/list")
    public BaseResult<List<AppCouponVO>> couponList() {
        LoginInfo loginInfo = requireUser();
        if (loginInfo == null) {
            return BaseResult.fail("角色不对");
        }
        return BaseResult.ok(appUserService.couponList(loginInfo.getEnId(), loginInfo.getAppId()));
    }

    @Operation(summary = "订单列表", description = "我的停车订单列表，按入场时间倒序")
    @GetMapping("/order/list")
    public BaseResult<List<AppOrderVO>> orderList() {
        LoginInfo loginInfo = requireUser();
        if (loginInfo == null) {
            return BaseResult.fail("角色不对");
        }
        return BaseResult.ok(appUserService.orderList(loginInfo.getEnId(), loginInfo.getId(), phone(loginInfo.getId())));
    }
}
