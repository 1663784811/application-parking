package com.cyyaw.admin.application.parking.service.impl;

import com.cyyaw.admin.application.parking.service.PkCostRulesService;
import com.cyyaw.admin.dao.parking.PkCostRulesDao;
import com.cyyaw.admin.entity.module.parking.PkCostRules;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PkCostRulesServiceImpl implements PkCostRulesService {

    @Autowired
    private PkCostRulesDao pkCostRulesDao;

    @Override
    public PkCostRules findById(Long id) {
        return pkCostRulesDao.selectById(id);
    }

    @Override
    public PkCostRules save(PkCostRules rules) {
        return pkCostRulesDao.save(rules);
    }

    @Override
    public void delete(Long id) {
        pkCostRulesDao.deleteById(id);
    }

    @Override
    public List<PkCostRules> findByParkingId(Long parkingId) {
        return pkCostRulesDao.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PkCostRules>()
                        .eq("parking_id", parkingId)
        );
    }

}