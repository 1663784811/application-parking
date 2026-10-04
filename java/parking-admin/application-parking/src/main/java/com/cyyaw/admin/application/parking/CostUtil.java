package com.cyyaw.admin.application.parking;

import com.cyyaw.admin.entity.module.parking.PkCostRules;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;

/**
 * 停车费计算。
 * <p>
 * 规则来自停车场配置的 {@link PkCostRules} 列表（一条规则可被多个停车场复用），
 * 按收费类型分四步计算：
 * <ol>
 *   <li><b>首段收费(type=0)</b>：阶梯，可配多档，按 {@code ruleTime} 升序。
 *       第 i 档的起算点 = 第 i-1 档的 {@code ruleTime}（首档为 0），
 *       停车时长超过起算点则累加该档 {@code amount}。
 *       于是「免费 15 分钟」= {@code ruleTime=15, amount=0}，
 *       「首小时 5 元」= {@code ruleTime=60, amount=5}：停 10 分钟收 0 元、停 40 分钟收 5 元。</li>
 *   <li><b>计费时段(type=2)</b>：超出首段覆盖时长（最高档的 {@code ruleTime}）的分钟进入此步。
 *       未设 {@code start_time/end_time} 表示全天适用；设了则只对该时段内的分钟计费（支持跨天，如 22:00–06:00）。
 *       每个规则对落在自己时段内的分钟数按 {@code ceil(分钟数 / ruleTime) * amount} 收费。</li>
 *   <li><b>每天封顶(type=3)</b>：按自然日切分，每天取 {@code min(当天费用, amount)} 再求和。</li>
 *   <li><b>每次封顶(type=5)</b>：总费用取 {@code min(总费用, amount)}。</li>
 * </ol>
 * 封顶在其余计费之后应用，每天封顶先于每次封顶。没有任何规则命中则费用为 0。
 * <p>
 * 说明：金额单位与 {@code pk_cost_rules.amount} 一致（元，两位小数）；时长不足一分钟按一分钟计。
 * <p>
 * {@link #computeCost} 除金额外还产出 {@link ParkingCostDetails} 逐段明细，用于向车主展示
 * "这笔钱是怎么算出来的"。封顶（每天/每次）在明细之上削减金额，不产生明细，
 * 故封顶生效时明细金额之和会大于 {@code totalAmount}。
 */
public class CostUtil {

    /** 收费类型：首段收费 */
    public static final int TYPE_FIRST_SEGMENT = 0;
    /** 收费类型：计费时段 */
    public static final int TYPE_TIME_PERIOD = 2;
    /** 收费类型：每天封顶金额 */
    public static final int TYPE_DAILY_CAP = 3;
    /** 收费类型：每次封顶金额 */
    public static final int TYPE_SESSION_CAP = 5;

