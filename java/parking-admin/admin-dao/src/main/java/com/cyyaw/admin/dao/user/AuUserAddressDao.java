package com.cyyaw.admin.dao.user;

import com.cyyaw.admin.dao.BaseMapperPlus;
import com.cyyaw.admin.entity.module.user.AuUserAddress;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AuUserAddressDao extends BaseMapperPlus<AuUserAddressDao, AuUserAddress> {

    @Select("select * from au_user_address where user_id = #{userId} and `def` = 1")
    List<AuUserAddress> selectDefAddressByUserId(Long userId);

}