package com.cyyaw.admin.application.iot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.entity.module.iot.IotDevice;

import java.util.Map;

public interface IotDeviceService {

    IotDevice findById(Long id);

    IotDevice save(IotDevice device);

    void delete(Long id);

    Page<IotDevice> findPage(Integer page, Integer size, QueryWrapper<IotDevice> wrapper);

    void restart(Long id);

    /**
     * 修改设备账号与密码（TODO: 实际应加密存储，当前与 save 一致按原值写入）
     */
    void changePassword(Long id, String account, String password);

    Map<String, Object> stats();

    /**
     * 按设备编码查询设备（MQTT 连接的 clientId 即设备 code）
     */
    IotDevice findByCode(String code);

    /**
     * 更新设备在线状态（MQTT 客户端连接成功/断开时由 broker 转发调用）
     *
     * @param code         设备编码（即 MQTT clientId）
     * @param onlineStatus 1=在线, 0=离线
     */
    void updateOnlineStatus(String code, Integer onlineStatus);

}
