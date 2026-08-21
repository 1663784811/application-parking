package com.cyyaw.admin.application.user.service;


import com.cyyaw.admin.entity.module.user.AuEnterprise;

public interface AuEnterpriseService {


    AuEnterprise findEnterpriseByCode(String eCode);

    AuEnterprise saveEnterprise(AuEnterprise enterprise);

    AuEnterprise findEnterpriseById(Long enId);


    AuEnterprise findEnterpriseByPhone(String phone);

    /**
     * 查询是否存在企业（只取一条）。无企业时返回 null。
     */
    AuEnterprise findAnyEnterprise();

}
