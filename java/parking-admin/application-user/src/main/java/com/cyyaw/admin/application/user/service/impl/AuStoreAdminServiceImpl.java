package com.cyyaw.admin.application.user.service.impl;

import com.cyyaw.admin.application.user.service.AuStoreAdminService;
import com.cyyaw.admin.dao.user.AuStoreAdminDao;
import com.cyyaw.admin.entity.module.user.AuStoreAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class AuStoreAdminServiceImpl implements AuStoreAdminService {

    @Autowired
    private AuStoreAdminDao auStoreAdminDao;


    @Override
    public AuStoreAdmin findByAccountAndStoreId(String account, Long storeId) {
       return auStoreAdminDao.findByAccountAndStoreId(account, storeId);
    }

    @Override
    public AuStoreAdmin findById(Long id) {
        return auStoreAdminDao.selectById(id);
    }
}
