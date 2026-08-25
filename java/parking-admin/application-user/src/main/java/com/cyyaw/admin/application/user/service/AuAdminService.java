package com.cyyaw.admin.application.user.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.entity.module.user.AuAdmin;

import java.util.List;

public interface AuAdminService {

    AuAdmin findAdminByAccountAndEnId(String account, Long enId);

    AuAdmin saveAdmin(AuAdmin auAdmin);

    AuAdmin findByAdminId(Long adminId);

    /**
     * 分页查询管理员（条件由调用方构造 QueryWrapper 传入）。
     */
    Page<AuAdmin> findPage(Integer page, Integer size, QueryWrapper<AuAdmin> wrapper);

    /**
     * 根据ID查询管理员（不含密码，回填角色ID集合）。
     */
    AuAdmin findById(Long id);

    /**
     * 新增管理员：写入账号信息 + 默认密码 + 关联角色（多对多）。
     *
     * @param admin   账号信息（account/realName/phone/status），enId 由上下文自动填充
     * @param roleIds 关联角色ID集合（字符串形式雪花ID），可为空表示暂不分配角色
     */
    AuAdmin add(AuAdmin admin, List<String> roleIds);

    /**
     * 编辑管理员：更新账号信息 + 同步角色关联（多对多，清旧链按新集合重写）。
     * 密码字段不在更新范围（保留原值）。
     *
     * @param admin   账号信息（id 必填）
     * @param roleIds 关联角色ID集合（字符串形式雪花ID），可为空表示清除全部角色
     */
    AuAdmin edit(AuAdmin admin, List<String> roleIds);

    /**
     * 删除管理员：先清角色关联，再物理删除。
     */
    void delete(Long id);

    /**
     * 重置密码为默认值（123456）。
     */
    void resetPassword(Long id);

    /**
     * 查询某管理员已关联的角色ID集合（字符串形式雪花ID，供前端回显）。
     */
    List<String> findRoleIdsByAdminId(Long adminId);

}
