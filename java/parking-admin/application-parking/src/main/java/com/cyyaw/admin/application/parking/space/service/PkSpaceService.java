package com.cyyaw.admin.application.parking.space.service;

import com.cyyaw.admin.entity.module.parking.PkSpace;

import java.util.List;
import java.util.Map;

/**
 * 车位管理服务（基于 pk_space：平面图视图，支持分配/解绑/报修）
 */
public interface PkSpaceService {

    /** 车位详情 */
    PkSpace findById(Long id);

    /** 车位列表：按停车场/状态/类型/编号筛选，返回全部匹配车位（平面图） */
    List<PkSpace> list(Long parkingId, Integer status, Integer type, String keyword);

    /** 车位状态统计：{ free, fixed, temp, fault } */
    Map<String, Object> stats(Long parkingId);

    /** 新增或更新车位 */
    PkSpace save(PkSpace space);

    /** 分配车位：绑定会员/车牌/有效期，状态置为占用；返回更新后的车位 */
    PkSpace assign(PkSpace input);

    /** 解绑车位：清除绑定，状态置为空闲 */
    void unbind(Long id);

    /** 车位报修：状态置为故障 */
    void repair(Long id);

    /** 删除车位 */
    void delete(Long id);

}
