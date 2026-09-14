package com.cyyaw.admin.application.user.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.application.user.service.AuAdminService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.user.AuAdminQueryDTO;
import com.cyyaw.admin.entity.module.user.AuAdmin;
import com.cyyaw.admin.entity.utils.LoginInfo;
import com.cyyaw.admin.entity.utils.LoginInfoContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理员账号")
@RestController
@RequestMapping("/system/admin")
public class AuAdminController {

    @Autowired
    private AuAdminService auAdminService;

    @Operation(summary = "管理员列表", description = "分页查询当前企业管理员（支持账号、真实姓名、手机号、状态筛选）；查询参数 page/size/keyword/status，以 AuAdminQueryDTO 实体类接收（Spring 隐式 @ModelAttribute 按名绑定查询串到字段）")
    @GetMapping("/list")
    public BaseResult<List<AuAdmin>> list(AuAdminQueryDTO query) {
        Integer page = query.getPage();
        Integer size = query.getSize();
        String keyword = query.getKeyword();
        Integer status = query.getStatus();
        LoginInfo loginInfo = LoginInfoContext.getLoginInfo();
        Long enId = loginInfo != null ? loginInfo.getEnId() : null;
        QueryWrapper<AuAdmin> wrapper = new QueryWrapper<>();
        if (enId != null) {
            wrapper.eq("en_id", enId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("account", keyword)
                    .or().like("real_name", keyword)
                    .or().like("phone", keyword));
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.eq("del_time", 0);
        Page<AuAdmin> pageResult = auAdminService.findPage(page, size, wrapper);
        BaseResult.Result result = new BaseResult.Result(page, size, pageResult.getTotal());
        return BaseResult.ok(pageResult.getRecords(), result);
    }

    @Operation(summary = "查询管理员", description = "根据ID查询管理员（回填关联角色ID）")
    @GetMapping("/find/{id}")
    public BaseResult<AuAdmin> findById(@PathVariable Long id) {
        return BaseResult.ok(auAdminService.findById(id));
    }

    @Operation(summary = "新增管理员", description = "新增管理员账号，默认密码 123456，可关联多个角色")
    @PostMapping("/add")
    public BaseResult<AuAdmin> add(@RequestBody AuAdmin admin) {
        AuAdmin result = auAdminService.add(admin, admin.getRoleIds());
        return BaseResult.ok(result);
    }

    @Operation(summary = "编辑管理员", description = "更新管理员账号信息并同步角色关联（多对多，清旧链按新集合重写），密码不在更新范围")
    @PutMapping("/edit")
    public BaseResult<AuAdmin> edit(@RequestBody AuAdmin admin) {
        AuAdmin result = auAdminService.edit(admin, admin.getRoleIds());
        return BaseResult.ok(result);
    }

    @Operation(summary = "删除管理员", description = "根据ID删除管理员（同时清除角色关联）")
    @DeleteMapping("/delete/{id}")
    public BaseResult<Void> delete(@PathVariable Long id) {
        auAdminService.delete(id);
        return BaseResult.ok();
    }

    @Operation(summary = "重置密码", description = "将管理员密码重置为默认值 123456")
    @PostMapping("/resetPassword/{id}")
    public BaseResult<Void> resetPassword(@PathVariable Long id) {
        auAdminService.resetPassword(id);
        return BaseResult.ok();
    }

}
