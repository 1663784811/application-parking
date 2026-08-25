package com.cyyaw.admin.dao.dashboard;

import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 工作台聚合查询 DAO（跨 or_order / pk_car_log / pk_parking 三表统计，纯 @Select，返回 Map）。
 * 由 @MapperScan("com.cyyaw.admin.dao.**") 自动注册，无需 @Mapper。
 */
public interface DashboardDao {

    /**
     * 核心指标（单行多列）：
     * 今日营收 / 昨日营收 / 当前在场车辆 / 总车位 / 今日进场 / 今日出场 / 未支付订单数 / 无牌在场车数。
     * 营收同比、车位利用率由 service 层计算。
     */
    @Select("SELECT " +
            "(SELECT COALESCE(SUM(pay_amount), 0) FROM or_order WHERE pay_status = 2 AND pay_time >= CURDATE()) AS todayRevenue, " +
            "(SELECT COALESCE(SUM(pay_amount), 0) FROM or_order WHERE pay_status = 2 AND pay_time >= CURDATE() - INTERVAL 1 DAY AND pay_time < CURDATE()) AS yesterdayRevenue, " +
            "(SELECT COUNT(*) FROM pk_car_log WHERE `status` = 0) AS currentVehicles, " +
            "(SELECT COALESCE(SUM(capacity), 0) FROM pk_parking) AS totalSpaces, " +
            "(SELECT COUNT(*) FROM pk_car_log WHERE entry_time >= CURDATE()) AS todayIn, " +
            "(SELECT COUNT(*) FROM pk_car_log WHERE out_time >= CURDATE()) AS todayOut, " +
            "(SELECT COUNT(*) FROM or_order WHERE pay_status IN (0, 1)) AS unpaidCount, " +
            "(SELECT COUNT(*) FROM pk_car_log WHERE `status` = 0 AND (car_number IS NULL OR car_number = '')) AS noPlateCount")
    Map<String, Object> selectStats();

    /**
     * 近7日营收趋势：{ date(yyyy-MM-dd), amount }
     */
    @Select("SELECT DATE_FORMAT(pay_time, '%Y-%m-%d') AS `date`, COALESCE(SUM(pay_amount), 0) AS amount " +
            "FROM or_order " +
            "WHERE pay_status = 2 AND pay_time >= CURDATE() - INTERVAL 6 DAY " +
            "GROUP BY DATE_FORMAT(pay_time, '%Y-%m-%d') " +
            "ORDER BY `date`")
    List<Map<String, Object>> selectRevenueTrend();

    /**
     * 近7日进场趋势：{ date(yyyy-MM-dd), cnt }
     */
    @Select("SELECT DATE_FORMAT(entry_time, '%Y-%m-%d') AS `date`, COUNT(*) AS cnt " +
            "FROM pk_car_log " +
            "WHERE entry_time >= CURDATE() - INTERVAL 6 DAY " +
            "GROUP BY DATE_FORMAT(entry_time, '%Y-%m-%d') " +
            "ORDER BY `date`")
    List<Map<String, Object>> selectInTrend();

    /**
     * 近7日出场趋势：{ date(yyyy-MM-dd), cnt }
     * 出场按 out_time 归日，与进场(entry_time)分开统计后由 service 合并补齐7日。
     */
    @Select("SELECT DATE_FORMAT(out_time, '%Y-%m-%d') AS `date`, COUNT(*) AS cnt " +
            "FROM pk_car_log " +
            "WHERE out_time >= CURDATE() - INTERVAL 6 DAY " +
            "GROUP BY DATE_FORMAT(out_time, '%Y-%m-%d') " +
            "ORDER BY `date`")
    List<Map<String, Object>> selectOutTrend();
}
