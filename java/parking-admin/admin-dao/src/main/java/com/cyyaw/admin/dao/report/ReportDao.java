package com.cyyaw.admin.dao.report;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 营收报表聚合查询（跨 or_order ↔ or_order_pay）
 * <p>
 * 金额来自 or_order_pay（pay_status=2 支付成功），按支付方式拆分线上(支付宝/微信)/线下(银行卡/现金)；
 * 优惠、订单数来自 or_order（pay_status=2 已支付）。
 * 通过对 or_order_pay 按 order_id 预聚合为 pa 子查询，保证每个订单只关联一行，
 * 避免 or_order.discount_amount 在 JOIN 中被多次累加。
 * 日期范围用 DATE(pay_time) BETWEEN #{start} AND #{end}（含首尾，且无需 XML 转义）。
 */
public interface ReportDao {

    /**
     * 营收汇总（单行）：线上/线下/优惠/订单数；可选停车场过滤
     */
    @Select("<script>" +
            "SELECT " +
            "COALESCE(SUM(pa.onlineAmount), 0) AS onlineRevenue, " +
            "COALESCE(SUM(pa.offlineAmount), 0) AS offlineRevenue, " +
            "COALESCE(SUM(o.discount_amount), 0) AS discount, " +
            "COUNT(DISTINCT o.id) AS orderCount " +
            "FROM or_order o " +
            "LEFT JOIN ( " +
            "  SELECT order_id, " +
            "    SUM(CASE WHEN pay_type IN (1, 2) THEN pay_amount ELSE 0 END) AS onlineAmount, " +
            "    SUM(CASE WHEN pay_type IN (3, 4) THEN pay_amount ELSE 0 END) AS offlineAmount " +
            "  FROM or_order_pay WHERE pay_status = 2 GROUP BY order_id " +
            ") pa ON pa.order_id = o.id " +
            "WHERE o.pay_status = 2 AND DATE(o.pay_time) BETWEEN #{start} AND #{end} " +
            "<if test='parkingId != null'>AND o.store_id = #{parkingId} </if>" +
            "</script>")
    Map<String, Object> selectRevenueStats(@Param("start") String start, @Param("end") String end, @Param("parkingId") Long parkingId);

    /**
     * 日营收趋势（图表）：{ date(yyyy-MM-dd), amount }
     */
    @Select("<script>" +
            "SELECT DATE_FORMAT(o.pay_time, '%Y-%m-%d') AS `date`, COALESCE(SUM(pa.totalAmount), 0) AS amount " +
            "FROM or_order o " +
            "LEFT JOIN ( " +
            "  SELECT order_id, SUM(pay_amount) AS totalAmount FROM or_order_pay WHERE pay_status = 2 GROUP BY order_id " +
            ") pa ON pa.order_id = o.id " +
            "WHERE o.pay_status = 2 AND DATE(o.pay_time) BETWEEN #{start} AND #{end} " +
            "<if test='parkingId != null'>AND o.store_id = #{parkingId} </if>" +
            "GROUP BY DATE_FORMAT(o.pay_time, '%Y-%m-%d') ORDER BY `date`" +
            "</script>")
    List<Map<String, Object>> selectDailyRevenue(@Param("start") String start, @Param("end") String end, @Param("parkingId") Long parkingId);

