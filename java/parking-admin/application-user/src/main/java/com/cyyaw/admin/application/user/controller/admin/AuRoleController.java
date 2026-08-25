package com.cyyaw.admin.application.user.controller.admin;

import com.cyyaw.admin.application.user.service.AuRoleService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.module.user.AuRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色")
@RestController
@RequestMapping("/system/role")
public class AuRoleController {

    @Autowired
    private AuRoleService auRoleService;

    @Operation(summary = "角色列表", description = "查询全部角色（按创建时间倒序，含各角色成员数）")
    @GetMapping("/list")
    public BaseResult<List<AuRole>> list() {
        return BaseResult.ok(auRoleService.findAll());
    }

    @Operation(summary = "查询角色", description = "根据ID查询角色")
    @GetMapping("/find/{id}")
    public BaseResult<AuRole> findById(@PathVariable Long id) {
        return BaseResult.ok(auRoleService.findById(id));
    }

    @Operation(summary = "保存角色", description = "新增或更新角色")
    @PostMapping("/save")
    public BaseResult<AuRole> save(@RequestBody AuRole role) {
        return BaseResult.ok(auRoleService.save(role));
    }

    @Operation(summary = "删除角色", description = "根据ID删除角色")
    @DeleteMapping("/delete/{id}")
    public BaseResult<Void> delete(@PathVariable Long id) {
        auRoleService.delete(id);
        return BaseResult.ok();
    }
}
