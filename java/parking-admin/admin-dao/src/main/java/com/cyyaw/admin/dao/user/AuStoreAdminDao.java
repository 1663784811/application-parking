package com.cyyaw.admin.dao.user;

import com.cyyaw.admin.dao.BaseMapperPlus;
import com.cyyaw.admin.entity.module.user.AuStoreAdmin;
import org.apache.ibatis.annotations.Select;



public interface AuStoreAdminDao extends BaseMapperPlus<AuStoreAdminDao, AuStoreAdmin> {

    @Select("SELECT * FROM au_store_admin WHERE account = #{account} AND store_id = #{storeId}")
    AuStoreAdmin findByAccountAndStoreId(String account, Long storeId);

}
