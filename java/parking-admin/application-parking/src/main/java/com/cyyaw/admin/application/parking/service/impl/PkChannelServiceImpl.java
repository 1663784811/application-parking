package com.cyyaw.admin.application.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.application.parking.service.PkChannelService;
import com.cyyaw.admin.dao.parking.PkChannelDao;
import com.cyyaw.admin.entity.module.parking.PkChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PkChannelServiceImpl implements PkChannelService {

    @Autowired
    private PkChannelDao pkChannelDao;

    @Override
    public PkChannel findById(Long id) {
        return pkChannelDao.selectById(id);
    }

    @Override
    public PkChannel save(PkChannel channel) {
        return pkChannelDao.save(channel);
    }

    @Override
    public void delete(Long id) {
        pkChannelDao.deleteById(id);
    }

    @Override
    public Page<PkChannel> findPage(Integer page, Integer size, QueryWrapper<PkChannel> wrapper) {
        return pkChannelDao.selectPage(new Page<>(page, size), wrapper);
    }

}
