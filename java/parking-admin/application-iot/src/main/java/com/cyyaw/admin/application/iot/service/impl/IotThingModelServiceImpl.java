package com.cyyaw.admin.application.iot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.application.iot.service.IotThingModelService;
import com.cyyaw.admin.dao.iot.IotThingAttributeDao;
import com.cyyaw.admin.dao.iot.IotThingCommandDao;
import com.cyyaw.admin.dao.iot.IotThingEventDao;
import com.cyyaw.admin.dao.iot.IotThingModelDao;
import com.cyyaw.admin.entity.module.iot.IotThingAttribute;
import com.cyyaw.admin.entity.module.iot.IotThingCommand;
import com.cyyaw.admin.entity.module.iot.IotThingEvent;
import com.cyyaw.admin.entity.module.iot.IotThingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IotThingModelServiceImpl implements IotThingModelService {

    @Autowired
    private IotThingModelDao iotThingModelDao;

    @Autowired
    private IotThingAttributeDao iotThingAttributeDao;

    @Autowired
    private IotThingEventDao iotThingEventDao;

    @Autowired
    private IotThingCommandDao iotThingCommandDao;

    @Override
    public IotThingModel findById(Long id) {
        return iotThingModelDao.selectById(id);
    }

    @Override
    public IotThingModel save(IotThingModel model) {
        return iotThingModelDao.save(model);
    }

    @Override
    public void delete(Long id) {
        iotThingModelDao.deleteById(id);
        // 级联删除子实体（按 thing_model_id）
        iotThingAttributeDao.delete(new QueryWrapper<IotThingAttribute>().eq("thing_model_id", id));
        iotThingEventDao.delete(new QueryWrapper<IotThingEvent>().eq("thing_model_id", id));
        iotThingCommandDao.delete(new QueryWrapper<IotThingCommand>().eq("thing_model_id", id));
    }

    @Override
    public Page<IotThingModel> findPage(Integer page, Integer size, QueryWrapper<IotThingModel> wrapper) {
        return iotThingModelDao.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public Map<String, Object> stats() {
        Map<String, Object> s = new HashMap<>();
        s.put("total", iotThingModelDao.selectCount(null));
        s.put("attribute", iotThingAttributeDao.selectCount(null));
        s.put("event", iotThingEventDao.selectCount(null));
        s.put("command", iotThingCommandDao.selectCount(null));
        return s;
    }

    @Override
    public Map<Long, Map<String, Integer>> countSubByModelIds(List<Long> modelIds) {
        Map<Long, Map<String, Integer>> result = new HashMap<>();
        if (modelIds == null || modelIds.isEmpty()) {
            return result;
        }
        // 预置每个模型的三项计数为 0（无子实体的模型也能显示 0）
        for (Long id : modelIds) {
            Map<String, Integer> c = new HashMap<>();
            c.put("attribute", 0);
            c.put("event", 0);
            c.put("command", 0);
            result.put(id, c);
        }
        // 按 thing_model_id 批量查询子实体，再分组计数（3 次查询，避免 N+1）
        List<IotThingAttribute> attrs = iotThingAttributeDao.selectList(
                new QueryWrapper<IotThingAttribute>().in("thing_model_id", modelIds));
        for (IotThingAttribute a : attrs) {
            Map<String, Integer> c = result.get(a.getThingModelId());
            if (c != null) c.merge("attribute", 1, Integer::sum);
        }
        List<IotThingEvent> events = iotThingEventDao.selectList(
                new QueryWrapper<IotThingEvent>().in("thing_model_id", modelIds));
        for (IotThingEvent e : events) {
            Map<String, Integer> c = result.get(e.getThingModelId());
            if (c != null) c.merge("event", 1, Integer::sum);
        }
        List<IotThingCommand> cmds = iotThingCommandDao.selectList(
                new QueryWrapper<IotThingCommand>().in("thing_model_id", modelIds));
        for (IotThingCommand cmd : cmds) {
            Map<String, Integer> c = result.get(cmd.getThingModelId());
            if (c != null) c.merge("command", 1, Integer::sum);
        }
        return result;
    }

    @Override
    public List<IotThingAttribute> listAttributeByModelId(Long thingModelId) {
        return iotThingAttributeDao.selectList(new QueryWrapper<IotThingAttribute>()
                .eq("thing_model_id", thingModelId).orderByDesc("create_time"));
    }

    @Override
    public IotThingAttribute saveAttribute(IotThingAttribute attribute) {
        return iotThingAttributeDao.save(attribute);
    }

    @Override
    public void deleteAttribute(Long id) {
        iotThingAttributeDao.deleteById(id);
    }

    @Override
    public List<IotThingEvent> listEventByModelId(Long thingModelId) {
        return iotThingEventDao.selectList(new QueryWrapper<IotThingEvent>()
                .eq("thing_model_id", thingModelId).orderByDesc("create_time"));
    }

    @Override
    public IotThingEvent saveEvent(IotThingEvent event) {
        return iotThingEventDao.save(event);
    }

    @Override
    public void deleteEvent(Long id) {
        iotThingEventDao.deleteById(id);
    }

    @Override
    public List<IotThingCommand> listCommandByModelId(Long thingModelId) {
        return iotThingCommandDao.selectList(new QueryWrapper<IotThingCommand>()
                .eq("thing_model_id", thingModelId).orderByDesc("create_time"));
    }

    @Override
    public IotThingCommand saveCommand(IotThingCommand command) {
        return iotThingCommandDao.save(command);
    }

    @Override
    public void deleteCommand(Long id) {
        iotThingCommandDao.deleteById(id);
    }

}
