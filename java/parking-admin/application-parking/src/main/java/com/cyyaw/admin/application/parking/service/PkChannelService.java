package com.cyyaw.admin.application.parking.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.entity.module.parking.PkChannel;

public interface PkChannelService {

    PkChannel findById(Long id);

    PkChannel save(PkChannel channel);

    void delete(Long id);

    Page<PkChannel> findPage(Integer page, Integer size, QueryWrapper<PkChannel> wrapper);

}
