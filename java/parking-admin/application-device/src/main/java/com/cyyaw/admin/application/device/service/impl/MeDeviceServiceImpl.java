package com.cyyaw.admin.application.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.application.device.service.MeDeviceService;
import com.cyyaw.admin.dao.device.MeDeviceDao;
import com.cyyaw.admin.dao.device.MeDeviceFaultDao;
import com.cyyaw.admin.entity.module.device.MeDevice;
import com.cyyaw.admin.entity.module.device.MeDeviceFault;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MeDeviceServiceImpl implements MeDeviceService {

    @Autowired
    private MeDeviceDao meDeviceDao;

    @Autowired
    private MeDeviceFaultDao meDeviceFaultDao;

    @Override
    public MeDevice findById(Long id) {
        return meDeviceDao.selectById(id);
    }

    @Override
    public MeDevice save(MeDevice device) {
        return meDeviceDao.save(device);
    }

    @Override
    public void delete(Long id) {
        meDeviceDao.deleteById(id);
    }

    @Override
    public Page<MeDevice> findPage(Integer page, Integer size, QueryWrapper<MeDevice> wrapper) {
        return meDeviceDao.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public void restart(Long id) {
        // 远程重启占位：实际下发需对接设备网关，此处仅校验设备存在
        MeDevice device = meDeviceDao.selectById(id);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }
    }

    @Override
    public Map<String, Object> stats() {
        Map<String, Object> s = new HashMap<>();
        s.put("total", meDeviceDao.selectCount(null));
        s.put("online", meDeviceDao.selectCount(new QueryWrapper<MeDevice>().eq("online_status", 1)));
        s.put("offline", meDeviceDao.selectCount(new QueryWrapper<MeDevice>().eq("online_status", 0)));
        // 故障数 = 未完成（非 completed）的故障工单数
        s.put("fault", meDeviceFaultDao.selectCount(new QueryWrapper<MeDeviceFault>().ne("status", "completed")));
        return s;
    }

}
