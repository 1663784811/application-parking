package com.cyyaw.admin.application.user.service.impl;

import com.cyyaw.admin.application.user.service.AuEnterpriseService;
import com.cyyaw.admin.dao.user.AuEnterpriseDao;
import com.cyyaw.admin.entity.module.user.AuEnterprise;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class AuEnterpriseServiceImpl implements AuEnterpriseService {


    @Autowired
    private AuEnterpriseDao auEnterpriseDao;


    @Override
    public AuEnterprise findEnterpriseByCode(String eCode) {
        return auEnterpriseDao.findByCode(eCode);
    }

    @Override
    public AuEnterprise saveEnterprise(AuEnterprise enterprise) {
        enterprise.setEnId(0L);
        return auEnterpriseDao.save(enterprise);
    }

    @Override
    public AuEnterprise findEnterpriseById(Long enId) {
        AuEnterprise auEnterprise = auEnterpriseDao.selectById(enId);
        return auEnterprise;
    }

    @Override
    public AuEnterprise findEnterpriseByPhone(String phone) {
        return auEnterpriseDao.findByPhone(phone);
    }

    @Override
    public AuEnterprise findAnyEnterprise() {
        return auEnterpriseDao.findAny();
    }

}