    /**
     * 计算停车费用，返回金额与逐段收费明细。
     *
     * @param entryTime 入场时间
     * @param exitTime  出场时间
     * @param rules     本次参与计费的收费规则，可为空
     * @return 结算结果，恒不为 null；入参非法或无命中规则时 {@code totalAmount} 为 0、明细为空
     */
    public static ParkingCost computeCost(LocalDateTime entryTime, LocalDateTime exitTime, List<PkCostRules> rules) {
        ParkingCost result = new ParkingCost();
        if (entryTime == null || exitTime == null || !exitTime.isAfter(entryTime)) {
            result.setTotalAmount(BigDecimal.ZERO);
            return result;
        }
        // 出场时间为参考点：规则有效期、星期都按它筛，不按入场时间
        List<PkCostRules> matched = filterRules(rules, exitTime);
        if (matched.isEmpty()) {
            result.setTotalAmount(BigDecimal.ZERO);
            return result;
        }

        // 总时长（分钟），整除前 +59 即向上取整：不足一分钟也按一分钟
        long totalMinutes = (Duration.between(entryTime, exitTime).getSeconds() + 59) / 60;

        // ---- 首段收费：阶梯累加，起算点取上一档的 ruleTime ----
        List<PkCostRules> segments = new ArrayList<>();
        for (PkCostRules rule : matched) {
            if (isType(rule, TYPE_FIRST_SEGMENT)) {
                segments.add(rule);
            }
        }
        segments.sort(Comparator.comparingLong(r -> minutes(r.getRuleTime())));

        // 首段只对入场当天生效，且该天的星期须命中规则
        LocalDate entryDate = entryTime.toLocalDate();
        BigDecimal firstSegmentCost = BigDecimal.ZERO;
        // coveredMinutes：首段覆盖到第几分钟，之后的分钟才进计费时段。
        // 注意这里即使本档星期不命中也照填 —— 时长被"覆盖"是事实，只是不收费。
        long coveredMinutes = 0;
        long previousRuleTime = 0;
        // 首段每命中一档产出一条明细，区间落在它覆盖的时长范围内
        List<ParkingCostDetails> details = new ArrayList<>();
        for (PkCostRules segment : segments) {
            long ruleTime = minutes(segment.getRuleTime());
            if (weekMatches(segment, entryDate) && totalMinutes > previousRuleTime) {
                firstSegmentCost = firstSegmentCost.add(amount(segment));
                details.add(buildDetail(segment, entryTime.plusMinutes(previousRuleTime), entryTime.plusMinutes(ruleTime)));
            }
            previousRuleTime = ruleTime;
            coveredMinutes = ruleTime;
        }

        // ---- 计费时段：逐分钟归类到命中的时段规则，再按每个规则的分钟数计费 ----
        List<PkCostRules> periodRules = new ArrayList<>();
        for (PkCostRules rule : matched) {
            if (isType(rule, TYPE_TIME_PERIOD)) {
                periodRules.add(rule);
            }
        }
        // 逐分钟归类：i 是相对入场时刻的分钟偏移，每个分钟只归到一个规则（时段不重叠计费）。
        // LinkedHashMap 保序，后续按自然日汇总时顺序稳定。
        Map<LocalDate, Map<PkCostRules, List<Long>>> periodMinutes = new LinkedHashMap<>();
        for (long i = coveredMinutes; i < totalMinutes; i++) {
            LocalDateTime moment = entryTime.plusMinutes(i);
            LocalDate date = moment.toLocalDate();
            PkCostRules hit = pickPeriodRule(periodRules, date, moment.toLocalTime());
            if (hit == null) {
                // 该分钟没有命中任何时段规则：不计费也不归入任何一天
                continue;
            }
            periodMinutes.computeIfAbsent(date, k -> new LinkedHashMap<>()).computeIfAbsent(hit, k -> new ArrayList<>()).add(i);
        }

        // ---- 按自然日汇总，并在每天应用「每天封顶」 ----
        // 每天封顶按天独立生效，跨天的两次封顶互不影响，故先逐日封顶再累加。
        BigDecimal dailyCap = minAmount(matched, TYPE_DAILY_CAP);
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<LocalDate, Map<PkCostRules, List<Long>>> dayEntry : periodMinutes.entrySet()) {
            // 首段费只归入场当天；后续自然日只计时段费
            BigDecimal dayCost = dayEntry.getKey().equals(entryDate) ? firstSegmentCost : BigDecimal.ZERO;
            for (Map.Entry<PkCostRules, List<Long>> ruleEntry : dayEntry.getValue().entrySet()) {
                long mins = ruleEntry.getValue().size();
                dayCost = dayCost.add(periodCost(ruleEntry.getKey(), mins));
                // 同一天同一规则可能落在多个不连续的时间窗内，区间用「实际起止」而非逐段
                List<Long> offsets = ruleEntry.getValue();
                details.add(buildDetail(ruleEntry.getKey(), entryTime.plusMinutes(offsets.get(0)), entryTime.plusMinutes(offsets.get(offsets.size() - 1) + 1)));
            }
            if (dailyCap != null && dayCost.compareTo(dailyCap) > 0) {
                dayCost = dailyCap;
            }
            total = total.add(dayCost);
        }
        // 首段费可能落在没有任何计费时段分钟的当天（例如免费放行、或全部时长被首段覆盖），
        // 那种情况该天不会进入上面那个 map，这里单独补算并同样受每天封顶约束
        if (!periodMinutes.containsKey(entryDate)) {
            BigDecimal dayCost = firstSegmentCost;
            if (dailyCap != null && dayCost.compareTo(dailyCap) > 0) {
                dayCost = dailyCap;
            }
            total = total.add(dayCost);
        }

