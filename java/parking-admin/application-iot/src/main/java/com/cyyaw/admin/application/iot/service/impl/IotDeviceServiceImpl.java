package com.cyyaw.admin.application.iot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.application.iot.service.IotDeviceService;
import com.cyyaw.admin.dao.iot.IotDeviceDao;
import com.cyyaw.admin.dao.iot.IotDeviceFaultDao;
import com.cyyaw.admin.entity.module.iot.IotDevice;
import com.cyyaw.admin.entity.module.iot.IotDeviceFault;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class IotDeviceServiceImpl implements IotDeviceService {

    @Autowired
    private IotDeviceDao iotDeviceDao;

    @Autowired
    private IotDeviceFaultDao iotDeviceFaultDao;

    @Override
    public IotDevice findById(Long id) {
        return iotDeviceDao.selectById(id);
    }

    @Override
    public IotDevice save(IotDevice device) {
        return iotDeviceDao.save(device);
    }

    @Override
    public void delete(Long id) {
        iotDeviceDao.deleteById(id);
    }

    @Override
    public Page<IotDevice> findPage(Integer page, Integer size, QueryWrapper<IotDevice> wrapper) {
        return iotDeviceDao.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public void restart(Long id) {
        // 远程重启占位：实际下发需对接设备网关，此处仅校验设备存在
        IotDevice device = iotDeviceDao.selectById(id);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }
    }

    @Override
    public void changePassword(Long id, String password) {
        IotDevice device = iotDeviceDao.selectById(id);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }
        // TODO: 实际应加密存储（如 BCrypt）；当前与 save 一致按原值写入
        device.setPassword(password);
        iotDeviceDao.save(device);
    }

    @Override
    public Map<String, Object> stats() {
        Map<String, Object> s = new HashMap<>();
        s.put("total", iotDeviceDao.selectCount(null));
        s.put("online", iotDeviceDao.selectCount(new QueryWrapper<IotDevice>().eq("online_status", 1)));
        s.put("offline", iotDeviceDao.selectCount(new QueryWrapper<IotDevice>().eq("online_status", 0)));
        // 故障数 = 未完成（非 completed）的故障工单数
        s.put("fault", iotDeviceFaultDao.selectCount(new QueryWrapper<IotDeviceFault>().ne("status", "completed")));
        return s;
    }

    @Override
    public IotDevice findByCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        // code 唯一，传 false 防止脏数据抛 TooManyResultsException
        return iotDeviceDao.selectOne(new QueryWrapper<IotDevice>().eq("code", code), false);
    }

}
