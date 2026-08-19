package com.cyyaw.admin.application.user.service;



import com.cyyaw.admin.entity.module.user.AuApp;

import java.util.List;

public interface AuAppService {


    AuApp findAppById(Long appId);

    AuApp saveApp(AuApp auApp);

    List<AuApp> findAppByEnIdAndType(Long enId, String type);

}
