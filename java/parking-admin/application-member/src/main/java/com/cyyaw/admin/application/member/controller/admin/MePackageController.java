package com.cyyaw.admin.application.member.controller.admin;

import com.cyyaw.admin.application.member.service.MePackageService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.module.member.MePackage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "会员套餐")
@RestController
@RequestMapping("/admin/member/package")
public class MePackageController {

    @Autowired
    private MePackageService mePackageService;

    @Operation(summary = "查询套餐", description = "根据ID查询套餐")
    @GetMapping("/find/{id}")
    public BaseResult<MePackage> findById(@PathVariable Long id) {
        MePackage mePackage = mePackageService.findById(id);
        return BaseResult.ok(mePackage);
    }

    @Operation(summary = "套餐列表", description = "查询全部套餐")
    @GetMapping("/list")
    public BaseResult<List<MePackage>> list() {
        List<MePackage> list = mePackageService.findAll();
        return BaseResult.ok(list);
    }

    @Operation(summary = "保存套餐", description = "新增或更新套餐")
    @PostMapping("/save")
    public BaseResult<MePackage> save(@RequestBody MePackage mePackage) {
        MePackage result = mePackageService.save(mePackage);
        return BaseResult.ok(result);
    }

    @Operation(summary = "删除套餐", description = "根据ID删除套餐")
    @DeleteMapping("/delete/{id}")
    public BaseResult<Void> delete(@PathVariable Long id) {
        mePackageService.delete(id);
        return BaseResult.ok();
    }

}
