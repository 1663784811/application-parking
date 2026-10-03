package com.cyyaw.admin.application.parking;

import com.cyyaw.admin.dao.parking.PkCostRulesDao;
import com.cyyaw.admin.entity.module.parking.PkCostRules;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 费率引擎单测。规则口径见 {@link CostUtil} 类注释。
 */
class CostUtilTest {

    private static final LocalDateTime ENTRY = LocalDateTime.of(2026, 3, 4, 10, 0); // 周三

    @Test
    @DisplayName("无规则时费用为 0")
    void noRules() {
        assertEquals(0, cost(60, Collections.emptyList()));
        assertEquals(0, cost(60, null));
    }

    @Test
    @DisplayName("首段阶梯：免费 15 分钟 + 首小时 5 元")
    void firstSegmentLadder() {
        List<PkCostRules> rules = Arrays.asList(
                firstSegment(15, "0.00"),
                firstSegment(60, "5.00"));

        assertEquals(0, cost(10, rules), "停 10 分钟在免费时长内");
        assertEquals(0, cost(15, rules), "整 15 分钟仍未超过起算点");
        assertEquals(5, cost(16, rules), "超过 15 分钟收首小时费");
        assertEquals(5, cost(40, rules), "停 40 分钟 = 首小时 5 元");
        assertEquals(5, cost(60, rules), "停满 1 小时仍只有首段费");
    }

    @Test
    @DisplayName("首段 + 计费时段：超出首段后按小时加收")
    void firstSegmentPlusPeriod() {
        List<PkCostRules> rules = Arrays.asList(
                firstSegment(15, "0.00"),
                firstSegment(60, "5.00"),
                period(60, "3.00"));

        assertEquals(8, cost(90, rules), "1.5 小时 = 首段 5 + 后续 1 小时 3");
        assertEquals(11, cost(121, rules), "2 小时零 1 分 = 首段 5 + 后续 1 小时 3*2（不足一小时按一小时）");
    }

    @Test
    @DisplayName("跨天夜间时段：22:00-06:00 按 2 元/小时")
    void nightWindow() {
        List<PkCostRules> rules = Arrays.asList(
                firstSegment(15, "0.00"),
                firstSegment(60, "5.00"),
                period(60, "3.00"),
                windowedPeriod(LocalTime.of(22, 0), LocalTime.of(6, 0), 60, "2.00"));

        // 20:00 入场，次日 00:30 出场：20:00-21:00 首段 5 元，
        // 21:00-22:00 白天时段 3 元，22:00-00:30 夜间 150 分钟 -> 3 个单位 * 2 = 6 元
        LocalDateTime entry = LocalDateTime.of(2026, 3, 4, 20, 0);
        assertEquals(14, compute(entry, entry.plusMinutes(270), rules));
    }

    @Test
    @DisplayName("每天封顶按自然日分别生效")
    void dailyCap() {
        List<PkCostRules> rules = Arrays.asList(
                firstSegment(0, "0.00"),
                period(60, "30.00"),
                dailyCap("50.00"));

        // 单日 5 小时 = 150 元，被封顶到 50
        assertEquals(50, cost(300, rules));
    }

    @Test
    @DisplayName("每次封顶在每天封顶之后应用")
    void sessionCap() {
        List<PkCostRules> rules = Arrays.asList(
                firstSegment(0, "0.00"),
                period(60, "30.00"),
                dailyCap("50.00"),
                sessionCap("80.00"));

        // 跨两天：每天 50 元封顶共 100，再被每次封顶压到 80
        LocalDateTime entry = LocalDateTime.of(2026, 3, 4, 23, 0);
        assertEquals(80, compute(entry, entry.plusHours(4), rules));
    }

    @Test
    @DisplayName("车型过滤在取规则时完成（PkCostRulesDao.matchCarType）")
    void carTypeFilter() {
        PkCostRules small = firstSegment(0, "5.00");
        PkCostRules big = firstSegment(0, "10.00");
        big.setCarType("2"); // 大型汽车
        List<PkCostRules> rules = Arrays.asList(small, big);

        assertEquals(5, compute(ENTRY, ENTRY.plusMinutes(30), PkCostRulesDao.matchCarType(rules, "0")),
                "小型车只走自己的规则");
        assertEquals(10, compute(ENTRY, ENTRY.plusMinutes(30), PkCostRulesDao.matchCarType(rules, "2")),
                "大型车走大型车规则");
        // pk_car_log.car_type 可空：车型未知时不过滤，让所有规则参与计费，
        // 否则限定车型的规则被全排除会导致费用恒为 0
        assertEquals(15, compute(ENTRY, ENTRY.plusMinutes(30), PkCostRulesDao.matchCarType(rules, null)),
                "车型未知时不按车型过滤");
        // 规则侧 car_type 为空视为不限车型：不限车型的规则对所有车生效
        PkCostRules anyType = firstSegment(0, "8.00");
        anyType.setCarType("");
        assertEquals(18, compute(ENTRY, ENTRY.plusMinutes(30), PkCostRulesDao.matchCarType(
                Arrays.asList(big, anyType), "2")), "不限车型规则 + 大型车规则合计 10 + 8");
    }

