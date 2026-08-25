package com.cyyaw.admin.application.parking.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.entity.module.parking.PkCarLog;

import java.util.List;

public interface PkCarLogService {

    PkCarLog findById(Long id);

    PkCarLog save(PkCarLog carLog);

    int selectStatusCount(Long parkingId, int status);

    PkCarLog selectByParkingIdAndCarNumber(Long parkingId, String carNumber);

    int selectTodayCount();

    List<PkCarLog> findByParkingId(Long parkingId);

    Page<PkCarLog> findPage(Integer page, Integer size, QueryWrapper<PkCarLog> wrapper);

}