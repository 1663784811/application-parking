package com.cyyaw.admin.application.iot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.application.iot.service.IotDeviceFaultService;
import com.cyyaw.admin.dao.iot.IotDeviceDao;
import com.cyyaw.admin.dao.iot.IotDeviceFaultDao;
import com.cyyaw.admin.dao.parking.PkParkingDao;
import com.cyyaw.admin.entity.module.iot.IotDevice;
import com.cyyaw.admin.entity.module.iot.IotDeviceFault;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class IotDeviceFaultServiceImpl implements IotDeviceFaultService {

    @Autowired
    private IotDeviceFaultDao iotDeviceFaultDao;

    @Autowired
    private IotDeviceDao iotDeviceDao;

    @Autowired
    private PkParkingDao pkParkingDao;

    @Override
    public IotDeviceFault findById(Long id) {
        return iotDeviceFaultDao.selectById(id);
    }

    @Override
    public Page<IotDeviceFault> findPage(Integer page, Integer size, QueryWrapper<IotDeviceFault> wrapper) {
        return iotDeviceFaultDao.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public IotDeviceFault create(Long deviceId, String faultType) {
        IotDevice device = iotDeviceDao.selectById(deviceId);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }
        IotDeviceFault fault = new IotDeviceFault();
        fault.setOrderNo("F" + System.currentTimeMillis());
        fault.setDeviceId(deviceId);
        // 快照设备信息，便于工单独立展示
        fault.setDeviceName(device.getName());
        fault.setDeviceType(device.getType());
//        fault.setParkingId(device.getParkingId());
//        PkParking parking = device.getParkingId() == null ? null : pkParkingDao.selectById(device.getParkingId());
//        fault.setParkingName(parking == null ? null : parking.getName());
        fault.setFaultType(faultType);
        fault.setReportTime(LocalDateTime.now());
        fault.setStatus("pending");
        return iotDeviceFaultDao.save(fault);
    }

    @Override
    public IotDeviceFault handle(Long id, String action, String repairer, LocalDateTime expectCompleteTime) {
        IotDeviceFault fault = iotDeviceFaultDao.selectById(id);
        if (fault == null) {
            throw new RuntimeException("故障工单不存在");
        }
        if ("assign".equals(action)) {
            fault.setRepairer(repairer);
            fault.setExpectCompleteTime(expectCompleteTime);
            fault.setStatus("processing");
        } else if ("complete".equals(action)) {
            fault.setCompleteTime(LocalDateTime.now());
            fault.setStatus("completed");
        } else {
            throw new RuntimeException("不支持的操作: " + action);
        }
        return iotDeviceFaultDao.save(fault);
    }

    @Override
    public Map<String, Object> stats() {
        Map<String, Object> s = new HashMap<>();
        s.put("pending", iotDeviceFaultDao.selectCount(new QueryWrapper<IotDeviceFault>().eq("status", "pending")));
        s.put("processing", iotDeviceFaultDao.selectCount(new QueryWrapper<IotDeviceFault>().eq("status", "processing")));
        s.put("completed", iotDeviceFaultDao.selectCount(new QueryWrapper<IotDeviceFault>().eq("status", "completed")));
        return s;
    }

}
