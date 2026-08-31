package com.cyyaw.admin.application.iot.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.application.iot.service.IotThingModelService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.module.iot.IotThingAttribute;
import com.cyyaw.admin.entity.module.iot.IotThingCommand;
import com.cyyaw.admin.entity.module.iot.IotThingEvent;
import com.cyyaw.admin.entity.module.iot.IotThingModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "物模型")
@RestController
@RequestMapping("/admin/device/thingModel")
public class IotThingModelController {

    @Autowired
    private IotThingModelService iotThingModelService;

    // ===== 物模型 =====

    @Operation(summary = "物模型详情", description = "根据ID查询物模型")
    @GetMapping("/find/{id}")
    public BaseResult<IotThingModel> findById(@PathVariable Long id) {
        return BaseResult.ok(iotThingModelService.findById(id));
    }

    @Operation(summary = "物模型列表", description = "分页查询物模型")
    @GetMapping("/list")
    public BaseResult<List<IotThingModel>> list(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size, @RequestParam(required = false) String keyword) {
        QueryWrapper<IotThingModel> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like("name", keyword);
        }
        wrapper.orderByDesc("create_time");
        Page<IotThingModel> pageResult = iotThingModelService.findPage(page, size, wrapper);
        BaseResult.Result result = new BaseResult.Result(page, size, pageResult.getTotal());
        return BaseResult.ok(pageResult.getRecords(), result);
    }

    @Operation(summary = "保存物模型", description = "新增或更新物模型")
    @PostMapping("/save")
    public BaseResult<IotThingModel> save(@RequestBody IotThingModel model) {
        return BaseResult.ok(iotThingModelService.save(model));
    }

    @Operation(summary = "删除物模型", description = "根据ID删除物模型，并级联删除其属性/事件/指令")
    @DeleteMapping("/delete/{id}")
    public BaseResult<Void> delete(@PathVariable Long id) {
        iotThingModelService.delete(id);
        return BaseResult.ok();
    }

    @Operation(summary = "物模型统计", description = "模型总数、属性/事件/指令总数")
    @GetMapping("/stats")
    public BaseResult<Map<String, Object>> stats() {
        return BaseResult.ok(iotThingModelService.stats());
    }

    @Operation(summary = "子实体计数", description = "批量统计多个物模型的属性/事件/指令数量")
    @GetMapping("/subCounts")
    public BaseResult<Map<Long, Map<String, Integer>>> subCounts(@RequestParam(required = false) List<Long> modelIds) {
        return BaseResult.ok(iotThingModelService.countSubByModelIds(modelIds));
    }

    // ===== 属性 =====

    @Operation(summary = "属性列表", description = "按物模型ID查询全部属性")
    @GetMapping("/attribute/list")
    public BaseResult<List<IotThingAttribute>> listAttribute(@RequestParam Long thingModelId) {
        return BaseResult.ok(iotThingModelService.listAttributeByModelId(thingModelId));
    }

    @Operation(summary = "保存属性", description = "新增或更新属性")
    @PostMapping("/attribute/save")
    public BaseResult<IotThingAttribute> saveAttribute(@RequestBody IotThingAttribute attribute) {
        return BaseResult.ok(iotThingModelService.saveAttribute(attribute));
    }

    @Operation(summary = "删除属性", description = "根据ID删除属性")
    @DeleteMapping("/attribute/delete/{id}")
    public BaseResult<Void> deleteAttribute(@PathVariable Long id) {
        iotThingModelService.deleteAttribute(id);
        return BaseResult.ok();
    }

    // ===== 事件 =====

    @Operation(summary = "事件列表", description = "按物模型ID查询全部事件")
    @GetMapping("/event/list")
    public BaseResult<List<IotThingEvent>> listEvent(@RequestParam Long thingModelId) {
        return BaseResult.ok(iotThingModelService.listEventByModelId(thingModelId));
    }

    @Operation(summary = "保存事件", description = "新增或更新事件")
    @PostMapping("/event/save")
    public BaseResult<IotThingEvent> saveEvent(@RequestBody IotThingEvent event) {
        return BaseResult.ok(iotThingModelService.saveEvent(event));
    }

    @Operation(summary = "删除事件", description = "根据ID删除事件")
    @DeleteMapping("/event/delete/{id}")
    public BaseResult<Void> deleteEvent(@PathVariable Long id) {
        iotThingModelService.deleteEvent(id);
        return BaseResult.ok();
    }

    // ===== 指令 =====

    @Operation(summary = "指令列表", description = "按物模型ID查询全部指令")
    @GetMapping("/command/list")
    public BaseResult<List<IotThingCommand>> listCommand(@RequestParam Long thingModelId) {
        return BaseResult.ok(iotThingModelService.listCommandByModelId(thingModelId));
    }

    @Operation(summary = "保存指令", description = "新增或更新指令")
    @PostMapping("/command/save")
    public BaseResult<IotThingCommand> saveCommand(@RequestBody IotThingCommand command) {
        return BaseResult.ok(iotThingModelService.saveCommand(command));
    }

    @Operation(summary = "删除指令", description = "根据ID删除指令")
    @DeleteMapping("/command/delete/{id}")
    public BaseResult<Void> deleteCommand(@PathVariable Long id) {
        iotThingModelService.deleteCommand(id);
        return BaseResult.ok();
    }

}
