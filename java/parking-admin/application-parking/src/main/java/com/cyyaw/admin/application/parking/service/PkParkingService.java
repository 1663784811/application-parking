package com.cyyaw.admin.application.parking.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.entity.module.parking.PkParking;

import java.util.List;

public interface PkParkingService {

    PkParking findById(Long id);

    PkParking save(PkParking parking);

    void delete(Long id);

    List<PkParking> findAll();

    Page<PkParking> findPage(Integer page, Integer size, QueryWrapper<PkParking> wrapper);

}