    /**
     * 营收明细（按日，分页）：{ date, onlineAmount, offlineAmount, discountAmount, totalAmount, orderCount }
     */
    @Select("<script>" +
            "SELECT DATE_FORMAT(o.pay_time, '%Y-%m-%d') AS `date`, " +
            "COALESCE(SUM(pa.onlineAmount), 0) AS onlineAmount, " +
            "COALESCE(SUM(pa.offlineAmount), 0) AS offlineAmount, " +
            "COALESCE(SUM(o.discount_amount), 0) AS discountAmount, " +
            "COALESCE(SUM(pa.totalAmount), 0) AS totalAmount, " +
            "COUNT(DISTINCT o.id) AS orderCount " +
            "FROM or_order o " +
            "LEFT JOIN ( " +
            "  SELECT order_id, " +
            "    SUM(CASE WHEN pay_type IN (1, 2) THEN pay_amount ELSE 0 END) AS onlineAmount, " +
            "    SUM(CASE WHEN pay_type IN (3, 4) THEN pay_amount ELSE 0 END) AS offlineAmount, " +
            "    SUM(pay_amount) AS totalAmount " +
            "  FROM or_order_pay WHERE pay_status = 2 GROUP BY order_id " +
            ") pa ON pa.order_id = o.id " +
            "WHERE o.pay_status = 2 AND DATE(o.pay_time) BETWEEN #{start} AND #{end} " +
            "<if test='parkingId != null'>AND o.store_id = #{parkingId} </if>" +
            "GROUP BY DATE_FORMAT(o.pay_time, '%Y-%m-%d') ORDER BY `date` DESC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<Map<String, Object>> selectRevenueDetail(@Param("start") String start, @Param("end") String end, @Param("parkingId") Long parkingId, @Param("offset") int offset, @Param("size") int size);

    /**
     * 营收明细按日分组总数（分页 total）
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM ( " +
            "  SELECT DATE(o.pay_time) AS d FROM or_order o " +
            "  WHERE o.pay_status = 2 AND DATE(o.pay_time) BETWEEN #{start} AND #{end} " +
            "  <if test='parkingId != null'>AND o.store_id = #{parkingId} </if>" +
            "  GROUP BY DATE(o.pay_time) " +
            ") t" +
            "</script>")
    long selectRevenueDetailCount(@Param("start") String start, @Param("end") String end, @Param("parkingId") Long parkingId);

    // ==================== 车流量报表（pk_car_log） ====================

    /**
     * 车流量汇总（单行）：今日入场 / 今日出场 / 当前在场 / 今日入场峰值时段（hour 0-23，无数据为 null）
     */
    @Select("<script>" +
            "SELECT " +
            "(SELECT COUNT(*) FROM pk_car_log WHERE entry_time >= CURDATE() <if test='parkingId != null'>AND parking_id = #{parkingId} </if>) AS todayIn, " +
            "(SELECT COUNT(*) FROM pk_car_log WHERE out_time >= CURDATE() <if test='parkingId != null'>AND parking_id = #{parkingId} </if>) AS todayOut, " +
            "(SELECT COUNT(*) FROM pk_car_log WHERE `status` = 0 <if test='parkingId != null'>AND parking_id = #{parkingId} </if>) AS currentIn, " +
            "(SELECT HOUR(entry_time) FROM pk_car_log WHERE entry_time >= CURDATE() <if test='parkingId != null'>AND parking_id = #{parkingId} </if> GROUP BY HOUR(entry_time) ORDER BY COUNT(*) DESC LIMIT 1) AS peakHour" +
            "</script>")
    Map<String, Object> selectTrafficStats(@Param("parkingId") Long parkingId);

    /**
     * 今日分小时入场：{ hour(0-23), cnt }，无数据的小时由 service 补 0
     */
    @Select("<script>" +
            "SELECT HOUR(entry_time) AS `hour`, COUNT(*) AS cnt " +
            "FROM pk_car_log WHERE entry_time >= CURDATE() " +
            "<if test='parkingId != null'>AND parking_id = #{parkingId} </if>" +
            "GROUP BY HOUR(entry_time) ORDER BY `hour`" +
            "</script>")
    List<Map<String, Object>> selectHourlyIn(@Param("parkingId") Long parkingId);

    /**
     * 今日分小时出场：{ hour(0-23), cnt }
     */
    @Select("<script>" +
            "SELECT HOUR(out_time) AS `hour`, COUNT(*) AS cnt " +
            "FROM pk_car_log WHERE out_time >= CURDATE() " +
            "<if test='parkingId != null'>AND parking_id = #{parkingId} </if>" +
            "GROUP BY HOUR(out_time) ORDER BY `hour`" +
            "</script>")
    List<Map<String, Object>> selectHourlyOut(@Param("parkingId") Long parkingId);

    /**
     * 近7日每日入场：{ date(yyyy-MM-dd), cnt }，缺失日期由 service 补 0
     */
    @Select("<script>" +
            "SELECT DATE_FORMAT(entry_time, '%Y-%m-%d') AS `date`, COUNT(*) AS cnt " +
            "FROM pk_car_log WHERE entry_time >= CURDATE() - INTERVAL 6 DAY " +
            "<if test='parkingId != null'>AND parking_id = #{parkingId} </if>" +
            "GROUP BY DATE_FORMAT(entry_time, '%Y-%m-%d') ORDER BY `date`" +
            "</script>")
    List<Map<String, Object>> selectDailyIn(@Param("parkingId") Long parkingId);

    /**
     * 近7日每日出场：{ date(yyyy-MM-dd), cnt }
     */
    @Select("<script>" +
            "SELECT DATE_FORMAT(out_time, '%Y-%m-%d') AS `date`, COUNT(*) AS cnt " +
            "FROM pk_car_log WHERE out_time >= CURDATE() - INTERVAL 6 DAY " +
            "<if test='parkingId != null'>AND parking_id = #{parkingId} </if>" +
            "GROUP BY DATE_FORMAT(out_time, '%Y-%m-%d') ORDER BY `date`" +
            "</script>")
    List<Map<String, Object>> selectDailyOut(@Param("parkingId") Long parkingId);

    // ==================== 车位利用率报表（pk_space ↔ pk_car_log） ====================

    /**
     * 车位利用率汇总（单行）：总车位数（pk_space）+ 当前在场车辆（pk_car_log status=0）；可选停车场过滤
     */
    @Select("<script>" +
            "SELECT " +
            "(SELECT COUNT(*) FROM pk_space WHERE 1=1 <if test='parkingId != null'>AND parking_id = #{parkingId} </if>) AS totalSpaces, " +
            "(SELECT COUNT(*) FROM pk_car_log WHERE `status` = 0 <if test='parkingId != null'>AND parking_id = #{parkingId} </if>) AS currentIn" +
            "</script>")
    Map<String, Object> selectSpaceStats(@Param("parkingId") Long parkingId);

    /**
     * 今日分小时在场车辆：{ hour(0-23), inLot }，无数据的小时由 numbers 子查询补 0
     * <p>
     * 在场判定：entry_time 早于该小时末 且（未出场 或 out_time 不早于该小时初）
     */
    @Select("<script>" +
            "SELECT h.hh AS `hour`, COUNT(c.id) AS inLot " +
            "FROM ( " +
            "  SELECT 0 AS hh UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 " +
            "  UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10 " +
            "  UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15 " +
            "  UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20 " +
            "  UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 " +
            ") h " +
            "LEFT JOIN pk_car_log c ON c.entry_time &lt;= CONCAT(CURDATE(), ' ', LPAD(h.hh, 2, '0'), ':59:59') " +
            "  AND (c.out_time IS NULL OR c.out_time >= CONCAT(CURDATE(), ' ', LPAD(h.hh, 2, '0'), ':00:00')) " +
            "<if test='parkingId != null'>AND c.parking_id = #{parkingId} </if>" +
            "GROUP BY h.hh ORDER BY h.hh" +
            "</script>")
    List<Map<String, Object>> selectHourlyOccupancy(@Param("parkingId") Long parkingId);

    /**
     * 区域占用对比：{ area, total, occupied }，按区域分组
     */
    @Select("<script>" +
            "SELECT area, COUNT(*) AS total, SUM(CASE WHEN `status` = 1 THEN 1 ELSE 0 END) AS occupied " +
            "FROM pk_space WHERE area IS NOT NULL " +
            "<if test='parkingId != null'>AND parking_id = #{parkingId} </if>" +
            "GROUP BY area ORDER BY area" +
            "</script>")
    List<Map<String, Object>> selectAreaBreakdown(@Param("parkingId") Long parkingId);

    // ==================== 月卡营收报表（me_renewal_record） ====================

    /**
     * 月卡营收汇总（单行）：续费总金额 / 续费笔数 / 活跃会员数（member_id 去重）；
     * 日期范围按 renewal_time 过滤，可选卡类型过滤（card_type {1:月卡,2:季卡,3:年卡}）
     */
    @Select("<script>" +
            "SELECT " +
            "COALESCE(SUM(amount), 0) AS totalRevenue, " +
            "COUNT(*) AS renewalCount, " +
            "COUNT(DISTINCT member_id) AS memberCount " +
            "FROM me_renewal_record " +
            "WHERE DATE(renewal_time) BETWEEN #{start} AND #{end} " +
            "<if test='cardType != null'>AND card_type = #{cardType} </if>" +
            "</script>")
    Map<String, Object> selectMemberRevenueStats(@Param("start") String start, @Param("end") String end, @Param("cardType") Integer cardType);

    /**
     * 月卡营收按月趋势：{ month(yyyy-MM), revenue, count }，按月升序
     */
    @Select("<script>" +
            "SELECT DATE_FORMAT(renewal_time, '%Y-%m') AS `month`, " +
            "COALESCE(SUM(amount), 0) AS revenue, " +
            "COUNT(*) AS `count` " +
            "FROM me_renewal_record " +
            "WHERE DATE(renewal_time) BETWEEN #{start} AND #{end} " +
            "<if test='cardType != null'>AND card_type = #{cardType} </if>" +
            "GROUP BY DATE_FORMAT(renewal_time, '%Y-%m') ORDER BY `month`" +
            "</script>")
    List<Map<String, Object>> selectMemberRevenueMonthly(@Param("start") String start, @Param("end") String end, @Param("cardType") Integer cardType);

    /**
     * 会员营收明细（按会员聚合，分页）：{ memberId, name, plate, cardType, renewalCount, totalAmount }
     */
    @Select("<script>" +
            "SELECT member_id AS memberId, " +
            "MAX(name) AS name, MAX(plate) AS plate, MAX(card_type) AS cardType, " +
            "COUNT(*) AS renewalCount, COALESCE(SUM(amount), 0) AS totalAmount " +
            "FROM me_renewal_record " +
            "WHERE DATE(renewal_time) BETWEEN #{start} AND #{end} " +
            "<if test='cardType != null'>AND card_type = #{cardType} </if>" +
            "GROUP BY member_id ORDER BY totalAmount DESC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<Map<String, Object>> selectMemberRevenueDetail(@Param("start") String start, @Param("end") String end, @Param("cardType") Integer cardType, @Param("offset") int offset, @Param("size") int size);

    /**
     * 会员营收明细按会员分组总数（分页 total）
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM ( " +
            "  SELECT member_id FROM me_renewal_record " +
            "  WHERE DATE(renewal_time) BETWEEN #{start} AND #{end} " +
            "  <if test='cardType != null'>AND card_type = #{cardType} </if>" +
            "  GROUP BY member_id " +
            ") t" +
            "</script>")
    long selectMemberRevenueDetailCount(@Param("start") String start, @Param("end") String end, @Param("cardType") Integer cardType);

    /**
     * 订单对账汇总：{ totalRevenue, totalOrders, onlineRevenue, onlineCount, offlineRevenue, offlineCount }
     * 线上=支付宝/微信(pay_type 1,2)，线下=银行卡/现金(pay_type 3,4)，仅统计支付成功
     */
    @Select("<script>" +
            "SELECT " +
            "COALESCE(SUM(pa.totalAmount), 0) AS totalRevenue, " +
            "COUNT(DISTINCT o.id) AS totalOrders, " +
            "COALESCE(SUM(pa.onlineAmount), 0) AS onlineRevenue, " +
            "COUNT(DISTINCT CASE WHEN pa.onlineAmount > 0 THEN o.id END) AS onlineCount, " +
            "COALESCE(SUM(pa.offlineAmount), 0) AS offlineRevenue, " +
            "COUNT(DISTINCT CASE WHEN pa.offlineAmount > 0 THEN o.id END) AS offlineCount " +
            "FROM or_order o " +
            "LEFT JOIN ( " +
            "  SELECT order_id, " +
            "    SUM(CASE WHEN pay_type IN (1, 2) THEN pay_amount ELSE 0 END) AS onlineAmount, " +
            "    SUM(CASE WHEN pay_type IN (3, 4) THEN pay_amount ELSE 0 END) AS offlineAmount, " +
            "    SUM(pay_amount) AS totalAmount " +
            "  FROM or_order_pay WHERE pay_status = 2 GROUP BY order_id " +
            ") pa ON pa.order_id = o.id " +
            "WHERE o.pay_status = 2 AND DATE(o.pay_time) BETWEEN #{start} AND #{end} " +
            "<if test='parkingId != null'>AND o.store_id = #{parkingId} </if>" +
            "</script>")
    Map<String, Object> selectReconcileStats(@Param("start") String start, @Param("end") String end, @Param("parkingId") Long parkingId);

    /**
     * 订单对账明细（按停车场+对账日聚合，分页）：{ parkingId, parkingName, reconcileDate, onlineOrders, onlineAmount, offlineOrders, offlineAmount, totalAmount, diffAmount }
     */
    @Select("<script>" +
            "SELECT " +
            "o.store_id AS parkingId, " +
            "MAX(p.name) AS parkingName, " +
            "DATE_FORMAT(o.pay_time, '%Y-%m-%d') AS reconcileDate, " +
            "COUNT(DISTINCT CASE WHEN pa.onlineAmount > 0 THEN o.id END) AS onlineOrders, " +
            "COALESCE(SUM(pa.onlineAmount), 0) AS onlineAmount, " +
            "COUNT(DISTINCT CASE WHEN pa.offlineAmount > 0 THEN o.id END) AS offlineOrders, " +
            "COALESCE(SUM(pa.offlineAmount), 0) AS offlineAmount, " +
            "COALESCE(SUM(pa.totalAmount), 0) AS totalAmount, " +
            "(COALESCE(SUM(pa.totalAmount), 0) - COALESCE(SUM(pa.onlineAmount), 0) - COALESCE(SUM(pa.offlineAmount), 0)) AS diffAmount " +
            "FROM or_order o " +
            "LEFT JOIN ( " +
            "  SELECT order_id, " +
            "    SUM(CASE WHEN pay_type IN (1, 2) THEN pay_amount ELSE 0 END) AS onlineAmount, " +
            "    SUM(CASE WHEN pay_type IN (3, 4) THEN pay_amount ELSE 0 END) AS offlineAmount, " +
            "    SUM(pay_amount) AS totalAmount " +
            "  FROM or_order_pay WHERE pay_status = 2 GROUP BY order_id " +
            ") pa ON pa.order_id = o.id " +
            "LEFT JOIN pk_parking p ON p.id = o.store_id " +
            "WHERE o.pay_status = 2 AND DATE(o.pay_time) BETWEEN #{start} AND #{end} " +
            "<if test='parkingId != null'>AND o.store_id = #{parkingId} </if>" +
            "GROUP BY o.store_id, DATE_FORMAT(o.pay_time, '%Y-%m-%d') " +
            "ORDER BY reconcileDate DESC, o.store_id " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<Map<String, Object>> selectReconcileDetail(@Param("start") String start, @Param("end") String end, @Param("parkingId") Long parkingId, @Param("offset") int offset, @Param("size") int size);

    /**
     * 订单对账明细按停车场+日分组总数（分页 total）
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM ( " +
            "  SELECT o.store_id, DATE(o.pay_time) AS d " +
            "  FROM or_order o " +
            "  WHERE o.pay_status = 2 AND DATE(o.pay_time) BETWEEN #{start} AND #{end} " +
            "  <if test='parkingId != null'>AND o.store_id = #{parkingId} </if>" +
            "  GROUP BY o.store_id, DATE(o.pay_time) " +
            ") t" +
            "</script>")
    long selectReconcileDetailCount(@Param("start") String start, @Param("end") String end, @Param("parkingId") Long parkingId);
}
