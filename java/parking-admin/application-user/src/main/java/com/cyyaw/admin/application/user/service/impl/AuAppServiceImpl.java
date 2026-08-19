package com.cyyaw.admin.application.user.service.impl;


import com.cyyaw.admin.application.user.service.AuAppService;
import com.cyyaw.admin.dao.user.AuAppDao;
import com.cyyaw.admin.entity.module.user.AuApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuAppServiceImpl implements AuAppService {

    @Autowired
    private AuAppDao auAppDao;


    @Override
    public AuApp findAppById(Long appId) {
        return auAppDao.selectById(appId);
    }

    @Override
    public AuApp saveApp(AuApp auApp) {
        return auAppDao.save(auApp);
    }

    @Override
    public List<AuApp> findAppByEnIdAndType(Long enId, String type) {
        return auAppDao.findAppByEnIdAndType(enId, type);
    }
}
