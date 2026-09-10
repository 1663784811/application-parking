package com.cyyaw.admin.common.json;


import cn.hutool.json.JSONObject;

public class JsonUtil {


    public static String beanToString(Object bean) {
        JSONObject entries = new JSONObject(bean);

        return entries.toString();
    }
}
