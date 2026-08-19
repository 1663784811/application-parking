package com.cyyaw.admin.application.user.service.impl;

import com.cyyaw.admin.application.user.service.AuUserAddressService;
import com.cyyaw.admin.dao.service.BaseService;
import com.cyyaw.admin.dao.user.AuUserAddressDao;
import com.cyyaw.admin.entity.module.user.AuUserAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuUserAddressServiceImpl extends BaseService<AuUserAddressDao, AuUserAddress> implements AuUserAddressService {

    @Autowired
    private AuUserAddressDao auUserAddressDao;

    @Override
    public AuUserAddress saveAddress(AuUserAddress auUserAddress) {
        AuUserAddress save = auUserAddressDao.save(auUserAddress);
        // 查默认地址
        Long userId = save.getUserId();
        List<AuUserAddress> auUserAddresses = auUserAddressDao.selectDefAddressByUserId(userId);
        if (!auUserAddresses.isEmpty() && auUserAddresses.size() > 1) {
            for (AuUserAddress addressObj : auUserAddresses) {
                if (!addressObj.getId().equals(save.getId())) {
                    addressObj.setDef(0);
                    auUserAddressDao.save(addressObj);
                }
            }
        } else if (auUserAddresses.isEmpty()) {
            save.setDef(1);
            auUserAddressDao.save(save);
        }
        return save;
    }
}