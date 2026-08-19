package com.cyyaw.admin.dao.user;

import com.cyyaw.admin.dao.BaseMapperPlus;
import com.cyyaw.admin.entity.module.user.AuPermission;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface AuPermissionDao extends BaseMapperPlus<AuPermissionDao, AuPermission> {

    @Select("SELECT * FROM au_permission WHERE id IN (SELECT t.permission_id FROM au_role_permission t WHERE t.role_id IN (SELECT a.role_id FROM au_admin_role a WHERE a.admin_id = #{adminId}))")
    List<AuPermission> findPermissionsByAdminId(Long adminId);

    @Select("SELECT * FROM au_permission WHERE id IN (SELECT t.permission_id FROM au_admin_not_permission t WHERE t.admin_id = #{adminId})")
    List<AuPermission> findByAdminNotPermission(Long adminId);
}