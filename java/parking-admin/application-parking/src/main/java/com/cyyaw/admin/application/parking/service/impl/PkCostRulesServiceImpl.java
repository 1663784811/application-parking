package com.cyyaw.admin.application.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.application.parking.service.PkCostRulesService;
import com.cyyaw.admin.dao.parking.PkCostRulesDao;
import com.cyyaw.admin.dao.parking.PkParkingCostRulesDao;
import com.cyyaw.admin.entity.module.parking.PkCostRules;
import com.cyyaw.admin.entity.module.parking.PkParkingCostRules;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PkCostRulesServiceImpl implements PkCostRulesService {

    @Autowired
    private PkCostRulesDao pkCostRulesDao;

    @Autowired
    private PkParkingCostRulesDao pkParkingCostRulesDao;

    @Override
    public PkCostRules findById(Long id) {
        return pkCostRulesDao.selectById(id);
    }

    @Override
    public PkCostRules save(PkCostRules rules) {
        // 仅 upsert 规则主表（新规则由 MP 回填 id）。
        // 适用停车场关联由 parkingList 的「设置收费规则」按停车场方向维护（saveByParking）。
        pkCostRulesDao.save(rules);
        return rules;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 先清关联，再删规则
        pkParkingCostRulesDao.delete(
                new QueryWrapper<PkParkingCostRules>().eq("cost_rules_id", id)
        );
        pkCostRulesDao.deleteById(id);
    }

    @Override
    public Page<PkCostRules> findPage(Integer page, Integer size, QueryWrapper<PkCostRules> wrapper) {
        // 列表轻量返回：仅规则主表字段
        return pkCostRulesDao.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public List<String> findRuleIdsByParkingId(Long parkingId) {
        List<PkParkingCostRules> links = pkParkingCostRulesDao.selectList(
                new QueryWrapper<PkParkingCostRules>()
                        .eq("parking_id", parkingId)
                        .eq("del_time", 0)
        );
        return links.stream().map(l -> String.valueOf(l.getCostRulesId())).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveByParking(Long parkingId, List<String> costRulesIds) {
        // 同步关联表（parking 方向）：先清旧链，再按 costRulesIds 插新链
        pkParkingCostRulesDao.delete(
                new QueryWrapper<PkParkingCostRules>().eq("parking_id", parkingId)
        );
        if (costRulesIds == null || costRulesIds.isEmpty()) {
            return;
        }
        List<PkParkingCostRules> links = new ArrayList<>();
        for (String rid : costRulesIds) {
            if (rid == null || rid.isEmpty()) {
                continue;
            }
            PkParkingCostRules link = new PkParkingCostRules();
            link.setParkingId(parkingId);
            link.setCostRulesId(Long.parseLong(rid));
            links.add(link);
        }
        if (!links.isEmpty()) {
            pkParkingCostRulesDao.insertBatch(links);
        }
    }

}
