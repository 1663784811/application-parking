package com.cyyaw.admin.application.user.controller.admin;

import com.cyyaw.admin.application.user.service.AuMenuService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.module.user.AuMenu;
import com.cyyaw.admin.entity.utils.TreeResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "菜单-模块")
@RestController
@RequestMapping("/admin/menu")
public class AdminMenuController {

    @Autowired
    private AuMenuService auMenuService;

    @Operation(summary = "获取用户菜单", description = "获取用户菜单")
    @GetMapping("/userMenu")
    public BaseResult<List<TreeResponseEntity.Node<AuMenu>>> userMenu() {
        List<TreeResponseEntity.Node<AuMenu>> menuList = auMenuService.findLoginAdminMenu();
        return BaseResult.ok(menuList);
    }

}
