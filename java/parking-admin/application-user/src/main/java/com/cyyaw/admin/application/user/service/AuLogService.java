package com.cyyaw.admin.application.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.entity.module.user.AuLog;

public interface AuLogService {

    AuLog findById(Long id);

    Page<AuLog> findPage(Integer page, Integer size, QueryWrapper<AuLog> wrapper);

}
