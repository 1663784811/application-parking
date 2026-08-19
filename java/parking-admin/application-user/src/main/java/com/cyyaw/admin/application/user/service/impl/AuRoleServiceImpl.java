package com.cyyaw.admin.application.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.cyyaw.admin.application.user.service.AuRoleService;
import com.cyyaw.admin.dao.user.AuRoleDao;
import com.cyyaw.admin.entity.module.user.AuRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuRoleServiceImpl implements AuRoleService {

    @Autowired
    private AuRoleDao auRoleDao;


    @Override
    public String findAdminRoleByAdminId(Long adminId) {
        StringBuilder sb = new StringBuilder();
        List<AuRole> roleList = auRoleDao.findAdminRoleByAdminId(adminId);
        for (AuRole auRole : roleList) {
            String code = auRole.getCode();
            if (StrUtil.isNotBlank(code)) {
                if (!sb.isEmpty()) {
                    sb.append(",").append(code);
                } else {
                    sb.append(code);
                }
            }
        }
        return sb.toString();
    }
}