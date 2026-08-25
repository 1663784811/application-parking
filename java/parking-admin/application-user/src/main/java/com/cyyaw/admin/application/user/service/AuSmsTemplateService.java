package com.cyyaw.admin.application.user.service;

import com.cyyaw.admin.entity.module.user.AuSmsTemplate;

import java.util.List;

public interface AuSmsTemplateService {

    AuSmsTemplate findById(Long id);

    AuSmsTemplate save(AuSmsTemplate smsTemplate);

    void delete(Long id);

    List<AuSmsTemplate> findAll();
}
