package com.cyyaw.admin.application.user.me.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.cyyaw.admin.application.user.me.service.AppDefaultVehicleService;
import com.cyyaw.admin.dao.user.AppDefaultVehicleDao;
import com.cyyaw.admin.entity.module.user.AppDefaultVehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppDefaultVehicleServiceImpl implements AppDefaultVehicleService {

    @Autowired
    private AppDefaultVehicleDao appDefaultVehicleDao;

    /**
     * 拼归属条件：userId 有值走账号维度，否则退回手机号维度。
     * del_time = 0 是硬条件 —— 库内未开 MP 逻辑删除，得自己过滤。
     */
    private void applyOwner(QueryWrapper<AppDefaultVehicle> wrapper, Long enId, Long userId, String phone) {
        wrapper.eq("del_time", 0);
        if (enId != null) {
            wrapper.eq("en_id", enId);
        }
        if (userId != null) {
            wrapper.eq("user_id", userId);
        } else {
            wrapper.eq("phone", phone);
        }
    }

    @Override
    public List<AppDefaultVehicle> findList(Long enId, Long userId, String phone) {
        QueryWrapper<AppDefaultVehicle> wrapper = new QueryWrapper<>();
        applyOwner(wrapper, enId, userId, phone);
        // 默认车辆排最前，其余按登记时间倒序
        wrapper.orderByDesc("is_default").orderByDesc("create_time");
        return appDefaultVehicleDao.selectList(wrapper);
    }

    @Override
    public AppDefaultVehicle findById(Long id, Long enId, Long userId, String phone) {
        AppDefaultVehicle vehicle = appDefaultVehicleDao.selectById(id);
        if (vehicle == null) {
            return null;
        }
        // 必须再验一次归属：光有 id 不该能看到别人绑的车
        QueryWrapper<AppDefaultVehicle> wrapper = new QueryWrapper<>();
        applyOwner(wrapper, enId, userId, phone);
        wrapper.eq("id", id);
        List<AppDefaultVehicle> own = appDefaultVehicleDao.selectList(wrapper);
        return own.isEmpty() ? null : own.get(0);
    }

    @Override
    @Transactional
    public AppDefaultVehicle saveWithDefault(AppDefaultVehicle vehicle, Long enId, Long appId, Long userId, String phone) {
        vehicle.setEnId(enId);
        vehicle.setAppId(appId);
        vehicle.setUserId(userId);
        vehicle.setPhone(phone);
        vehicle.setDelTime(0);
        vehicle.setUpdateTime(LocalDateTime.now());
        if (vehicle.getVehicleType() == null || vehicle.getVehicleType().isBlank()) {
            vehicle.setVehicleType("小型汽车");
        }
        AppDefaultVehicle saved = appDefaultVehicleDao.save(vehicle);
        if (saved != null && saved.getId() != null && Integer.valueOf(1).equals(saved.getIsDefault())) {
            clearOtherDefaults(enId, userId, phone, saved.getId());
        }
        return saved;
    }

    /**
     * 清掉同车主名下其他记录的默认标记。
     * 必须用 UpdateWrapper 显式 set：MP 的 save/updateById 是 NOT_NULL 策略，null 不会清掉旧值。
     */
    private void clearOtherDefaults(Long enId, Long userId, String phone, Long keepId) {
        UpdateWrapper<AppDefaultVehicle> wrapper = new UpdateWrapper<>();
        if (enId != null) {
            wrapper.eq("en_id", enId);
        }
        if (userId != null) {
            wrapper.eq("user_id", userId);
        } else {
            wrapper.eq("phone", phone);
        }
        wrapper.eq("del_time", 0)
                .ne("id", keepId)
                .set("is_default", 0)
                .set("update_time", LocalDateTime.now());
        appDefaultVehicleDao.update(null, wrapper);
    }

    @Override
    public boolean deleteById(Long id, Long enId, Long userId, String phone) {
        AppDefaultVehicle vehicle = findById(id, enId, userId, phone);
        if (vehicle == null) {
            return false;
        }
        // del_time 列是 int，超过 INT 上限就顶格存，语义仍是「已删除」
        long now = System.currentTimeMillis();
        vehicle.setDelTime(now > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) now);
        vehicle.setUpdateTime(LocalDateTime.now());
        return appDefaultVehicleDao.updateById(vehicle) > 0;
    }
}
