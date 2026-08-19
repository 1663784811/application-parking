package com.cyyaw.admin.application.user.service.impl;

import com.cyyaw.admin.application.user.service.AuPermissionService;
import com.cyyaw.admin.dao.user.AuPermissionDao;
import com.cyyaw.admin.entity.module.user.AuPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;


@Service
public class AuPermissionServiceImpl implements AuPermissionService {

    @Autowired
    private AuPermissionDao auPermissionDao;


    @Override
    public String findPermissionByAdminId(Long adminId) {
        // 查角色权限
        List<AuPermission> permissionList = auPermissionDao.findPermissionsByAdminId(adminId);
        // 查已禁用的权限
        List<AuPermission> notPermissionList = auPermissionDao.findByAdminNotPermission(adminId);
        HashSet<String> permissionSet = new HashSet<>();
        for (AuPermission permission : permissionList) {
            permissionSet.add(permission.getCode());
        }
        for (AuPermission permission : notPermissionList) {
            permissionSet.remove(permission.getCode());
        }
        StringBuilder sb = new StringBuilder();
        for (String permissionCode : permissionSet) {
            if(!sb.isEmpty()) sb.append(",");
            sb.append(permissionCode);
        }
        return sb.toString();
    }
}
