package com.cyyaw.admin.application.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.application.user.service.AuLogService;
import com.cyyaw.admin.dao.user.AuLogDao;
import com.cyyaw.admin.entity.module.user.AuLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuLogServiceImpl implements AuLogService {

    @Autowired
    private AuLogDao auLogDao;

    @Override
    public AuLog findById(Long id) {
        return auLogDao.selectById(id);
    }

    @Override
    public Page<AuLog> findPage(Integer page, Integer size, QueryWrapper<AuLog> wrapper) {
        return auLogDao.selectPage(new Page<>(page, size), wrapper);
    }

}
