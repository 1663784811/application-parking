package com.cyyaw.admin.common;

import lombok.Data;


/**
 * sql 数据
 */
@Data
public class SqlData {

    /**
     * 要执行的sql
     */
    private String sql;

    /**
     * 数据
     */
    private Object[] val;

}
