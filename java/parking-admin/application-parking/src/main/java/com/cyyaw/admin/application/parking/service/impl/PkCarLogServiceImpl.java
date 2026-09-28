package com.cyyaw.admin.application.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.application.parking.service.PkCarLogService;
import com.cyyaw.admin.dao.parking.PkCarLogDao;
import com.cyyaw.admin.entity.module.parking.PkCarLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PkCarLogServiceImpl implements PkCarLogService {

    @Autowired
    private PkCarLogDao pkCarLogDao;

    @Override
    public PkCarLog findById(Long id) {
        return pkCarLogDao.selectById(id);
    }

    @Override
    public PkCarLog save(PkCarLog carLog) {
        return pkCarLogDao.save(carLog);
    }

    @Override
    public int selectStatusCount(Long parkingId, int status) {
        return pkCarLogDao.selectStatusCount(parkingId, status);
    }

    @Override
    public long countInLot(Long parkingId) {
        if (parkingId == null) {
            return 0L;
        }
        QueryWrapper<PkCarLog> wrapper = new QueryWrapper<>();
        wrapper.eq("parking_id", parkingId);
        wrapper.eq("status", 0);
        // 与看板同口径：排除逻辑删除的记录，否则"幽灵车"会拉低剩余车位
        wrapper.eq("del_time", 0);
        Long total = pkCarLogDao.selectCount(wrapper);
        return total == null ? 0L : total;
    }

    @Override
    public PkCarLog selectByParkingIdAndCarNumber(Long parkingId, String carNumber) {
        return pkCarLogDao.selectByParkingIdAndCarNumber(parkingId, carNumber);
    }

    @Override
    public int selectTodayCount() {
        return pkCarLogDao.selectTodayCount();
    }

    @Override
    public List<PkCarLog> findByParkingId(Long parkingId) {
        return pkCarLogDao.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PkCarLog>()
                        .eq("parking_id", parkingId)
                        .orderByDesc("entry_time")
        );
    }

    @Override
    public Page<PkCarLog> findPage(Integer page, Integer size, QueryWrapper<PkCarLog> wrapper) {
        return pkCarLogDao.selectPage(new Page<>(page, size), wrapper);
    }

}