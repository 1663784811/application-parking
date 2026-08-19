package com.cyyaw.admin.dao.user;

import com.cyyaw.admin.dao.BaseMapperPlus;
import com.cyyaw.admin.entity.module.user.AuMenu;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface AuMenuDao extends BaseMapperPlus<AuMenuDao, AuMenu> {

    @Select("SELECT * FROM au_menu WHERE en_id = #{enId}")
    List<AuMenu> findByEnId(Long enId);

    @Select("SELECT * FROM au_menu WHERE en_id = #{enId} AND system_role = #{systemRole} order by sort asc")
    List<AuMenu>  findMenuByEnIdAndSystemRole(Long enId, String systemRole);

    @Select("SELECT * FROM au_menu WHERE en_id = #{enId} AND system_role = #{systemRole} AND app_id = #{appId} order by sort asc")
    List<AuMenu>  findMenuByEnIdAndSystemRoleAndAppid(Long enId, String systemRole, Long appId);

    @Select("SELECT * FROM au_menu WHERE en_id = #{enId} AND app_id = #{appId} AND route_name = #{routeName} AND system_role = #{systemRole} AND name = #{name}")
    List<AuMenu> findByEnIdAndAppIdAndRouteNameAndSystemRoleAndName(Long enId, Long appId, String routeName,String systemRole, String name);


    @Select("SELECT max(sort) as sort FROM au_menu WHERE en_id = #{enId}")
    Integer selectMenuSortMaxValue(Long enId);

}