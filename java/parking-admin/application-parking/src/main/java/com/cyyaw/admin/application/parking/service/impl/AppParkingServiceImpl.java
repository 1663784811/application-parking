package com.cyyaw.admin.application.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cyyaw.admin.application.parking.service.AppParkingService;
import com.cyyaw.admin.application.parking.service.PkCarLogService;
import com.cyyaw.admin.common.GeographicUtil;
import com.cyyaw.admin.dao.parking.PkParkingDao;
import com.cyyaw.admin.entity.dto.parking.AppParkingVO;
import com.cyyaw.admin.entity.module.parking.PkParking;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * H5 首页「附近停车场」实现。只读，不写库。
 * <p>
 * 剩余车位复用 {@link PkCarLogService#countInLot}（status=0 且未逻辑删除），
 * 与在场看板同一口径。
 */
@Slf4j
@Service
public class AppParkingServiceImpl implements AppParkingService {

    /** 是否对外开放：0 对外开放 */
    private static final int OPENING_UP = 0;

    @Autowired
    private PkParkingDao pkParkingDao;

    @Autowired
    private PkCarLogService pkCarLogService;

    @Override
    public List<AppParkingVO> findList(Long appId, Double lng, Double lat) {
        QueryWrapper<PkParking> wrapper = new QueryWrapper<>();
        wrapper.eq("opening_up", OPENING_UP);
        if (appId != null) {
            wrapper.eq("app_id", appId);
        }
        wrapper.orderByDesc("create_time");
        List<PkParking> parkings = pkParkingDao.selectList(wrapper);

        // 是否具备算距离的条件：请求带了坐标，且至少有一个停车场坐标可用
        boolean withDistance = lng != null && lat != null;

        List<AppParkingVO> list = new ArrayList<>(parkings.size());
        for (PkParking parking : parkings) {
            AppParkingVO vo = new AppParkingVO();
            vo.setId(parking.getId());
            vo.setName(parking.getName());
            vo.setAddress(parking.getAddress());
            vo.setImage(parking.getImage());

            int capacity = parking.getCapacity() == null ? 0 : parking.getCapacity();
            vo.setCapacity(capacity);
            long inLot = pkCarLogService.countInLot(parking.getId());
            // 在场数理论上不会超过容量，但数据脏了也可能超，兜个 0 免得前端显示负数
            vo.setRemainSpaces((int) Math.max(0L, capacity - inLot));

            if (withDistance) {
                double[] point = parseLongLat(parking.getLongLat());
                if (point != null) {
                    double meters = GeographicUtil.getDistance(lng, lat, point[0], point[1]);
                    vo.setDistanceMeters(BigDecimal.valueOf(meters).setScale(1, RoundingMode.HALF_UP));
                    vo.setDistance(formatDistance(meters));
                }
            }
            list.add(vo);
        }

        if (withDistance) {
            // 有距离的排前面并按距离升序；坐标不可用的沉到末尾（nulls last）
            list.sort(Comparator.comparing(AppParkingVO::getDistanceMeters,
                    Comparator.nullsLast(Comparator.naturalOrder())));
        }
        return list;
    }

    /**
     * 解析停车场经纬度。
     * <p>
     * 约定 {@code long_lat = "经度,纬度"}（如 {@code "113.38,23.02"}）。
     * 该列是历史遗留的自由文本，既无格式约束也无有效值校验，库里现有数据甚至有占位值
     * （如 {@code "23,12"}）。因此这里只认「两个能解析的数值 + 落在合法经纬度范围内」，
     * 其余一律当作<b>无坐标</b>返回 null —— 不能缺省成 (0,0)，
     * 否则占位值会被当成几内亚湾附近的真实坐标，反而排到列表最前面。
     *
     * @return {@code [经度, 纬度]}；不可用时返回 null
     */
    private double[] parseLongLat(String longLat) {
        if (longLat == null) {
            return null;
        }
        String[] parts = longLat.split(",");
        if (parts.length != 2) {
            return null;
        }
        double lng;
        double lat;
        try {
            lng = Double.parseDouble(parts[0].trim());
            lat = Double.parseDouble(parts[1].trim());
        } catch (NumberFormatException e) {
            log.debug("停车场经纬度无法解析，按无坐标处理: {}", longLat);
            return null;
        }
        if (!isValidLngLat(lng, lat)) {
            log.debug("停车场经纬度超出合法范围，按无坐标处理: {}", longLat);
            return null;
        }
        return new double[]{lng, lat};
    }

    /** 经度 ∈ [-180,180]，纬度 ∈ [-90,90]，且不同时为 0（(0,0) 几乎必然是没填而不是真在海上） */
    private boolean isValidLngLat(double lng, double lat) {
        if (lng < -180 || lng > 180 || lat < -90 || lat > 90) {
            return false;
        }
        return lng != 0 || lat != 0;
    }

    /** 展示用距离：1km 以内给整数米，超出给一位小数的公里 */
    private String formatDistance(double meters) {
        if (meters < 1000) {
            return Math.round(meters) + "m";
        }
        return BigDecimal.valueOf(meters / 1000).setScale(1, RoundingMode.HALF_UP) + "km";
    }

}