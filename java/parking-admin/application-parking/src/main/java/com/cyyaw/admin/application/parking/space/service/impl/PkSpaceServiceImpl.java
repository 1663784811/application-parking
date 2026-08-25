package com.cyyaw.admin.application.parking.space.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cyyaw.admin.application.parking.space.service.PkSpaceService;
import com.cyyaw.admin.dao.parking.PkSpaceDao;
import com.cyyaw.admin.entity.module.parking.PkSpace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PkSpaceServiceImpl implements PkSpaceService {

    @Autowired
    private PkSpaceDao pkSpaceDao;

    @Override
    public PkSpace findById(Long id) {
        return pkSpaceDao.selectById(id);
    }

    @Override
    public List<PkSpace> list(Long parkingId, Integer status, Integer type, String keyword) {
        QueryWrapper<PkSpace> wrapper = new QueryWrapper<>();
        if (parkingId != null) {
            wrapper.eq("parking_id", parkingId);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        // 前端 type 参数映射到 space_type 列（避免 type 列名歧义）
        if (type != null) {
            wrapper.eq("space_type", type);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like("space_no", keyword.trim());
        }
        wrapper.orderByAsc("area", "space_no");
        return pkSpaceDao.selectList(wrapper);
    }

    @Override
    public Map<String, Object> stats(Long parkingId) {
        return pkSpaceDao.selectStats(parkingId);
    }

    @Override
    public PkSpace save(PkSpace space) {
        return pkSpaceDao.save(space);
    }

    @Override
    public PkSpace assign(PkSpace input) {
        pkSpaceDao.assign(input.getId(), input.getMemberId(), input.getMemberName(), input.getPlate(), input.getExpireDate());
        return pkSpaceDao.selectById(input.getId());
    }

    @Override
    public void unbind(Long id) {
        pkSpaceDao.unbind(id);
    }

    @Override
    public void repair(Long id) {
        pkSpaceDao.repair(id);
    }

    @Override
    public void delete(Long id) {
        pkSpaceDao.deleteById(id);
    }

}
