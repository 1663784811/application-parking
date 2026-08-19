package com.cyyaw.admin.application.user.service.impl;

import com.cyyaw.admin.application.user.service.AuAdminService;
import com.cyyaw.admin.dao.user.AuAdminDao;
import com.cyyaw.admin.entity.module.user.AuAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuAdminServiceImpl implements AuAdminService {


    @Autowired
    private AuAdminDao auAdminDao;


    @Override
    public AuAdmin findAdminByAccountAndEnId(String account, Long enId) {
        return auAdminDao.findAdminByAccountAndEnId(account, enId);
    }

    @Override
    public AuAdmin saveAdmin(AuAdmin auAdmin) {
        return auAdminDao.save(auAdmin);
    }

    @Override
    public AuAdmin findByAdminId(Long adminId) {
        return auAdminDao.selectById(adminId);
    }


}
