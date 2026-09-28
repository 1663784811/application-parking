package com.cyyaw.admin.application.parking.service;

import com.cyyaw.admin.entity.dto.parking.AppParkingVO;

import java.util.List;

/**
 * H5 首页「附近停车场」。
 */
public interface AppParkingService {

    /**
     * 查询对外开放的停车场列表。
     * <p>
     * 传了 lng/lat 时按距离升序并回填距离；未传（或该停车场坐标不可用）时距离为空，
     * 退回按创建时间倒序。
     *
     * @param appId 应用ID，为空表示不限
     * @param lng   当前位置经度，与 lat 需同时提供
     * @param lat   当前位置纬度，与 lng 需同时提供
     */
    List<AppParkingVO> findList(Long appId, Double lng, Double lat);

}