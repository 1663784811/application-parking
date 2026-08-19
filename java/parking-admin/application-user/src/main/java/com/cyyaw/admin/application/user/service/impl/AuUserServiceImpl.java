package com.cyyaw.admin.application.user.service.impl;

import com.cyyaw.admin.application.user.service.AuUserService;
import com.cyyaw.admin.dao.user.AuUserDao;
import com.cyyaw.admin.entity.module.user.AuUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuUserServiceImpl implements AuUserService {

    @Autowired
    private AuUserDao auUserDao;


    @Override
    public AuUser findUserByAccountAndAppId(String account, Long appId) {
        return auUserDao.findByAccountAndAppId(account, appId);
    }

    @Override
    public AuUser saveUser(AuUser user) {
        return auUserDao.save(user);
    }

    @Override
    public AuUser findUserById(Long id) {
        return auUserDao.selectById(id);
    }

    @Override
    public AuUser findUserByEmailAndAppId(String email, Long appId) {
        return auUserDao.findByEmailAndAppId(email, appId);
    }

    @Override
    public AuUser findUserByPhoneAndAppId(String phone, Long appId) {
        return auUserDao.findByPhoneAndAppId(phone, appId);
    }
}