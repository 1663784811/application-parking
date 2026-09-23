package com.cyyaw.admin.application.parking.service;

import com.cyyaw.admin.entity.dto.parking.InLotBoardVO;

/**
 * 在场车辆看板（只读）。
 */
public interface InLotBoardService {

    /**
     * 加载某停车场的在场车辆看板。
     *
     * @param parkingId 停车场ID，为空返回空看板
     * @param carNumber 车牌号，按模糊匹配筛选，可为空
     */
    InLotBoardVO loadInLotBoard(Long parkingId, String carNumber);
}
