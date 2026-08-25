package com.cyyaw.admin.application.parking.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.entity.module.parking.PkCostRules;

import java.util.List;

public interface PkCostRulesService {

    PkCostRules findById(Long id);

    PkCostRules save(PkCostRules rules);

    void delete(Long id);

    /**
     * 分页查询计费规则（轻量返回，仅规则主表字段）。
     */
    Page<PkCostRules> findPage(Integer page, Integer size, QueryWrapper<PkCostRules> wrapper);

    /**
     * 按停车场查询已关联的计费规则ID集合（字符串形式，供 parkingList 编辑回显）。
     */
    List<String> findRuleIdsByParkingId(Long parkingId);

    /**
     * 按停车场同步计费规则关联：清旧链（parking_id）→按 costRulesIds 插新链。
     * 与 {@link #save(PkCostRules)}（按 cost_rules_id 同步）互为逆方向，二者写入同一张关联表。
     */
    void saveByParking(Long parkingId, List<String> costRulesIds);

}
