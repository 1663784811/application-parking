package com.cyyaw.admin.application.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cyyaw.admin.application.user.service.AuRoleService;
import com.cyyaw.admin.dao.user.AuAdminRoleDao;
import com.cyyaw.admin.dao.user.AuRoleDao;
import com.cyyaw.admin.entity.module.user.AuAdminRole;
import com.cyyaw.admin.entity.module.user.AuRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuRoleServiceImpl implements AuRoleService {

    @Autowired
    private AuRoleDao auRoleDao;

    @Autowired
    private AuAdminRoleDao auAdminRoleDao;

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

    @Override
    public List<AuRole> findAll() {
        List<AuRole> roles = auRoleDao.selectList(new QueryWrapper<AuRole>().orderByDesc("create_time"));
        // 填充各角色成员数（按 au_admin_role 统计）
        for (AuRole role : roles) {
            Long count = auAdminRoleDao.selectCount(new QueryWrapper<AuAdminRole>().eq("role_id", role.getId()));
            role.setMemberCount(count == null ? 0 : count.intValue());
        }
        return roles;
    }

    @Override
    public AuRole findById(Long id) {
        return auRoleDao.selectById(id);
    }

    @Override
    public AuRole save(AuRole role) {
        // 新增角色时编码缺省则按名称生成（前缀 role_ 避免与系统角色编码 Admin/Store 冲突）；
        // 编辑时（id 已存在）不改动编码，由 BaseMapperPlus.save 的 copy 逻辑保留原值
        if (role.getId() == null && StrUtil.isBlank(role.getCode())) {
            role.setCode("role_" + role.getName());
        }
        return auRoleDao.save(role);
    }

    @Override
    public void delete(Long id) {
        auRoleDao.deleteById(id);
    }
}