    @Test
    @DisplayName("week 不匹配的规则当天不参与计费")
    void weekFilter() {
        PkCostRules sundayOnly = firstSegment(0, "10.00");
        sundayOnly.setWeek("Sunday");
        List<PkCostRules> rules = Collections.singletonList(sundayOnly);

        // ENTRY 是 2026-03-04 周三
        assertEquals(0, compute(ENTRY, ENTRY.plusMinutes(30), rules));
        // 2026-03-08 是周日
        assertEquals(10, compute(LocalDateTime.of(2026, 3, 8, 10, 0),
                LocalDateTime.of(2026, 3, 8, 10, 30), rules));
    }

    @Test
    @DisplayName("规则有效期不含出场日期时不参与计费")
    void effectiveRangeFilter() {
        PkCostRules expired = firstSegment(0, "10.00");
        expired.setEffectiveEndTime(ENTRY.toLocalDate().minusDays(1));
        assertEquals(0, compute(ENTRY, ENTRY.plusMinutes(30), Collections.singletonList(expired)));

        PkCostRules future = firstSegment(0, "10.00");
        future.setEffectiveStartTime(ENTRY.toLocalDate().plusDays(1));
        assertEquals(0, compute(ENTRY, ENTRY.plusMinutes(30), Collections.singletonList(future)));
    }

    @Test
    @DisplayName("已删除的规则不参与计费")
    void deletedRule() {
        PkCostRules deleted = firstSegment(0, "10.00");
        deleted.setDelTime(1);
        assertEquals(0, compute(ENTRY, ENTRY.plusMinutes(30), Collections.singletonList(deleted)));
    }

    @Test
    @DisplayName("出场时间不晚于入场时间时费用为 0")
    void invalidRange() {
        List<PkCostRules> rules = Collections.singletonList(firstSegment(0, "10.00"));
        assertEquals(0, compute(ENTRY, ENTRY, rules));
        assertEquals(0, compute(ENTRY, ENTRY.minusMinutes(10), rules));
        assertEquals(0, CostUtil.computeCost(null, ENTRY, rules).getTotalAmount().intValueExact());
    }

    @Test
    @DisplayName("时长格式化")
    void durationText() {
        assertEquals("0分钟", CostUtil.formatDuration(ENTRY, ENTRY));
        assertEquals("45分钟", CostUtil.formatDuration(ENTRY, ENTRY.plusMinutes(45)));
        assertEquals("2小时", CostUtil.formatDuration(ENTRY, ENTRY.plusHours(2)));
        assertEquals("1小时30分", CostUtil.formatDuration(ENTRY, ENTRY.plusMinutes(90)));
    }

    // ==================== 测试辅助 ====================

    /** 返回 int 而非 BigDecimal，断言时才不会踩到 scale（5 与 5.00）比较的坑 */
    private static int cost(long minutes, List<PkCostRules> rules) {
        return compute(ENTRY, ENTRY.plusMinutes(minutes), rules);
    }

    private static int compute(LocalDateTime entry, LocalDateTime exit, List<PkCostRules> rules) {
        return CostUtil.computeCost(entry, exit, rules).getTotalAmount().intValueExact();
    }

    /** 首段收费档 */
    private static PkCostRules firstSegment(int ruleTime, String amount) {
        PkCostRules rule = base(CostUtil.TYPE_FIRST_SEGMENT, ruleTime, amount);
        return rule;
    }

    /** 全天计费时段 */
    private static PkCostRules period(int ruleTime, String amount) {
        return base(CostUtil.TYPE_TIME_PERIOD, ruleTime, amount);
    }

    /** 限定时间窗的计费时段 */
    private static PkCostRules windowedPeriod(LocalTime start, LocalTime end, int ruleTime, String amount) {
        PkCostRules rule = base(CostUtil.TYPE_TIME_PERIOD, ruleTime, amount);
        rule.setStartTime(start);
        rule.setEndTime(end);
        return rule;
    }

    private static PkCostRules dailyCap(String amount) {
        return base(CostUtil.TYPE_DAILY_CAP, null, amount);
    }

    private static PkCostRules sessionCap(String amount) {
        return base(CostUtil.TYPE_SESSION_CAP, null, amount);
    }

    private static PkCostRules base(int type, Integer ruleTime, String amount) {
        PkCostRules rule = new PkCostRules();
        rule.setType(type);
        rule.setRuleTime(ruleTime);
        rule.setAmount(new BigDecimal(amount));
        rule.setCarType("0"); // 默认小型汽车
        rule.setDelTime(0);
        return rule;
    }

    /** 保留：便于将来补多档阶梯的组合用例 */
    @SuppressWarnings("unused")
    private static List<PkCostRules> ladder(PkCostRules... rules) {
        return new ArrayList<>(Arrays.asList(rules));
    }

}
