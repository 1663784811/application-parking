package com.cyyaw.admin.dao.service;

import lombok.Getter;

/**
 * jpa 条件类型
 */
public enum WhereType {
    like("lk", "%模糊查询%"),
    likeR("lkR", "模糊查询%"),
    likeL("lkL", "%模糊查询"),
    eq("eq", "等于 = "),
    neq("neq", "不等于"),
    geq("geq", "大于等于 >="),
    gt("gt", "大于 >"),
    leq("leq", "小于等于 <="),
    lt("lt", "小于 <"),
    in("in", "包含"),
    inN("niN", "不包含");

    WhereType(String where, String note) {
        this.where = where;
        this.note = note;
    }

    /**
     * 条件
     */
    @Getter
    private final String where;
    /**
     * 备注
     */
    private final String note;

}
