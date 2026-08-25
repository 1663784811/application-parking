package com.cyyaw.admin.application.user.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.application.user.service.AuAdminService;
import com.cyyaw.admin.common.WebException;
import com.cyyaw.admin.dao.user.AuAdminDao;
import com.cyyaw.admin.dao.user.AuAdminRoleDao;
import com.cyyaw.admin.dao.user.AuRoleDao;
import com.cyyaw.admin.entity.module.user.AuAdmin;
import com.cyyaw.admin.entity.module.user.AuAdminRole;
import com.cyyaw.admin.entity.module.user.AuRole;
import com.cyyaw.admin.entity.utils.LoginInfo;
import com.cyyaw.admin.entity.utils.LoginInfoContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuAdminServiceImpl implements AuAdminService {

    /**
     * 重置密码的默认值
     */
    private static final String DEFAULT_PASSWORD = "123456";

    @Autowired
    private AuAdminDao auAdminDao;

    @Autowired
    private AuAdminRoleDao auAdminRoleDao;

    @Autowired
    private AuRoleDao auRoleDao;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Override
    public AuAdmin findAdminByAccountAndEnId(String account, Long enId) {
        return auAdminDao.findAdminByAccountAndEnId(account, enId);
    }

    @Override
    public AuAdmin saveAdmin(AuAdmin auAdmin) {
        return auAdminDao.save(auAdmin);
    }

    @Override
    public AuAdmin findByAdminId(Long adminId) {
        return auAdminDao.selectById(adminId);
    }

    @Override
    public Page<AuAdmin> findPage(Integer page, Integer size, QueryWrapper<AuAdmin> wrapper) {
        wrapper.orderByDesc("create_time");
        Page<AuAdmin> pageResult = auAdminDao.selectPage(new Page<>(page, size), wrapper);
        // 回填角色名称与角色ID集合（列表展示）
        fillRoles(pageResult.getRecords());
        return pageResult;
    }

    @Override
    public AuAdmin findById(Long id) {
        AuAdmin admin = auAdminDao.selectById(id);
        if (admin != null) {
            List<AuAdmin> list = new ArrayList<>();
            list.add(admin);
            fillRoles(list);
        }
        return admin;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuAdmin add(AuAdmin admin, List<String> roleIds) {
        LoginInfo loginInfo = LoginInfoContext.getLoginInfo();
        Long enId = loginInfo != null ? loginInfo.getEnId() : null;
        if (enId == null) {
            WebException.fail("无法获取当前企业信息");
        }
        // 账号唯一性校验（同企业内账号不可重复）
        AuAdmin exist = auAdminDao.findAdminByAccountAndEnId(admin.getAccount(), enId);
        if (exist != null) {
            WebException.fail("账号已存在");
        }
        if (StrUtil.isBlank(admin.getPassword())) {
            admin.setPassword(DEFAULT_PASSWORD);
        }
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        admin.setEnId(enId);
        if (admin.getStatus() == null) {
            admin.setStatus(1);
        }
        AuAdmin saved = auAdminDao.save(admin);
        // 关联角色（多对多）
        if (saved != null && saved.getId() != null) {
            saveRoleLinks(saved.getId(), roleIds);
        }
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuAdmin edit(AuAdmin admin, List<String> roleIds) {
        if (admin.getId() == null) {
            WebException.fail("管理员ID不能为空");
        }
        AuAdmin origin = auAdminDao.selectById(admin.getId());
        if (origin == null) {
            WebException.fail("管理员不存在");
        }
        // 密码不在编辑范围（保留原值）
        admin.setPassword(null);
        admin.setEnId(origin.getEnId());
        AuAdmin updated = auAdminDao.save(admin);
        // 同步角色关联（清旧链按新集合重写）
        auAdminRoleDao.delete(new QueryWrapper<AuAdminRole>().eq("admin_id", admin.getId()));
        saveRoleLinks(admin.getId(), roleIds);
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 先清角色关联，再物理删除
        auAdminRoleDao.delete(new QueryWrapper<AuAdminRole>().eq("admin_id", id));
        auAdminDao.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id) {
        AuAdmin admin = auAdminDao.selectById(id);
        if (admin == null) {
            WebException.fail("管理员不存在");
        }
        admin.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        auAdminDao.save(admin);
    }

    @Override
    public List<String> findRoleIdsByAdminId(Long adminId) {
        List<AuAdminRole> links = auAdminRoleDao.selectList(
                new QueryWrapper<AuAdminRole>().eq("admin_id", adminId)
        );
        return links.stream().map(l -> String.valueOf(l.getRoleId())).collect(Collectors.toList());
    }

    /**
     * 写入管理员-角色关联（多对多，逐条插入，重复对由唯一键约束兜底）。
     */
    private void saveRoleLinks(Long adminId, List<String> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return;
        }
        List<AuAdminRole> links = new ArrayList<>();
        for (String rid : roleIds) {
            if (StrUtil.isBlank(rid)) {
                continue;
            }
            AuAdminRole link = new AuAdminRole();
            link.setAdminId(adminId);
            link.setRoleId(Long.parseLong(rid));
            links.add(link);
        }
        if (!links.isEmpty()) {
            auAdminRoleDao.insertBatch(links);
        }
    }

    /**
     * 批量回填角色ID集合与角色名称（多角色逗号拼接）：先收集各管理员的关联行，
     * 一次性查询角色后映射回填，避免 N+1 查询。
     */
    private void fillRoles(List<AuAdmin> admins) {
        if (CollUtil.isEmpty(admins)) {
            return;
        }
        List<Long> adminIds = admins.stream().map(AuAdmin::getId).collect(Collectors.toList());
        List<AuAdminRole> links = auAdminRoleDao.selectList(
                new QueryWrapper<AuAdminRole>().in("admin_id", adminIds)
        );
        if (links.isEmpty()) {
            for (AuAdmin admin : admins) {
                admin.setRoleIds(Collections.emptyList());
            }
            return;
        }
        List<Long> roleIds = links.stream().map(AuAdminRole::getRoleId).distinct().collect(Collectors.toList());
        List<AuRole> roles = auRoleDao.selectBatchIds(roleIds);
        Map<Long, String> roleIdToName = roles.stream()
                .collect(Collectors.toMap(AuRole::getId, AuRole::getName, (a, b) -> a));

        // adminId -> 角色ID集合 + 角色名拼接
        Map<Long, List<AuAdminRole>> linksByAdmin = links.stream()
                .collect(Collectors.groupingBy(AuAdminRole::getAdminId));
        for (AuAdmin admin : admins) {
            List<AuAdminRole> adminLinks = linksByAdmin.get(admin.getId());
            if (CollUtil.isEmpty(adminLinks)) {
                admin.setRoleIds(Collections.emptyList());
                continue;
            }
            List<String> ridStrs = adminLinks.stream()
                    .map(l -> String.valueOf(l.getRoleId()))
                    .collect(Collectors.toList());
            admin.setRoleIds(ridStrs);
            String names = adminLinks.stream()
                    .map(l -> roleIdToName.get(l.getRoleId()))
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.joining(","));
            admin.setRoleName(names);
        }
    }

}
