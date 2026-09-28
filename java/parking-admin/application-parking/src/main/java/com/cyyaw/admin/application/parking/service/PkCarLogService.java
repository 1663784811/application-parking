package com.cyyaw.admin.application.parking.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.entity.module.parking.PkCarLog;

import java.util.List;

public interface PkCarLogService {

    PkCarLog findById(Long id);

    PkCarLog save(PkCarLog carLog);

    int selectStatusCount(Long parkingId, int status);

    /**
     * 在场车辆数：{@code status=0}（场内）且未逻辑删除。
     * <p>
     * 这是「在场」的唯一口径 —— {@link #selectStatusCount} 未过滤 {@code del_time}，
     * 会把已删除的「幽灵车」算进占用率，别用它统计在场。
     *
     * @return 在场车辆数；parkingId 为空时返回 0
     */
    long countInLot(Long parkingId);

    PkCarLog selectByParkingIdAndCarNumber(Long parkingId, String carNumber);

    int selectTodayCount();

    List<PkCarLog> findByParkingId(Long parkingId);

    Page<PkCarLog> findPage(Integer page, Integer size, QueryWrapper<PkCarLog> wrapper);

}