//package com.cyyaw.admin.common;
//
//import cn.hutool.core.date.DateUtil;
//import cn.hutool.core.util.StrUtil;
//import cn.hutool.json.JSONObject;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
///**
// * 解释sql工具类
// * <prl>
// * <p>
// * select * from sys_user s where s.id = '123' and s.name = '张三'
// * {=a select * from sys_user s where [s.id] and [%s.name] {=b and [s.age]} }
// * <p>
// * {=a }  块判断
// * <p>
// * []  --->   =
// * [@]  --->   in
// * [!@] ---->  not in
// * [%]  ---->  like
// * [L%]  ---->  like
// * [R%]  ---->  like
// * [!%]  ---->  not like
// * [:=]  ---->  别名  [cc:=sss]
// * [!!>=] ---->  大于等于
// * [!!<=] ---->  小于等于
// * </prl>
// */
//public class SqlUtils {
//
//    /**
//     * 解释sql
//     */
//    public static SqlData explainSql(String sql, JSONObject json) {
//        List<SqlField> listField = explainSquare(sql, json);
//        SqlData sqlData = new SqlData();
//        List<Object> values = new ArrayList<>();
//        for (SqlField sqlField : listField) {
//            String args = sqlField.getArgs();
//            String key = sqlField.getKey();
//            sql = sql.replace(key, args);
//            List<Object> outValue = sqlField.getOutValue();
//            if (outValue != null && !outValue.isEmpty()) {
//                values.addAll(outValue);
//            }
//        }
//        sqlData.setVal(values.toArray());
//        sqlData.setSql(sql);
//        return sqlData;
//    }
//
//    /**
//     * 解释 [] 括号内的内容
//     */
//    private static List<SqlField> explainSquare(String sql, JSONObject json) {
//        List<SqlField> rest = new ArrayList<>();
//        List<JSONObject> map = extractVariableContent(sql);
//        for (JSONObject js : map) {
//            String value = js.getStr("value");
//            String key = js.getStr("key");
//            SqlField sqlField = SqlField.explainWhere(value, json);
//            sqlField.setKey(key);
//            rest.add(sqlField);
//        }
//        return rest;
//    }
//
//    /**
//     * 通用保存， 解释数据
//     *
//     * @param json 正常[]
//     *             默认字符串[df:str_默认字符串]
//     *             函数[df:fn_函数名]
//     *             支持的函数名: uuid     (  32位 )
//     */
//    public static String[] saveExplainData(String sql, JSONObject json, SnowflakeIdGenerator snowflakeIdGenerator) {
//        List<String> rest = new ArrayList<>();
//        int startIndex = 0;
//        for (int i = 0; i < sql.length(); i++) {
//            if (sql.charAt(i) == '[') {
//                startIndex = i;
//            } else if (sql.charAt(i) == ']') {
//                StringBuilder jsonKey = new StringBuilder();
//                String key = sql.substring(startIndex + 1, i);
//                boolean must = false;
//                if (key.startsWith("&")) {
//                    key = key.substring(1);
//                    must = true;
//                }
//                int lastInx = key.lastIndexOf(":");
//                if (lastInx != -1) {
//                    jsonKey.append(key, lastInx + 1, key.length());
//                } else {
//                    jsonKey.append(key);
//                }
//                String str = json.getStr(jsonKey.toString());
//                if (StrUtil.isNotBlank(str)) {
//                    rest.add(str);
//                } else {
//                    String dfStr = "df:";
//                    int defIndex = key.indexOf(dfStr);
//                    if (defIndex == 0) {
//                        // 设置默认
//                        String[] split = key.split(":");
//                        if (split.length >= 2) {
//                            String defType = split[1];
//                            if (defType.indexOf("str_") == 0) {
//                                rest.add(defType.substring("str_".length()));
//                            } else if (defType.indexOf("fn_") == 0) {
//                                String fnName = defType.substring("fn_".length());
//                                if (StrUtil.isNotBlank(fnName)) {
//                                    if ("uuid".equals(fnName)) {
//                                        rest.add(WhyStringUtil.getUUID());
//                                    } else if ("snowId".equals(fnName)) {
//                                        rest.add(String.valueOf(snowflakeIdGenerator.nextId()));
//                                    } else if ("nowTime".equals(fnName)) {
//                                        rest.add(DateUtil.now());
//                                    } else {
//                                        rest.add(fnName);
//                                    }
//                                } else {
//                                    // 默认
//                                    rest.add(defType);
//                                }
//                            } else {
//                                // 默认
//                                rest.add(defType);
//                            }
//                        } else {
//                            // 默认
//                            rest.add("");
//                        }
//                    } else {
//                        // 默认
//                        if (must) {
//                            WebException.fail("参数【" + key + "】不能为空");
//                        }
//                        rest.add(str);
//                    }
//                }
//            }
//        }
//        return rest.toArray(new String[0]);
//    }
//
//    /**
//     * 通用保存, 解释sql
//     * 把 [] 号里的东西替换成 ? 号
//     */
//    public static String saveExplainSql(String sql) {
//        return explainSqlToEscapeSql(sql);
//    }
//
//    /**
//     * 通用更新， 解释数据
//     */
//    public static String[] updateExplainData(String sql, JSONObject newData, SnowflakeIdGenerator snowflakeIdGenerator) {
//        List<String> rest = new ArrayList<>();
//        int startIndex = 0;
//        for (int i = 0; i < sql.length(); i++) {
//            if (sql.charAt(i) == '[') {
//                startIndex = i;
//            } else if (sql.charAt(i) == ']') {
//                StringBuilder jsonKey = new StringBuilder();
//                String key = sql.substring(startIndex + 1, i);
//                int lastInx = key.lastIndexOf(":");
//                if (lastInx != -1) {
//                    jsonKey.append(key.substring(lastInx + 1));
//                } else {
//                    jsonKey.append(key);
//                }
//                String str = newData.getStr(jsonKey.toString());
//                if (StrUtil.isNotBlank(str)) {
//                    rest.add(str);
//                } else {
//                    String dfStr = "df:";
//                    int defIndex = key.indexOf(dfStr);
//                    if (defIndex == 0) {
//                        // 设置默认
//                        String[] split = key.split(":");
//                        if (split.length >= 2) {
//                            String defType = split[1];
//                            if (defType.indexOf("str_") == 0) {
//                                rest.add(defType.substring("str_".length()));
//                            } else if (defType.indexOf("fn_") == 0) {
//                                String fnName = defType.substring("fn_".length());
//                                if (StrUtil.isNotBlank(fnName)) {
//                                    if ("uuid".equals(fnName)) {
//                                        rest.add(WhyStringUtil.getUUID());
//                                    } else if ("snowId".equals(fnName)) {
//                                        rest.add(String.valueOf(snowflakeIdGenerator.nextId()));
//                                    } else if ("nowTime".equals(fnName)) {
//                                        rest.add(DateUtil.now());
//                                    } else {
//                                        rest.add(fnName);
//                                    }
//                                } else {
//                                    // 默认
//                                    rest.add(defType);
//                                }
//                            } else {
//                                // 默认
//                                rest.add(defType);
//                            }
//                        } else {
//                            // 默认
//                            rest.add("");
//                        }
//                    } else {
//                        // 默认
//                        rest.add(null);
//                    }
//                }
//            }
//        }
//        return rest.toArray(new String[0]);
//    }
//
//    /**
//     * 通用删除, 解释sql
//     * 把 [] 号里的东西替换成 ? 号
//     */
//    public static String delExplainSql(String sql, JSONObject json) {
//        return explainSqlToEscapeSql(sql);
//    }
//
//    /**
//     * 把 [] 号里的东西替换成 ? 号
//     */
//    public static String explainSqlToEscapeSql(String sql) {
//        StringBuilder sb = new StringBuilder();
//        boolean start = false;
//        for (int i = 0; i < sql.length(); i++) {
//            if (sql.charAt(i) == '[') {
//                start = true;
//            } else if (sql.charAt(i) == ']') {
//                start = false;
//                sb.append("?");
//            } else if (!start) {
//                sb.append(sql.charAt(i));
//            }
//        }
//        return sb.toString();
//    }
//
//    /**
//     * 提取字符 [] 里的内容
//     */
//    public static List<JSONObject> extractVariableContent(String sql) {
//        List<JSONObject> result = new ArrayList<>();
//        // 正则表达式匹配方括号及其内容
//        Pattern pattern = Pattern.compile("\\[(.*?)]");
//        Matcher matcher = pattern.matcher(sql);
//        while (matcher.find()) {
//            String bracketedContent = matcher.group(0);  // 包含方括号的内容，如"[%lk_username:=username]"
//            String innerContent = matcher.group(1);      // 方括号内的内容，如"%lk_username:=username"
//            JSONObject json = new JSONObject();
//            json.set("key", bracketedContent);
//            json.set("value", innerContent);
//            result.add(json);
//        }
//        return result;
//    }
//
//
//}
