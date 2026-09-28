package com.cyyaw.admin.application.common.controller;

import com.cyyaw.admin.common.BaseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "公共接口")
@RestController
@RequestMapping("/admin/common/{module}")
public class CommonController {


    @Operation(summary = "公共查询", description = "公共查询")
    @GetMapping("/query/{code}")
    public BaseResult<Object> query() {



        return BaseResult.ok();
    }


}
