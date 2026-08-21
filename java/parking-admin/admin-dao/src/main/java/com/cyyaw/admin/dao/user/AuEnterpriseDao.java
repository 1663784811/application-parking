package com.cyyaw.admin.dao.user;

import com.cyyaw.admin.dao.BaseMapperPlus;
import com.cyyaw.admin.entity.module.user.AuEnterprise;
import org.apache.ibatis.annotations.Select;


public interface AuEnterpriseDao extends BaseMapperPlus<AuEnterpriseDao, AuEnterprise> {

    @Select("select * from au_enterprise where code = #{code}")
    AuEnterprise findByCode(String code);

    @Select("SELECT * FROM au_enterprise WHERE phone = #{phone}")
    AuEnterprise findByPhone(String phone);

    /**
     * 查询是否存在企业（只取一条，用于判断系统是否已初始化）
     * del_time 未标 @TableLogic，此处手动过滤软删除
     */
    @Select("SELECT * FROM au_enterprise WHERE del_time = 0 LIMIT 1")
    AuEnterprise findAny();

}