        // ---- 每次封顶 ----
        BigDecimal sessionCap = minAmount(matched, TYPE_SESSION_CAP);
        if (sessionCap != null && total.compareTo(sessionCap) > 0) {
            total = sessionCap;
        }
        result.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
        result.setParkingCostDetails(details);
        return result;
    }

    /**
     * 构造一条收费明细。金额为 0 的区间（如免费时长档）也保留，便于展示"为什么没收费"。
     */
    private static ParkingCostDetails buildDetail(PkCostRules rule, LocalDateTime start, LocalDateTime end) {
        ParkingCostDetails detail = new ParkingCostDetails();
        detail.setRuleId(rule.getId());
        detail.setRuleName(rule.getName());
        detail.setStartTime(Date.from(start.atZone(ZoneId.systemDefault()).toInstant()));
        detail.setEndTime(Date.from(end.atZone(ZoneId.systemDefault()).toInstant()));
        detail.setAmount(amount(rule));
        return detail;
    }

    /**
     * 判断给定时刻是否存在适用的规则。
     * <p>
     * 供看板区分「真 0 元」（例如免费时段）与「压根没有适用费率」——
     * 后者 {@link #computeCost} 同样返回 0，但展示成 "¥0.00" 会误导人。
     *
     * @param rules    本次参与计费的收费规则，可为空
     * @param exitTime 参考时刻（筛选规则有效期用）
     */
    public static boolean hasApplicableRules(List<PkCostRules> rules, LocalDateTime exitTime) {
        if (exitTime == null) {
            return false;
        }
        return !filterRules(rules, exitTime).isEmpty();
    }

    /**
     * 把停车时长格式化成「X小时Y分」，用于显示屏播报。
     */
    public static String formatDuration(LocalDateTime entryTime, LocalDateTime exitTime) {
        if (entryTime == null || exitTime == null || !exitTime.isAfter(entryTime)) {
            return "0分钟";
        }
        long minutes = (Duration.between(entryTime, exitTime).getSeconds() + 59) / 60;
        long hours = minutes / 60;
        long rest = minutes % 60;
        if (hours <= 0) {
            return rest + "分钟";
        }
        if (rest == 0) {
            return hours + "小时";
        }
        return hours + "小时" + rest + "分";
    }

    /**
     * 全局筛选：未删除、规则有效期覆盖出场日期。
     * 星期(week)不在此处过滤 —— 它按自然日逐天判定，见 {@link #weekMatches}。
     * 车型不再过滤：规则按 {@code pk_cost_rules.car_type} 区分车型，由调用方在取规则时
     * 按本次车辆类型筛好再传入。
     */
    private static List<PkCostRules> filterRules(List<PkCostRules> rules, LocalDateTime exitTime) {
        List<PkCostRules> matched = new ArrayList<>();
        if (rules == null) {
            return matched;
        }
        LocalDate exitDate = exitTime.toLocalDate();
        for (PkCostRules rule : rules) {
            if (rule == null || rule.getType() == null) {
                continue;
            }
            if (rule.getDelTime() != null && rule.getDelTime() != 0) {
                continue;
            }
            if (rule.getEffectiveStartTime() != null && exitDate.isBefore(rule.getEffectiveStartTime())) {
                continue;
            }
            if (rule.getEffectiveEndTime() != null && exitDate.isAfter(rule.getEffectiveEndTime())) {
                continue;
            }
            matched.add(rule);
        }
        return matched;
    }

    /**
     * 判定规则的适用星期是否包含指定日期。week 为空表示每天适用，
     * 否则为逗号分隔的英文星期（如 {@code Monday,Tuesday}）。
     */
    private static boolean weekMatches(PkCostRules rule, LocalDate date) {
        String week = rule.getWeek();
        if (week == null || week.isBlank()) {
            return true;
        }
        String today = date.getDayOfWeek().name();
        for (String item : week.split(",")) {
            if (today.equalsIgnoreCase(item.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 挑出某一时刻命中的计费时段规则：先匹配设了时间窗的规则，都不中再退回全天规则。
     * 同一天内有多个窗口命中时取第一个。
     */
    private static PkCostRules pickPeriodRule(List<PkCostRules> periodRules, LocalDate date, LocalTime time) {
        PkCostRules allDay = null;
        for (PkCostRules rule : periodRules) {
            if (!weekMatches(rule, date)) {
                continue;
            }
            LocalTime start = rule.getStartTime();
            LocalTime end = rule.getEndTime();
            if (start == null || end == null) {
                // 未设时间窗 = 全天适用，优先级低于设了窗口的规则
                if (allDay == null) {
                    allDay = rule;
                }
                continue;
            }
            if (inWindow(start, end, time)) {
                return rule;
            }
        }
        return allDay;
    }

    /**
     * 判断时刻是否落在 [start, end) 内。start 晚于 end 表示跨天窗口（如 22:00–06:00）；
     * start 与 end 相同视为全天。
     */
    private static boolean inWindow(LocalTime start, LocalTime end, LocalTime time) {
        if (start.equals(end)) {
            return true;
        }
        if (start.isBefore(end)) {
            return !time.isBefore(start) && time.isBefore(end);
        }
        return !time.isBefore(start) || time.isBefore(end);
    }

    /**
     * 单个计费时段规则对给定分钟数的收费：不足一个计费单位按一个单位算。
     */
    private static BigDecimal periodCost(PkCostRules rule, long minutes) {
        if (minutes <= 0) {
            return BigDecimal.ZERO;
        }
        long unit = minutes(rule.getRuleTime());
        if (unit <= 0) {
            // 没配计费单位时长，整段按一次收费
            return amount(rule);
        }
        long units = (minutes + unit - 1) / unit;
        return amount(rule).multiply(BigDecimal.valueOf(units));
    }

    /**
     * 取某类封顶规则中最低的封顶金额，没有则返回 null。
     * <p>
     * 配了多条封顶规则时取最低值，避免运营端配置重复/历史遗留规则导致封顶虚高。
     */
    private static BigDecimal minAmount(List<PkCostRules> matched, int type) {
        BigDecimal min = null;
        for (PkCostRules rule : matched) {
            if (!isType(rule, type)) {
                continue;
            }
            BigDecimal value = amount(rule);
            if (min == null || value.compareTo(min) < 0) {
                min = value;
            }
        }
        return min;
    }

    private static boolean isType(PkCostRules rule, int type) {
        return rule.getType() != null && rule.getType() == type;
    }

    /** ruleTime 为 null 或负数时按 0 处理 */
    private static long minutes(Integer ruleTime) {
        return ruleTime == null || ruleTime < 0 ? 0 : ruleTime;
    }

    /** amount 为 null 时按 0 处理 */
    private static BigDecimal amount(PkCostRules rule) {
        return rule.getAmount() == null ? BigDecimal.ZERO : rule.getAmount();
    }

}
