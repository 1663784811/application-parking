//package com.cyyaw.admin.common;
//
//import cn.hutool.json.JSONObject;
//import lombok.Getter;
//import lombok.Setter;
//
//import java.util.ArrayList;
//import java.util.List;
//
//
///**
// * sql 字段解释
// */
//@Getter
//public class SqlField {
//
//    private SqlField() {
//    }
//
//    /**
//     * 条件
//     */
//    private WhereType whereType;
//
//    /**
//     * 数据库字段名
//     */
//    private String dbField;
//
//    /**
//     * 别名
//     */
//    private String asField;
//
//    /**
//     *
//     */
//    @Setter
//    private String key;
//
//    /**
//     * 要替换的参数
//     */
//    private String args;
//
//    /**
//     * 输出的数据
//     */
//    private List<Object> outValue = new ArrayList<>();
//
//
//    /**
//     * 规则：
//     * 1.是否可以为空: &不为空
//     * 2.条件: @、!@ 、%、L%、R%、>=、<=、>、<
//     * 3.别名分割： :=
//     * 示例 [条件1  条件2  别名 := 字段名]
//     * [&@appId:=app_id]  解释: 不为空、in、appId别名 、 app_id 字段名
//     * 解释条件
//     * []     ---->   =
//     * [@]    ---->   in
//     * [!@]   ---->  not in
//     * [%]    ---->  like
//     * [L%]   ---->  like
//     * [R%]   ---->  like
//     * [!%]   ---->  not like
//     * [>=] ---->  大于等于
//     * [<=] ---->  小于等于
//     * [>] ---->  大于
//     * [<] ---->  小于
//     * [:=]   ---->  别名  [cc:=sss]
//     * [&]   ----> 非null
//     *
//     * @param str
//     * @param json
//     */
//    public static SqlField explainWhere(String str, JSONObject json) {
//        SqlField sqlField = new SqlField();
//        boolean notNull = false;
//        if (str.indexOf("&") == 0) {
//            str = str.substring("&".length());
//            notNull = true;
//        }
//        if (str.indexOf("!@") == 0) {
//            sqlField.whereType = WhereType.inN;
//            str = str.substring("!@".length());
//        } else if (str.indexOf("@") == 0) {
//            sqlField.whereType = WhereType.in;
//            str = str.substring("@".length());
//        } else if (str.indexOf("L%") == 0) {
//            sqlField.whereType = WhereType.likeL;
//            str = str.substring("L%".length());
//        } else if (str.indexOf("R%") == 0) {
//            sqlField.whereType = WhereType.likeR;
//            str = str.substring("R%".length());
//        } else if (str.indexOf("%") == 0) {
//            sqlField.whereType = WhereType.like;
//            str = str.substring("%".length());
//        } else if (str.indexOf(">=") == 0) {
//            sqlField.whereType = WhereType.geq;
//            str = str.substring(">=".length());
//        } else if (str.indexOf("<=") == 0) {
//            sqlField.whereType = WhereType.leq;
//            str = str.substring("<=".length());
//        } else if (str.indexOf(">") == 0) {
//            sqlField.whereType = WhereType.gt;
//            str = str.substring(">".length());
//        } else if (str.indexOf("<") == 0) {
//            sqlField.whereType = WhereType.lt;
//            str = str.substring("<".length());
//        } else {
//            sqlField.whereType = WhereType.eq;
//        }
//        String[] split = str.split(":=");
//        if (split.length == 2) {
//            sqlField.dbField = split[1];
//            sqlField.asField = split[0];
//        } else {
//            sqlField.dbField = split[0];
//            sqlField.asField = split[0];
//        }
//        Object value = json.get(sqlField.asField);
//        // =============================
//        if (value == null && notNull) {
//            sqlField.args = "1<>1";
//        } else if (value == null) {
//            sqlField.args = "1=1";
//        } else {
//            if (sqlField.whereType == WhereType.inN) {
//                String valueStr = String.valueOf(value);
//                String[] valueArr = valueStr.split(",");
//                StringBuffer sb = new StringBuffer();
//                for (int i = 0; i < valueArr.length; i++) {
//                    sqlField.outValue.add(valueArr[i]);
//                    if (i == 0) {
//                        sb.append("?");
//                    } else {
//                        sb.append(",?");
//                    }
//                }
//                sqlField.args = sqlField.dbField + " not in (" + sb.toString() + ")";
//            } else if (sqlField.whereType == WhereType.in) {
//                String valueStr = String.valueOf(value);
//                String[] valueArr = valueStr.split(",");
//                StringBuffer sb = new StringBuffer();
//                for (int i = 0; i < valueArr.length; i++) {
//                    sqlField.outValue.add(valueArr[i]);
//                    if (i == 0) {
//                        sb.append("?");
//                    } else {
//                        sb.append(",?");
//                    }
//                }
//                sqlField.args = sqlField.dbField + " in (" + sb.toString() + ")";
//            } else if (sqlField.whereType == WhereType.likeL) {
//                sqlField.outValue.add(value);
//                sqlField.args = sqlField.dbField + " like concat('%', ?)";
//            } else if (sqlField.whereType == WhereType.likeR) {
//                sqlField.outValue.add(value);
//                sqlField.args = sqlField.dbField + " like concat(?,'%')";
//            } else if (sqlField.whereType == WhereType.like) {
//                sqlField.outValue.add(value);
//                sqlField.args = sqlField.dbField + " like concat('%',?,'%')";
//            } else if (sqlField.whereType == WhereType.geq) {
//                sqlField.outValue.add(value);
//                sqlField.args = sqlField.dbField + " >= ?";
//            } else if (sqlField.whereType == WhereType.leq) {
//                sqlField.outValue.add(value);
//                sqlField.args = sqlField.dbField + " <= ?";
//            } else if (sqlField.whereType == WhereType.gt) {
//                sqlField.outValue.add(value);
//                sqlField.args = sqlField.dbField + " > ?";
//            } else if (sqlField.whereType == WhereType.lt) {
//                sqlField.outValue.add(value);
//                sqlField.args = sqlField.dbField + " < ?";
//            } else if (sqlField.whereType == WhereType.eq) {
//                sqlField.outValue.add(value);
//                sqlField.args = sqlField.dbField + " = ?";
//            }
//        }
//        return sqlField;
//    }
//
//
//}
