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
    public PkCarLog selectByParkingIdAndCarNumber(Long parkingId, String carNumber) {
        return pkCarLogDao.selectByParkingIdAndCarNumber(parkingId, carNumber);
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