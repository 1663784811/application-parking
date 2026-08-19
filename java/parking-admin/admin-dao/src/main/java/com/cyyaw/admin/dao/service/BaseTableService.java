package com.cyyaw.admin.dao.service;

import cn.hutool.json.JSONObject;
import com.cyyaw.admin.common.BaseResult;

import java.util.List;

public interface BaseTableService<T> {

    List<T> findByExample(T t);

    BaseResult<List<T>> findPage(JSONObject json, Class<T> clazz);
}
