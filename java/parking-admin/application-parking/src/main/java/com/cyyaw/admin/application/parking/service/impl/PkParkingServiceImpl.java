package com.cyyaw.admin.application.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.application.parking.service.PkParkingService;
import com.cyyaw.admin.dao.parking.PkParkingDao;
import com.cyyaw.admin.entity.module.parking.PkParking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PkParkingServiceImpl implements PkParkingService {

    @Autowired
    private PkParkingDao pkParkingDao;

    @Override
    public PkParking findById(Long id) {
        return pkParkingDao.selectById(id);
    }

    @Override
    public PkParking save(PkParking parking) {
        return pkParkingDao.save(parking);
    }

    @Override
    public void delete(Long id) {
        pkParkingDao.deleteById(id);
    }

    @Override
    public List<PkParking> findAll() {
        return pkParkingDao.selectList();
    }

    @Override
    public Page<PkParking> findPage(Integer page, Integer size, QueryWrapper<PkParking> wrapper) {
        return pkParkingDao.selectPage(new Page<>(page, size), wrapper);
    }

}