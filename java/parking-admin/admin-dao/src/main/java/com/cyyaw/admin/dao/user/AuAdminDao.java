package com.cyyaw.admin.dao.user;

import com.cyyaw.admin.dao.BaseMapperPlus;
import com.cyyaw.admin.entity.module.user.AuAdmin;
import org.apache.ibatis.annotations.Select;

public interface AuAdminDao extends BaseMapperPlus<AuAdminDao, AuAdmin> {

    @Select("select * from au_admin where account = #{account} and en_id = #{enId}")
    AuAdmin findAdminByAccountAndEnId(String account, Long enId);

}