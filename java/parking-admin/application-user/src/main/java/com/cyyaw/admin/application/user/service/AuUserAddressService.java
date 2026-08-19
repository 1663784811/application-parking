package com.cyyaw.admin.application.user.service;


import com.cyyaw.admin.dao.service.BaseTableService;
import com.cyyaw.admin.entity.module.user.AuUserAddress;

public interface AuUserAddressService extends BaseTableService<AuUserAddress> {

    AuUserAddress saveAddress(AuUserAddress auUserAddress);

}