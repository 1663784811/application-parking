package com.cyyaw.admin.application.member.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.entity.module.member.MeMember;

import java.util.Map;

public interface MeMemberService {

    MeMember findById(Long id);

    MeMember save(MeMember member);

    void delete(Long id);

    Page<MeMember> findPage(Integer page, Integer size, QueryWrapper<MeMember> wrapper);

    MeMember freeze(Long id, boolean frozen);

    void sendExpireNotice(Long id);

    Map<String, Object> stats();

}
