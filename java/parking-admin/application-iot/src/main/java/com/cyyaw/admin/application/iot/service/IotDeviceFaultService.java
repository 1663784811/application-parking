package com.cyyaw.admin.application.iot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.entity.module.iot.IotDeviceFault;

import java.time.LocalDateTime;
import java.util.Map;

public interface IotDeviceFaultService {

    IotDeviceFault findById(Long id);

    Page<IotDeviceFault> findPage(Integer page, Integer size, QueryWrapper<IotDeviceFault> wrapper);

    IotDeviceFault create(Long deviceId, String faultType);

    IotDeviceFault handle(Long id, String action, String repairer, LocalDateTime expectCompleteTime);

    Map<String, Object> stats();

}
