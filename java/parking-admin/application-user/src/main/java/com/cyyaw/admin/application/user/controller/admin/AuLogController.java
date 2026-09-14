package com.cyyaw.admin.application.user.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.application.user.service.AuLogService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.user.AuLogQueryDTO;
import com.cyyaw.admin.entity.module.user.AuLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统日志")
@RestController
@RequestMapping("/system/log")
public class AuLogController {

    @Autowired
    private AuLogService auLogService;

    @Operation(summary = "查询日志", description = "根据ID查询日志详情")
    @GetMapping("/find/{id}")
    public BaseResult<AuLog> findById(@PathVariable Long id) {
        return BaseResult.ok(auLogService.findById(id));
    }

    @Operation(summary = "日志列表", description = "分页查询日志（登录日志/操作日志）；查询参数 page/size/logType/startTime/endTime/keyword，以 AuLogQueryDTO 实体类接收（Spring 隐式 @ModelAttribute 按名绑定查询串到字段）")
    @GetMapping("/list")
    public BaseResult<List<AuLog>> list(AuLogQueryDTO query) {
        Integer page = query.getPage();
        Integer size = query.getSize();
        String logType = query.getLogType();
        String startTime = query.getStartTime();
        String endTime = query.getEndTime();
        String keyword = query.getKeyword();
        QueryWrapper<AuLog> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(logType)) {
            wrapper.eq("log_type", logType);
        }
        if (StringUtils.hasText(startTime)) {
            wrapper.ge("create_time", startTime + " 00:00:00");
        }
        if (StringUtils.hasText(endTime)) {
            wrapper.le("create_time", endTime + " 23:59:59");
        }
        if (StringUtils.hasText(keyword)) {
            // 关键字匹配操作人账号或操作描述
            wrapper.and(w -> w.like("admin_name", keyword).or().like("description", keyword));
        }
        wrapper.orderByDesc("create_time");
        Page<AuLog> pageResult = auLogService.findPage(page, size, wrapper);
        BaseResult.Result result = new BaseResult.Result(page, size, pageResult.getTotal());
        return BaseResult.ok(pageResult.getRecords(), result);
    }

}
