package com.cyyaw.sigle.inf.impl;

import com.cyyaw.admin.dao.iot.IotDeviceDao;
import com.cyyaw.admin.entity.module.iot.IotDevice;
import com.cyyaw.admin.inf.InfIot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class InfIotSingle implements InfIot {

    @Autowired
    private IotDeviceDao iotDeviceDao;

    @Override
    public IotDevice findIotDeviceByCode(String deviceCode) {
        return iotDeviceDao.findByCode(deviceCode);
    }


}
