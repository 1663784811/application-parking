package com.cyyaw.admin.application.device.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.application.device.service.MeDeviceFaultService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.module.iot.IotDeviceFault;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Tag(name = "设备故障工单")
@RestController
@RequestMapping("/admin/device/fault")
public class MeDeviceFaultController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private MeDeviceFaultService meDeviceFaultService;

    @Operation(summary = "查询故障工单", description = "根据ID查询故障工单")
    @GetMapping("/find/{id}")
    public BaseResult<IotDeviceFault> findById(@PathVariable Long id) {
        return BaseResult.ok(meDeviceFaultService.findById(id));
    }

    @Operation(summary = "故障工单列表", description = "分页查询故障工单")
    @GetMapping("/list")
    public BaseResult<List<IotDeviceFault>> list(@RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer size,
                                                 @RequestParam(required = false) String status,
                                                 @RequestParam(required = false) String startTime,
                                                 @RequestParam(required = false) String endTime) {
        QueryWrapper<IotDeviceFault> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq("status", status);
        }
        if (StringUtils.hasText(startTime)) {
            wrapper.ge("report_time", startTime + " 00:00:00");
        }
        if (StringUtils.hasText(endTime)) {
            wrapper.le("report_time", endTime + " 23:59:59");
        }
        wrapper.orderByDesc("report_time");
        Page<IotDeviceFault> pageResult = meDeviceFaultService.findPage(page, size, wrapper);
        BaseResult.Result result = new BaseResult.Result(page, size, pageResult.getTotal());
        return BaseResult.ok(pageResult.getRecords(), result);
    }

    @Operation(summary = "创建故障工单", description = "根据设备ID报修并生成故障工单")
    @PostMapping("/create")
    public BaseResult<IotDeviceFault> create(@RequestBody Map<String, Object> body) {
        Object deviceIdVal = body.get("deviceId");
        if (deviceIdVal == null) {
            throw new RuntimeException("缺少设备ID");
        }
        Long deviceId = Long.valueOf(deviceIdVal.toString());
        String faultType = body.get("faultType") == null ? null : body.get("faultType").toString();
        return BaseResult.ok(meDeviceFaultService.create(deviceId, faultType));
    }

    @Operation(summary = "处理故障工单", description = "指派维修人员或标记完成")
    @PostMapping("/handle")
    public BaseResult<IotDeviceFault> handle(@RequestBody Map<String, Object> body) {
        Object idVal = body.get("id");
        if (idVal == null) {
            throw new RuntimeException("缺少工单ID");
        }
        Long id = Long.valueOf(idVal.toString());
        String action = body.get("action") == null ? null : body.get("action").toString();
        String repairer = body.get("repairer") == null ? null : body.get("repairer").toString();
        LocalDateTime expectCompleteTime = null;
        Object expectVal = body.get("expectCompleteTime");
        if (expectVal != null && StringUtils.hasText(expectVal.toString())) {
            expectCompleteTime = LocalDateTime.parse(expectVal.toString(), DATE_TIME_FORMATTER);
        }
        return BaseResult.ok(meDeviceFaultService.handle(id, action, repairer, expectCompleteTime));
    }

    @Operation(summary = "故障工单统计", description = "按处理状态统计")
    @GetMapping("/stats")
    public BaseResult<Map<String, Object>> stats() {
        return BaseResult.ok(meDeviceFaultService.stats());
    }

}
