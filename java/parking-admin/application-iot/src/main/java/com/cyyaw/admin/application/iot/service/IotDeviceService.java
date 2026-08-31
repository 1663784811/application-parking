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

    Map<String, Object> stats();

    /**
     * 按设备编码查询设备（MQTT 连接的 clientId 即设备 code）
     */
    IotDevice findByCode(String code);

}
