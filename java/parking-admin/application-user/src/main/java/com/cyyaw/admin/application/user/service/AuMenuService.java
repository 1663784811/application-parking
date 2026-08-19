package com.cyyaw.admin.application.user.service;



import com.cyyaw.admin.entity.module.user.AuMenu;
import com.cyyaw.admin.entity.utils.TreeResponseEntity;

import java.util.List;

public interface AuMenuService {


    List<AuMenu> findMenuByEnId(Long eCode);


    /**
     * 查询登录用户菜单
     */
    List<TreeResponseEntity.Node<AuMenu>> findLoginAdminMenu();

    /**
     * 初始化菜单
     * @param enId
     * @param filePath
     * @param systemRole
     */
    void initMenu(Long enId, String filePath, String systemRole, Long appId, String params, Integer sort);

}
