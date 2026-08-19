package com.cyyaw.admin.dao.user;

import com.cyyaw.admin.dao.BaseMapperPlus;
import com.cyyaw.admin.entity.module.user.AuApp;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface AuAppDao extends BaseMapperPlus<AuAppDao,AuApp> {

    @Select("select * from au_app m where m.en_id = #{enId} and m.type = #{type} ")
    List<AuApp> findAppByEnIdAndType(Long enId, String type);




}