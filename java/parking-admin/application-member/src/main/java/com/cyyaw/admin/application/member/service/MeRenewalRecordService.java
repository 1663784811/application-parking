package com.cyyaw.admin.application.member.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.entity.module.member.MeRenewalRecord;

public interface MeRenewalRecordService {

    MeRenewalRecord findById(Long id);

    Page<MeRenewalRecord> findPage(Integer page, Integer size, QueryWrapper<MeRenewalRecord> wrapper);

    MeRenewalRecord renewal(Long memberId);

}
