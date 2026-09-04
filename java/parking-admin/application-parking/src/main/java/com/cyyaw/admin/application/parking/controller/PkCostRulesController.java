package com.cyyaw.admin.application.parking.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.application.parking.service.PkCostRulesService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.module.parking.PkCostRules;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
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

    @Operation(summary = "计费规则列表", description = "分页查询计费规则")
    @GetMapping("/list")
    public BaseResult<List<PkCostRules>> list(@RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer size,
                                              @RequestParam(required = false) String name) {
        QueryWrapper<PkCostRules> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like("name", name);
        }
        wrapper.eq("del_time", 0).orderByDesc("create_time");
        Page<PkCostRules> pageResult = pkCostRulesService.findPage(page, size, wrapper);
        BaseResult.Result result = new BaseResult.Result(page, size, pageResult.getTotal());
        return BaseResult.ok(pageResult.getRecords(), result);
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

    @Operation(summary = "按停车场查询计费规则", description = "返回某停车场已关联的计费规则ID集合（字符串数组，供回显）")
    @GetMapping("/findByParkingId/{parkingId}")
    public BaseResult<List<String>> findByParkingId(@PathVariable Long parkingId) {
        return BaseResult.ok(pkCostRulesService.findRuleIdsByParkingId(parkingId));
    }

    @Operation(summary = "按停车场设置计费规则", description = "同步某停车场的计费规则关联（多对多，清旧链按新集合重写）")
    @PostMapping("/saveByParking/{parkingId}")
    public BaseResult<Void> saveByParking(@PathVariable Long parkingId,
                                          @RequestBody List<String> costRulesIds) {
        pkCostRulesService.saveByParking(parkingId, costRulesIds);
        return BaseResult.ok();
    }

}
