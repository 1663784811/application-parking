package com.cyyaw.admin.application.user.service;

import com.cyyaw.admin.entity.module.user.AuRole;

import java.util.List;

public interface AuRoleService {

    /**
     * 登录辅助：查询管理员所属角色编码（逗号拼接）
     */
    String findAdminRoleByAdminId(Long adminId);

    /**
     * 全部角色（按创建时间倒序，含各角色成员数）
     */
    List<AuRole> findAll();

    /**
     * 根据ID查询角色
     */
    AuRole findById(Long id);

    /**
     * 新增或更新角色（upsert）
     */
    AuRole save(AuRole role);

    /**
     * 根据ID删除角色
     */
    void delete(Long id);
}
