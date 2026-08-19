package com.cyyaw.admin.dao.user;

import com.cyyaw.admin.dao.BaseMapperPlus;
import com.cyyaw.admin.entity.module.user.AuEnterprise;
import org.apache.ibatis.annotations.Select;


public interface AuEnterpriseDao extends BaseMapperPlus<AuEnterpriseDao, AuEnterprise> {

    @Select("select * from au_enterprise where code = #{code}")
    AuEnterprise findByCode(String code);

    @Select("SELECT * FROM au_enterprise WHERE phone = #{phone}")
    AuEnterprise findByPhone(String phone);

}