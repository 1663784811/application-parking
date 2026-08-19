package com.cyyaw.admin.dao.user;

import com.cyyaw.admin.dao.BaseMapperPlus;
import com.cyyaw.admin.entity.module.user.AuRole;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface AuRoleDao extends BaseMapperPlus<AuRoleDao, AuRole> {

    /**
     * 根据角色编码查询
     */
    AuRole findByCode(String code);

    @Select("SELECT * FROM au_role WHERE id IN (SELECT t.role_id FROM au_admin_role t WHERE t.admin_id = #{adminId})")
    List<AuRole> findAdminRoleByAdminId(Long adminId);

}