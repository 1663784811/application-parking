package com.cyyaw.admin.application.user.me.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cyyaw.admin.application.user.me.service.AppDefaultVehicleService;
import com.cyyaw.admin.dao.user.AppDefaultVehicleDao;
import com.cyyaw.admin.application.user.me.service.AppUserService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.user.AppVehicleSaveDTO;
import com.cyyaw.admin.entity.dto.user.AppVehicleVO;
import com.cyyaw.admin.entity.module.user.AppDefaultVehicle;
import com.cyyaw.admin.entity.utils.LoginInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * H5「我的车辆」接口：列表 / 保存 / 删除。
 * <p>
 * 入参只收车牌与车辆信息，归属字段（enId / appId / userId / phone）一律由服务端从登录态补齐 ——
 * 收前端传的归属字段就等于越权。
 */
@Tag(name = "APP-用户-我的车辆")
@RestController
@RequestMapping("/app/user/me/vehicle")
public class AppUserVehicleController extends MeControllerBase {

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private AppDefaultVehicleService appDefaultVehicleService;

    @Autowired
    private AppDefaultVehicleDao appDefaultVehicleDao;

    @Operation(summary = "我的车辆列表", description = "默认车辆排最前，其余按登记时间倒序")
    @GetMapping("/list")
    public BaseResult<List<AppVehicleVO>> list() {
        LoginInfo loginInfo = requireUser();
        if (loginInfo == null) {
            return BaseResult.fail("角色不对");
        }
        return BaseResult.ok(appUserService.vehicleList(loginInfo.getEnId(), loginInfo.getId(), phone(loginInfo.getId())));
    }

    @Operation(summary = "保存车辆", description = "id 为空是新增，有值是按记录更新")
    @PostMapping("/save")
    public BaseResult<AppDefaultVehicle> save(@RequestBody AppVehicleSaveDTO dto) {
        LoginInfo loginInfo = requireUser();
        if (loginInfo == null) {
            return BaseResult.fail("角色不对");
        }
        Long userId = loginInfo.getId();
        String ownerPhone = phone(userId);
        Long enId = loginInfo.getEnId();

        String plateNumber = dto.getPlateNumber() == null ? "" : dto.getPlateNumber().trim();
        if (plateNumber.isEmpty()) {
            return BaseResult.fail("车牌号不能为空");
        }

        // 同企业内车牌只能登记一次：一辆车不该能被两个账号各自绑上
        // 更新时排除自己这条，否则不改车牌也会因为撞上自己的记录被拒
        for (AppDefaultVehicle v : appDefaultVehicleDao.selectList(
                new QueryWrapper<AppDefaultVehicle>().eq("del_time", 0).eq("en_id", enId))) {
            if (dto.getId() != null && dto.getId().equals(v.getId())) {
                continue;
            }
            if (plateNumber.equals(v.getPlate())) {
                return BaseResult.fail("该车牌已被登记");
            }
        }

        // 更新走 selectById + updateById，不验归属就能改任意人的车辆，所以先确认这条记录是我的
        if (dto.getId() != null) {
            AppDefaultVehicle own = appDefaultVehicleService.findById(dto.getId(), enId, userId, ownerPhone);
            if (own == null) {
                return BaseResult.fail("车辆不存在");
            }
        }

        AppDefaultVehicle vehicle = new AppDefaultVehicle();
        vehicle.setId(dto.getId());
        vehicle.setPlate(plateNumber);
        vehicle.setVehicleType(dto.getVehicleType() == null || dto.getVehicleType().isBlank()
                ? "小型汽车" : dto.getVehicleType());
        vehicle.setIsDefault(Boolean.TRUE.equals(dto.getIsDefault()) ? 1 : 0);

        AppDefaultVehicle saved = appDefaultVehicleService.saveWithDefault(vehicle,
                loginInfo.getEnId(), loginInfo.getAppId(), loginInfo.getId(), phone(loginInfo.getId()));
        return saved == null ? BaseResult.fail("保存失败") : BaseResult.ok(saved);
    }

    @Operation(summary = "删除车辆", description = "软删除，del_time 置为当前时间戳")
    @DeleteMapping("/delete/{id}")
    public BaseResult<Boolean> delete(@PathVariable("id") Long id) {
        LoginInfo loginInfo = requireUser();
        if (loginInfo == null) {
            return BaseResult.fail("角色不对");
        }
        boolean deleted = appDefaultVehicleService.deleteById(id,
                loginInfo.getEnId(), loginInfo.getId(), phone(loginInfo.getId()));
        return deleted ? BaseResult.ok(true) : BaseResult.fail("删除失败");
    }
}
