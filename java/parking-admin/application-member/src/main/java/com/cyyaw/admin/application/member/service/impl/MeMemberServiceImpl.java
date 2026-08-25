package com.cyyaw.admin.application.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.application.member.service.MeMemberService;
import com.cyyaw.admin.dao.member.MeMemberDao;
import com.cyyaw.admin.entity.module.member.MeMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class MeMemberServiceImpl implements MeMemberService {

    @Autowired
    private MeMemberDao meMemberDao;

    @Override
    public MeMember findById(Long id) {
        return meMemberDao.selectById(id);
    }

    @Override
    public MeMember save(MeMember member) {
        return meMemberDao.save(member);
    }

    @Override
    public void delete(Long id) {
        meMemberDao.deleteById(id);
    }

    @Override
    public Page<MeMember> findPage(Integer page, Integer size, QueryWrapper<MeMember> wrapper) {
        return meMemberDao.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public MeMember freeze(Long id, boolean frozen) {
        MeMember member = meMemberDao.selectById(id);
        if (member == null) {
            return null;
        }
        member.setFrozen(frozen ? 1 : 0);
        return meMemberDao.save(member);
    }

    @Override
    public void sendExpireNotice(Long id) {
        // 到期提醒：暂为占位实现，真实短信通知待对接短信网关
    }

    @Override
    public Map<String, Object> stats() {
        Map<String, Object> s = new HashMap<>();
        s.put("total", meMemberDao.selectCount(null));
        s.put("monthly", meMemberDao.selectCount(new QueryWrapper<MeMember>().eq("card_type", 1)));
        s.put("quarterly", meMemberDao.selectCount(new QueryWrapper<MeMember>().eq("card_type", 2)));
        s.put("yearly", meMemberDao.selectCount(new QueryWrapper<MeMember>().eq("card_type", 3)));
        LocalDateTime now = LocalDateTime.now();
        s.put("expiring", meMemberDao.selectCount(
                new QueryWrapper<MeMember>()
                        .eq("frozen", 0)
                        .ge("expire_time", now)
                        .le("expire_time", now.plusDays(7))));
        return s;
    }

}
