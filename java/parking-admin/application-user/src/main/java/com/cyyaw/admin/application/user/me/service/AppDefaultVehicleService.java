package com.cyyaw.admin.application.user.me.service;

import com.cyyaw.admin.entity.module.user.AppDefaultVehicle;

import java.util.List;

/**
 * 我的车辆。
 * <p>
 * 归属维度是「账号 ID 优先、手机号兜底」：车牌识别在出入口没有登录态，
 * 需要按企业 ID 找到车主、再匹配手机号或用户 ID，所以 save/list/delete 都带 owner 维度参数，
 * 由实现里拼成同一套 QueryWrapper 条件。
 */
public interface AppDefaultVehicleService {

    /**
     * 查某车主的车辆列表（del_time=0，按创建时间倒序）。
     *
     * @param enId   企业ID
     * @param userId 用户ID，可为 null（未登录时按手机号归属）
     * @param phone  手机号，userId 为空时生效
     */
    List<AppDefaultVehicle> findList(Long enId, Long userId, String phone);

    /**
     * 按 ID 查单条，且必须属于传入的归属范围（防止用别人的 ID 越权读）。
     */
    AppDefaultVehicle findById(Long id, Long enId, Long userId, String phone);

    /**
     * 新增或更新车辆（id 非空且存在则更新，否则新增）。
     * 保存成功且 isDefault=1 时，清掉该车主名下其他记录的默认标记。
     */
    AppDefaultVehicle saveWithDefault(AppDefaultVehicle vehicle, Long enId, Long appId, Long userId, String phone);

    /**
     * 软删除（del_time 置为当前毫秒时间戳，与库内其他表约定一致）。
     *
     * @return 是否真的删到了
     */
    boolean deleteById(Long id, Long enId, Long userId, String phone);
}
