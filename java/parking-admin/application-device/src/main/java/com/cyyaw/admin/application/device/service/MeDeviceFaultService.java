package com.cyyaw.admin.application.device.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.entity.module.device.MeDeviceFault;

import java.time.LocalDateTime;
import java.util.Map;

public interface MeDeviceFaultService {

    MeDeviceFault findById(Long id);

    Page<MeDeviceFault> findPage(Integer page, Integer size, QueryWrapper<MeDeviceFault> wrapper);

    MeDeviceFault create(Long deviceId, String faultType);

    MeDeviceFault handle(Long id, String action, String repairer, LocalDateTime expectCompleteTime);

    Map<String, Object> stats();

}
