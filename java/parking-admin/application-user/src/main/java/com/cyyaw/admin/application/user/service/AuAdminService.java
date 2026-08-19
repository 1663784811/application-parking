package com.cyyaw.admin.application.user.service;


import com.cyyaw.admin.entity.module.user.AuAdmin;

public interface AuAdminService {

    AuAdmin findAdminByAccountAndEnId(String account, Long enId);

    AuAdmin saveAdmin(AuAdmin auAdmin);

    AuAdmin findByAdminId(Long adminId);

}
