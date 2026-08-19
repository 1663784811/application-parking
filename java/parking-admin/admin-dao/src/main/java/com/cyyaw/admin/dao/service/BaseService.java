package com.cyyaw.admin.dao.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.dao.BaseMapperPlus;
import jakarta.persistence.Column;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
public abstract class BaseService<M extends BaseMapperPlus, T> implements BaseTableService<T> {

    @Autowired
    private M baseDao;

    private static Map<String, Map<String, String>> entityTable = new HashMap<String, Map<String, String>>();


    public List<T> findByExample(T t) {
        QueryWrapper<T> wrapper = new QueryWrapper<>(t);
        return baseDao.selectList(wrapper);
    }

    public BaseResult<List<T>> findPage(JSONObject json, Class<T> clazz) {
        String sortStr = json.getStr("sort");
        Integer page = json.getInt("page");
        Integer size = json.getInt("size");
        if (page == null) {
            page = 1;
        }
        if (size == null) {
            size = 30;
        }
        IPage<T> p = new Page(page, size);
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        Map<String, String> stringStringMap = loadEntityTable(clazz);
        buildWhere(json, stringStringMap, wrapper);
        buildSort(sortStr, stringStringMap, wrapper);

        IPage<T> pageData = baseDao.selectPage(p, wrapper);
        long total = pageData.getTotal();
        BaseResult.Result result = new BaseResult.Result();
        result.setTotal(total);
        result.setPage(page);
        result.setSize((int) pageData.getSize());
        List<T> records = pageData.getRecords();
        return BaseResult.ok(records, result);
    }

    private void buildSort(String sortStr, Map<String, String> stringStringMap, QueryWrapper<T> wrapper) {
        if (StrUtil.isNotBlank(sortStr)) {
            String[] split = sortStr.split(",");
            for (String string : split) {
                String[] sort = string.split("_");
                String s = stringStringMap.get(sort[0]);
                if (StrUtil.isNotBlank(s)) {
                    if (sort.length == 2 && sort[1].equals("desc")) {
                        wrapper.orderByDesc(s);
                    } else {
                        wrapper.orderByAsc(s);
                    }
                }
            }
        }
    }

    private Map<String, String> loadEntityTable(Class<?> clazz) {
        String clazzName = clazz.getName();
        Map<String, String> stringStringMap = entityTable.get(clazzName);
        if (stringStringMap == null) {
            stringStringMap = new HashMap<>();
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Column annotation = field.getAnnotation(Column.class);
                String dbName;
                if (annotation != null) {
                    dbName = annotation.name();
                    if (StrUtil.isBlank(dbName)) {
                        dbName = fieldName;
                    }
                } else {
                    dbName = fieldName;
                }
                stringStringMap.put(fieldName, dbName);
            }
            stringStringMap.put("id", "id");
            stringStringMap.put("enId", "en_id");
            stringStringMap.put("createTime", "create_time");
            stringStringMap.put("updateTime", "update_time");
            stringStringMap.put("note", "note");
            entityTable.put(clazzName, stringStringMap);
        }
        return stringStringMap;
    }

    private void buildWhere(JSONObject json, Map<String, String> stringStringMap, QueryWrapper<T> wrapper) {
        for (String key : json.keySet()) {
            String o = json.getStr(key);
            if (StrUtil.isBlank(o)) {
                continue;
            }
            WhereType[] values = WhereType.values();
            String entityKey = null;
            WhereType wheres = WhereType.eq;
            for (int i = 0; i < values.length; i++) {
                WhereType value = values[i];
                String where = value.getWhere() + "_";
                int index = key.indexOf(where);
                if (index == 0) {
                    entityKey = key.substring(where.length());
                    wheres = value;
                    break;
                }
            }
            String dbKey = stringStringMap.get(entityKey);
            if (StrUtil.isNotBlank(dbKey)) {
                if (WhereType.eq.equals(wheres)) {
                    wrapper.eq(dbKey, o);
                } else if (WhereType.like.equals(wheres)) {
                    wrapper.like(dbKey, "%" + o + "%");
                } else if (WhereType.likeL.equals(wheres)) {
                    wrapper.like(dbKey, o + "%");
                } else if (WhereType.likeR.equals(wheres)) {
                    wrapper.like(dbKey, "%" + o);
                } else if (WhereType.neq.equals(wheres)) {
//                    wrapper.not(dbKey, o);
                } else if (WhereType.geq.equals(wheres)) {
                    wrapper.ge(dbKey, o);
                } else if (WhereType.gt.equals(wheres)) {
                    wrapper.gt(dbKey, o);
                } else if (WhereType.leq.equals(wheres)) {
                    wrapper.le(dbKey, o);
                } else if (WhereType.lt.equals(wheres)) {
                    wrapper.lt(dbKey, o);
                } else if (WhereType.in.equals(wheres)) {
                    String[] split = o.split(",");
                    wrapper.in(dbKey, split);
                }
            }
        }
    }

}
