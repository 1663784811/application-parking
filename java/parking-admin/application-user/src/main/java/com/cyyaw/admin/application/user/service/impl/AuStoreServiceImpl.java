package com.cyyaw.admin.application.user.service.impl;

import com.cyyaw.admin.application.user.service.AuStoreService;
import com.cyyaw.admin.dao.user.AuStoreDao;
import com.cyyaw.admin.entity.module.user.AuStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class AuStoreServiceImpl implements AuStoreService {

    @Autowired
    private AuStoreDao auStoreDao;

    @Override
    public AuStore findById(Long id) {
        return auStoreDao.selectById(id);
    }


}
