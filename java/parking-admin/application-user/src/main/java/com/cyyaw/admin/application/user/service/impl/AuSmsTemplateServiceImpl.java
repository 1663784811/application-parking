package com.cyyaw.admin.application.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cyyaw.admin.application.user.service.AuSmsTemplateService;
import com.cyyaw.admin.dao.user.AuSmsTemplateDao;
import com.cyyaw.admin.entity.module.user.AuSmsTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuSmsTemplateServiceImpl implements AuSmsTemplateService {

    @Autowired
    private AuSmsTemplateDao auSmsTemplateDao;

    @Override
    public AuSmsTemplate findById(Long id) {
        return auSmsTemplateDao.selectById(id);
    }

    @Override
    public AuSmsTemplate save(AuSmsTemplate smsTemplate) {
        return auSmsTemplateDao.save(smsTemplate);
    }

    @Override
    public void delete(Long id) {
        auSmsTemplateDao.deleteById(id);
    }

    @Override
    public List<AuSmsTemplate> findAll() {
        return auSmsTemplateDao.selectList(new QueryWrapper<AuSmsTemplate>().orderByDesc("create_time"));
    }
}
