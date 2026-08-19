package com.cyyaw.admin.application.user.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.cyyaw.admin.application.user.service.AuMenuService;
import com.cyyaw.admin.dao.user.AuMenuDao;
import com.cyyaw.admin.entity.em.SystemRoleEnum;
import com.cyyaw.admin.entity.module.user.AuMenu;
import com.cyyaw.admin.entity.utils.LoginInfo;
import com.cyyaw.admin.entity.utils.LoginInfoContext;
import com.cyyaw.admin.entity.utils.TreeResponseEntity;
import io.micrometer.core.instrument.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
public class AuMenuServiceImpl implements AuMenuService {


    @Autowired
    private AuMenuDao auMenuDao;

    @Autowired
    private ResourceLoader resourceLoader;


    @Override
    public List<AuMenu> findMenuByEnId(Long eCode) {
        List<AuMenu> auMenuList = auMenuDao.findByEnId(eCode);
        return auMenuList;
    }

    @Override
    public List<TreeResponseEntity.Node<AuMenu>> findLoginAdminMenu() {
        LoginInfo loginInfo = LoginInfoContext.getLoginInfo();
        String systemRole = loginInfo.getSystemRole();
        Long appId = loginInfo.getAppId();
        Long enId = loginInfo.getEnId();
        List<AuMenu> auMenuList = null;
        if (systemRole.equals(SystemRoleEnum.Admin.getRole())) {
            // 查询企业 管理员菜单
            auMenuList = auMenuDao.findMenuByEnIdAndSystemRole(enId, systemRole);
        } else if (systemRole.equals(SystemRoleEnum.Store.getRole())) {
            auMenuList = auMenuDao.findMenuByEnIdAndSystemRoleAndAppid(enId, systemRole, appId);
        }
        TreeResponseEntity<AuMenu> res = new TreeResponseEntity<>();
        if (null != auMenuList && !auMenuList.isEmpty()) {
            auMenuList.forEach(auMenu -> {
                TreeResponseEntity.Node<AuMenu> node = new TreeResponseEntity.Node<>();
                node.setId(auMenu.getId() == null ? null : String.valueOf(auMenu.getId()));
                node.setPid(auMenu.getPid() == null ? null : String.valueOf(auMenu.getPid()));
                node.setTitle(auMenu.getName());
                node.setData(auMenu);
                res.add(node);
            });
        }
        return res.getRoot();
    }


    /**
     * 初始化企业菜单
     */
    @Transactional
    public void initMenu(Long enId, String filePath, String systemRole, Long appId, String params, Integer sort) {
        JSONArray menuList = getInitMenu(filePath);
        saveMenu(menuList, enId, null, systemRole, appId, params, sort);
    }


    @Transactional
    public void saveMenu(JSONArray menuList, Long enId, Long pid, String systemRole, Long appId, String params, Integer sort) {
        if (menuList != null && !menuList.isEmpty()) {
            for (int i = 0; i < menuList.size(); i++) {
                JSONObject json = menuList.getJSONObject(i);
                String routeName = json.getStr("routeName", "");
                String title = json.getStr("title", "");
                AuMenu auMenu = null;
                List<AuMenu> auMenuList = auMenuDao.findByEnIdAndAppIdAndRouteNameAndSystemRoleAndName(enId, appId, routeName, systemRole, title);
                if (auMenuList.isEmpty()) {
                    auMenu = new AuMenu();
                } else {
                    auMenu = auMenuList.get(0);
                }
                auMenu.setEnId(enId);
                auMenu.setPid(pid);
                auMenu.setSystemRole(systemRole);
                auMenu.setName(title);
                auMenu.setIcon(json.getStr("icon"));
                auMenu.setRouteName(routeName);
                auMenu.setParams(params);
                auMenu.setAppId(appId);
                auMenu.setNote("");
                auMenu.setSort(sort + i);
                auMenuDao.save(auMenu);
                JSONArray children = json.getJSONArray("children");
                saveMenu(children, enId, auMenu.getId(), systemRole, appId, params, auMenu.getSort());
            }
        }
    }


    /**
     * 获取初始化菜单
     */
    private JSONArray getInitMenu(String filePath) {
        try {
            Resource resource = resourceLoader.getResource(filePath);
            InputStream inputStream = resource.getInputStream();
            String json = IOUtils.toString(inputStream);
            return new JSONArray(json);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }


}
