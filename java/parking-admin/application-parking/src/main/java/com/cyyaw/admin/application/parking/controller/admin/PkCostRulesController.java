package com.cyyaw.admin.application.parking.controller.admin;

import com.cyyaw.admin.application.parking.service.PkCostRulesService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.module.parking.PkCostRules;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "停车场计费规则")
@RestController
@RequestMapping("/admin/parking/costRules")
public class PkCostRulesController {

    @Autowired
    private PkCostRulesService pkCostRulesService;

    @Operation(summary = "查询计费规则", description = "根据ID查询计费规则")
    @GetMapping("/find/{id}")
    public BaseResult<PkCostRules> findById(@PathVariable Long id) {
        PkCostRules rules = pkCostRulesService.findById(id);
        return BaseResult.ok(rules);
    }

    @Operation(summary = "计费规则列表", description = "根据停车场ID查询计费规则")
    @GetMapping("/list/{parkingId}")
    public BaseResult<List<PkCostRules>> list(@PathVariable Long parkingId) {
        List<PkCostRules> list = pkCostRulesService.findByParkingId(parkingId);
        return BaseResult.ok(list);
    }

    @Operation(summary = "保存计费规则", description = "新增或更新计费规则")
    @PostMapping("/save")
    public BaseResult<PkCostRules> save(@RequestBody PkCostRules rules) {
        PkCostRules result = pkCostRulesService.save(rules);
        return BaseResult.ok(result);
    }

    @Operation(summary = "删除计费规则", description = "根据ID删除计费规则")
    @DeleteMapping("/delete/{id}")
    public BaseResult<Void> delete(@PathVariable Long id) {
        pkCostRulesService.delete(id);
        return BaseResult.ok();
    }

}