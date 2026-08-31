package com.cyyaw.admin.application.iot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.entity.module.iot.IotThingAttribute;
import com.cyyaw.admin.entity.module.iot.IotThingCommand;
import com.cyyaw.admin.entity.module.iot.IotThingEvent;
import com.cyyaw.admin.entity.module.iot.IotThingModel;

import java.util.List;
import java.util.Map;

public interface IotThingModelService {

    // ===== 物模型 =====
    IotThingModel findById(Long id);

    IotThingModel save(IotThingModel model);

    /**
     * 删除物模型，并级联删除其下的属性/事件/指令（按 thing_model_id）
     */
    void delete(Long id);

    Page<IotThingModel> findPage(Integer page, Integer size, QueryWrapper<IotThingModel> wrapper);

    /**
     * 统计：模型总数、属性/事件/指令总数
     */
    Map<String, Object> stats();

    /**
     * 批量统计多个物模型的属性/事件/指令数量
     * key=模型ID, value={attribute,event,command}
     */
    Map<Long, Map<String, Integer>> countSubByModelIds(List<Long> modelIds);

    // ===== 属性 =====
    List<IotThingAttribute> listAttributeByModelId(Long thingModelId);

    IotThingAttribute saveAttribute(IotThingAttribute attribute);

    void deleteAttribute(Long id);

    // ===== 事件 =====
    List<IotThingEvent> listEventByModelId(Long thingModelId);

    IotThingEvent saveEvent(IotThingEvent event);

    void deleteEvent(Long id);

    // ===== 指令 =====
    List<IotThingCommand> listCommandByModelId(Long thingModelId);

    IotThingCommand saveCommand(IotThingCommand command);

    void deleteCommand(Long id);

}
