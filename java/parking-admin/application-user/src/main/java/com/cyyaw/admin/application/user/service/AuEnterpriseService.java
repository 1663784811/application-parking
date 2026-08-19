package com.cyyaw.admin.application.user.service;


import com.cyyaw.admin.entity.module.user.AuEnterprise;

public interface AuEnterpriseService {


    AuEnterprise findEnterpriseByCode(String eCode);

    AuEnterprise saveEnterprise(AuEnterprise enterprise);

    AuEnterprise findEnterpriseById(Long enId);


    AuEnterprise findEnterpriseByPhone(String phone);

}
