package com.cyyaw.admin.application.user.service;


import com.cyyaw.admin.entity.module.user.AuStoreAdmin;

public interface AuStoreAdminService {

    AuStoreAdmin findByAccountAndStoreId(String account, Long storeId);

    AuStoreAdmin findById(Long id);

}
