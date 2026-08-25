package com.cyyaw.admin.application.device.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.entity.module.device.MeDevice;

import java.util.Map;

public interface MeDeviceService {

    MeDevice findById(Long id);

    MeDevice save(MeDevice device);

    void delete(Long id);

    Page<MeDevice> findPage(Integer page, Integer size, QueryWrapper<MeDevice> wrapper);

    void restart(Long id);

    Map<String, Object> stats();

}
