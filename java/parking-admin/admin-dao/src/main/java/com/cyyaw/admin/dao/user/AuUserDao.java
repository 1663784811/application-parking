package com.cyyaw.admin.dao.user;

import com.cyyaw.admin.dao.BaseMapperPlus;
import com.cyyaw.admin.entity.module.user.AuUser;
import org.apache.ibatis.annotations.Select;



public interface AuUserDao extends BaseMapperPlus<AuUserDao, AuUser> {

    @Select("SELECT * FROM au_user WHERE account = #{account} AND app_id = #{appId}")
    AuUser findByAccountAndAppId(String account, Long appId);

    @Select("SELECT * FROM au_user WHERE email = #{email} AND app_id = #{appId}")
    AuUser findByEmailAndAppId(String email, Long appId);

    @Select("SELECT * FROM au_user WHERE phone = #{phone} AND app_id = #{appId}")
    AuUser findByPhoneAndAppId(String phone, Long appId);
}