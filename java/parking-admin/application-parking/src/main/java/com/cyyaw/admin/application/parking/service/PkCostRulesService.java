package com.cyyaw.admin.application.parking.service;

import com.cyyaw.admin.entity.module.parking.PkCostRules;

import java.util.List;

public interface PkCostRulesService {

    PkCostRules findById(Long id);

    PkCostRules save(PkCostRules rules);

    void delete(Long id);

    List<PkCostRules> findByParkingId(Long parkingId);

}