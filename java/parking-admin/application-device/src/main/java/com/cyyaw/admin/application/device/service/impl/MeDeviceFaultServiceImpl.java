package com.cyyaw.admin.application.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.application.device.service.MeDeviceFaultService;
import com.cyyaw.admin.dao.device.MeDeviceDao;
import com.cyyaw.admin.dao.device.MeDeviceFaultDao;
import com.cyyaw.admin.dao.parking.PkParkingDao;
import com.cyyaw.admin.entity.module.device.MeDevice;
import com.cyyaw.admin.entity.module.device.MeDeviceFault;
import com.cyyaw.admin.entity.module.parking.PkParking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class MeDeviceFaultServiceImpl implements MeDeviceFaultService {

    @Autowired
    private MeDeviceFaultDao meDeviceFaultDao;

    @Autowired
    private MeDeviceDao meDeviceDao;

    @Autowired
    private PkParkingDao pkParkingDao;

    @Override
    public MeDeviceFault findById(Long id) {
        return meDeviceFaultDao.selectById(id);
    }

    @Override
    public Page<MeDeviceFault> findPage(Integer page, Integer size, QueryWrapper<MeDeviceFault> wrapper) {
        return meDeviceFaultDao.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public MeDeviceFault create(Long deviceId, String faultType) {
        MeDevice device = meDeviceDao.selectById(deviceId);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }
        MeDeviceFault fault = new MeDeviceFault();
        fault.setOrderNo("F" + System.currentTimeMillis());
        fault.setDeviceId(deviceId);
        // 快照设备信息，便于工单独立展示
        fault.setDeviceName(device.getName());
        fault.setDeviceType(device.getType());
        fault.setParkingId(device.getParkingId());
        PkParking parking = device.getParkingId() == null ? null : pkParkingDao.selectById(device.getParkingId());
        fault.setParkingName(parking == null ? null : parking.getName());
        fault.setFaultType(faultType);
        fault.setReportTime(LocalDateTime.now());
        fault.setStatus("pending");
        return meDeviceFaultDao.save(fault);
    }

    @Override
    public MeDeviceFault handle(Long id, String action, String repairer, LocalDateTime expectCompleteTime) {
        MeDeviceFault fault = meDeviceFaultDao.selectById(id);
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
        return meDeviceFaultDao.save(fault);
    }

    @Override
    public Map<String, Object> stats() {
        Map<String, Object> s = new HashMap<>();
        s.put("pending", meDeviceFaultDao.selectCount(new QueryWrapper<MeDeviceFault>().eq("status", "pending")));
        s.put("processing", meDeviceFaultDao.selectCount(new QueryWrapper<MeDeviceFault>().eq("status", "processing")));
        s.put("completed", meDeviceFaultDao.selectCount(new QueryWrapper<MeDeviceFault>().eq("status", "completed")));
        return s;
    }

}
