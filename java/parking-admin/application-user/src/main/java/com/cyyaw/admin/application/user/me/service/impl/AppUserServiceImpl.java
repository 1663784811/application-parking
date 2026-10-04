package com.cyyaw.admin.application.user.me.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cyyaw.admin.application.user.me.service.AppUserService;
import com.cyyaw.admin.dao.member.MeMemberDao;
import com.cyyaw.admin.dao.member.MePackageDao;
import com.cyyaw.admin.dao.or.OrCouponDao;
import com.cyyaw.admin.dao.or.OrOrderDao;
import com.cyyaw.admin.dao.parking.PkCarLogDao;
import com.cyyaw.admin.dao.parking.PkParkingDao;
import com.cyyaw.admin.dao.user.AppDefaultVehicleDao;
import com.cyyaw.admin.entity.dto.user.AppCardVO;
import com.cyyaw.admin.entity.dto.user.AppCouponVO;
import com.cyyaw.admin.entity.dto.user.AppOrderVO;
import com.cyyaw.admin.entity.dto.user.AppUserBoardVO;
import com.cyyaw.admin.entity.dto.user.AppVehicleVO;
import com.cyyaw.admin.entity.module.member.MeMember;
import com.cyyaw.admin.entity.module.member.MePackage;
import com.cyyaw.admin.entity.module.or.OrCoupon;
import com.cyyaw.admin.entity.module.parking.PkParking;
import com.cyyaw.admin.entity.module.user.AppDefaultVehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * H5「我的」页面实现。
 * <p>
 * 归属范围 = 企业 enId + 我绑定的车牌集合（来自 app_default_vehicle）。
 * 订单、停车次数、会员卡全部按车牌关联，不按 or_order.user_id —— 入场建单时没有登录态，
 * 历史订单的 user_id / app_id 全是空的，按 user_id 过滤会漏掉所有未付款的单子。
 */
@Service
public class AppUserServiceImpl implements AppUserService {

    /** 订单列表上限：前端是滚动加载，服务端先兜个上限，别把全表捞回来 */
    private static final int ORDER_LIMIT = 100;

    /** 卡类型文案 */
    private static final Map<Integer, String> CARD_TYPE_NAME = Map.of(1, "月卡", 2, "季卡", 3, "年卡");

    /** 卡类型对应的卡片主色（前端渐变底用） */
    private static final Map<Integer, String> CARD_TYPE_COLOR = Map.of(1, "#10b981", 2, "#f59e0b", 3, "#8b5cf6");

    @Autowired
    private AppDefaultVehicleDao appDefaultVehicleDao;

    @Autowired
    private MeMemberDao meMemberDao;

    @Autowired
    private MePackageDao mePackageDao;

    @Autowired
    private OrCouponDao orCouponDao;

    @Autowired
    private OrOrderDao orOrderDao;

    @Autowired
    private PkCarLogDao pkCarLogDao;

    @Autowired
    private PkParkingDao pkParkingDao;

    @Override
    public List<AppVehicleVO> vehicleList(Long enId, Long userId, String phone) {
        List<AppDefaultVehicle> vehicles = myVehicles(enId, userId, phone);
        if (vehicles.isEmpty()) {
            return Collections.emptyList();
        }

        // 每辆车的停车次数：一次 group by 查回来，按车牌建索引
        Map<String, Long> timesByPlate = plateTimes(enId, vehicles.stream()
                .map(AppDefaultVehicle::getPlate).filter(p -> p != null && !p.isBlank()).collect(Collectors.toList()));

        List<AppVehicleVO> result = new ArrayList<>(vehicles.size());
        for (AppDefaultVehicle v : vehicles) {
            AppVehicleVO vo = new AppVehicleVO();
            vo.setId(v.getId());
            vo.setPlateNumber(v.getPlate());
            vo.setVehicleType(v.getVehicleType());
            vo.setIsDefault(Integer.valueOf(1).equals(v.getIsDefault()));
            vo.setParkingTimes(timesByPlate.getOrDefault(v.getPlate(), 0L));
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<AppCouponVO> couponList(Long enId, Long appId) {
        return orCouponDao.selectByOwner(enId, appId).stream()
                .map(this::toCouponVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppOrderVO> orderList(Long enId, Long userId, String phone) {
        List<Map<String, Object>> rows = orOrderDao.selectByPlates(enId, myPlates(enId, userId, phone), ORDER_LIMIT);
        if (rows.isEmpty()) {
            return Collections.emptyList();
        }
        return rows.stream().map(this::toOrderVO).collect(Collectors.toList());
    }

    @Override
    public AppUserBoardVO board(Long enId, Long appId, Long userId, String phone) {
        List<String> plates = myPlates(enId, userId, phone);

        AppUserBoardVO vo = new AppUserBoardVO();
        vo.setIsVip(false);

        // 会员：按车牌找有效期内、未冻结的会员卡，取到期最晚的一张
        MeMember activeCard = findActiveCard(plates);
        if (activeCard != null) {
            vo.setIsVip(true);
            vo.setMemberType(CARD_TYPE_NAME.getOrDefault(activeCard.getCardType(), "会员卡"));
            vo.setMemberExpireTime(activeCard.getExpireTime());
        }

        // 停车统计：累计次数 + 累计小时数，口径与车辆列表的车牌次数一致
        Map<String, Long> timesByPlate = plateTimes(enId, plates);
        vo.setParkingTimes(timesByPlate.values().stream().mapToLong(Long::longValue).sum());
        BigDecimal parkingHours = toHours(totalSeconds(pkCarLogDao.selectTotalOutSeconds(enId, plates)));
        vo.setParkingHours(parkingHours);
        // 展示文案：非整数带一位小数，整数不带
        vo.setParkingDuration(money(parkingHours) + "小时");

        vo.setCouponCount((long) orCouponDao.selectByOwner(enId, appId).size());
        Long orderCount = orOrderDao.selectCountByPlates(enId, plates);
        vo.setOrderCount(orderCount == null ? 0L : orderCount);
        vo.setVehicleCount((long) myVehicles(enId, userId, phone).size());
        vo.setCardCount(cardCount(plates));

        vo.setCouponList(couponList(enId, appId));
        vo.setOrderList(orderList(enId, userId, phone));
        vo.setVehicleList(vehicleList(enId, userId, phone));
        vo.setCardList(cardList(plates));

        return vo;
    }

    // ==================== 车辆归属 ====================

    /**
     * 我的车辆列表。userId 有值走账号维度，否则退回手机号维度；del_time = 0 是硬条件。
     */
    private List<AppDefaultVehicle> myVehicles(Long enId, Long userId, String phone) {
        QueryWrapper<AppDefaultVehicle> wrapper = new QueryWrapper<>();
        wrapper.eq("del_time", 0);
        if (enId != null) {
            wrapper.eq("en_id", enId);
        }
        if (userId != null) {
            wrapper.eq("user_id", userId);
        } else {
            wrapper.eq("phone", phone);
        }
        // 默认车辆排最前，其余按登记时间倒序
        wrapper.orderByDesc("is_default").orderByDesc("create_time");
        return appDefaultVehicleDao.selectList(wrapper);
    }

    /** 我的车牌集合，去空去重。 */
    private List<String> myPlates(Long enId, Long userId, String phone) {
        return myVehicles(enId, userId, phone).stream()
                .map(AppDefaultVehicle::getPlate)
                .filter(p -> p != null && !p.isBlank())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    // ==================== 停车统计 ====================

    /**
     * 按车牌聚合「已出场」次数。plates 为空时返回空 Map ——
     * 没绑定任何车辆时头部统计就该是 0，别退化成企业维度把别的车主的次数算进来。
     */
    private Map<String, Long> plateTimes(Long enId, List<String> plates) {
        if (plates == null || plates.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Long> result = new LinkedHashMap<>();
        for (Map<String, Object> row : pkCarLogDao.selectPlateTimes(enId, plates)) {
            Object plate = row.get("carNumber");
            if (plate == null) {
                continue;
            }
            result.put(plate.toString(), toLong(row.get("timesCount")));
        }
        return result;
    }

    private long totalSeconds(Map<String, Object> row) {
        return row == null ? 0L : toLong(row.get("seconds"));
    }

    /** 秒转小时，保留 1 位小数 */
    private BigDecimal toHours(long seconds) {
        return BigDecimal.valueOf(seconds).divide(BigDecimal.valueOf(3600), 1, RoundingMode.HALF_UP);
    }

    // ==================== 会员卡 / 卡包 ====================

    /** 找有效期最晚的一张未冻结会员卡。 */
    private MeMember findActiveCard(List<String> plates) {
        if (plates.isEmpty()) {
            return null;
        }
        QueryWrapper<MeMember> wrapper = new QueryWrapper<>();
        wrapper.eq("del_time", 0)
                .eq("frozen", 0)
                .in("plate", plates)
                .ge("expire_time", LocalDateTime.now());
        wrapper.orderByDesc("expire_time");
        List<MeMember> cards = meMemberDao.selectList(wrapper);
        return cards.isEmpty() ? null : cards.get(0);
    }

    private long cardCount(List<String> plates) {
        if (plates.isEmpty()) {
            return 0L;
        }
        QueryWrapper<MeMember> wrapper = new QueryWrapper<>();
        wrapper.eq("del_time", 0).in("plate", plates);
        Long count = meMemberDao.selectCount(wrapper);
        return count == null ? 0L : count;
    }

    private List<AppCardVO> cardList(List<String> plates) {
        if (plates.isEmpty()) {
            return Collections.emptyList();
        }
        QueryWrapper<MeMember> wrapper = new QueryWrapper<>();
        wrapper.eq("del_time", 0).in("plate", plates).orderByDesc("expire_time");
        List<MeMember> cards = meMemberDao.selectList(wrapper);
        if (cards.isEmpty()) {
            return Collections.emptyList();
        }

        // 适用停车场范围：卡类型 -> 套餐 -> parking_ids，查不到就按企业下全部停车场
        String scope = parkingScope(cards);
        LocalDateTime now = LocalDateTime.now();

        List<AppCardVO> result = new ArrayList<>(cards.size());
        for (MeMember card : cards) {
            AppCardVO vo = new AppCardVO();
            vo.setId(card.getId());
            String typeName = CARD_TYPE_NAME.getOrDefault(card.getCardType(), "会员卡");
            vo.setName(card.getPlate() == null || card.getPlate().isBlank() ? typeName : typeName + " · " + card.getPlate());
            vo.setType(card.getCardType());
            vo.setParkingName(scope);
            vo.setExpireTime(card.getExpireTime());
            vo.setStatus(cardStatus(card, now));
            // 没有次卡概念，恒为空，前端据此隐藏次数行
            vo.setTotalTimes(null);
            vo.setRemainTimes(null);
            vo.setColor(CARD_TYPE_COLOR.getOrDefault(card.getCardType(), "#10b981"));
            vo.setTotalAmount(card.getTotalAmount());
            result.add(vo);
        }
        return result;
    }

    /** 卡状态：3 已冻结 / 2 已过期 / 1 七天内到期 / 0 正常 */
    private int cardStatus(MeMember card, LocalDateTime now) {
        if (Integer.valueOf(1).equals(card.getFrozen())) {
            return 3;
        }
        if (card.getExpireTime() == null || !card.getExpireTime().isAfter(now)) {
            return 2;
        }
        if (card.getExpireTime().isBefore(now.plusDays(7))) {
            return 1;
        }
        return 0;
    }

    /** 卡包卡片的适用停车场名称，解析不了就兜底「全部停车场」 */
    private String parkingScope(List<MeMember> cards) {
        for (MeMember card : cards) {
            if (card.getCardType() == null) {
                continue;
            }
            List<MePackage> packages = mePackageDao.selectList(
                    new QueryWrapper<MePackage>().eq("del_time", 0).eq("type", card.getCardType()));
            for (MePackage pkg : packages) {
                if (pkg.getParkingIds() == null || pkg.getParkingIds().isBlank()) {
                    continue;
                }
                List<Long> ids = parseIds(pkg.getParkingIds());
                if (ids.isEmpty()) {
                    continue;
                }
                List<PkParking> parkings = pkParkingDao.selectList(
                        new QueryWrapper<PkParking>().eq("del_time", 0).in("id", ids));
                if (!parkings.isEmpty()) {
                    return parkings.stream().map(PkParking::getName).collect(Collectors.joining(" / "));
                }
            }
        }
        QueryWrapper<PkParking> wrapper = new QueryWrapper<>();
        wrapper.eq("del_time", 0).ne("en_id", 0);
        List<PkParking> all = pkParkingDao.selectList(wrapper);
        return all.isEmpty() ? "全部停车场" : all.stream().map(PkParking::getName).collect(Collectors.joining(" / "));
    }

    private List<Long> parseIds(String commaSeparated) {
        List<Long> ids = new ArrayList<>();
        for (String part : commaSeparated.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                try {
                    ids.add(Long.parseLong(trimmed));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return ids;
    }

    // ==================== 优惠券 ====================

    private AppCouponVO toCouponVO(OrCoupon coupon) {
        AppCouponVO vo = new AppCouponVO();
        vo.setId(coupon.getId());
        vo.setName(coupon.getName());
        vo.setType(coupon.getType() == null ? 1 : coupon.getType());
        vo.setAmount(coupon.getDiscount());
        vo.setTotalCount(coupon.getTotalCount());
        vo.setUsedCount(coupon.getUsedCount());

        BigDecimal threshold = coupon.getThreshold() == null ? BigDecimal.ZERO : coupon.getThreshold();
        BigDecimal discount = coupon.getDiscount() == null ? BigDecimal.ZERO : coupon.getDiscount();
        vo.setCondition(threshold.compareTo(BigDecimal.ZERO) > 0 ? "满" + money(threshold) + "可用" : "无门槛");

        // 折扣券的 discount 列存的是 0.7 这种比率，展示要乘 10 才是「7 折」
        int type = vo.getType();
        if (type == 2) {
            vo.setDescription(money(discount.multiply(BigDecimal.TEN)) + " 折");
        } else if (type == 3) {
            vo.setDescription(money(discount) + " 分钟免费");
        } else {
            vo.setDescription("立减 " + money(discount));
        }

        // 没有领取记录表，只知道平台什么时候发放的这批券（create_time），
        // 从发放日起算 valid_days。这是给展示用的近似值，不是精确的领取到期日
        LocalDate expireDate = expireDate(coupon);
        vo.setExpireTime(expireDate == null ? null : expireDate.toString());

        vo.setStatus(couponStatus(coupon, expireDate));
        return vo;
    }

    /** 到期日 = 平台发放日 + 有效天数；发放日未知或不限期时返回 null */
    private LocalDate expireDate(OrCoupon coupon) {
        if (coupon.getCreateTime() == null || coupon.getValidDays() == null || coupon.getValidDays() <= 0) {
            return null;
        }
        return coupon.getCreateTime().toLocalDate().plusDays(coupon.getValidDays());
    }

    /**
     * 优惠券状态：1 已用尽（平台停用或已用完）/ 2 已过期 / 0 可用。
     * 停用算「已用尽」是因为前端对 status=1 的卡不显示删除按钮、样式置灰，正好对应「平台已下架」。
     */
    private int couponStatus(OrCoupon coupon, LocalDate expireDate) {
        boolean usedUp = coupon.getTotalCount() != null && coupon.getUsedCount() != null
                && coupon.getUsedCount() >= coupon.getTotalCount();
        boolean disabled = Integer.valueOf(0).equals(coupon.getStatus());
        if (usedUp || disabled) {
            return 1;
        }
        return expireDate != null && LocalDate.now().isAfter(expireDate) ? 2 : 0;
    }

    // ==================== 订单 ====================

    private AppOrderVO toOrderVO(Map<String, Object> row) {
        AppOrderVO vo = new AppOrderVO();
        vo.setId(toLong(row.get("id")));
        vo.setOrderNo(str(row.get("orderNo")));
        vo.setPlateNumber(str(row.get("plateNumber")));
        vo.setParkingName(str(row.get("parkingName")));
        vo.setParkingAddress(str(row.get("parkingAddress")));
        vo.setEntryTime(str(row.get("entryTime")));
        vo.setExitTime(str(row.get("exitTime")));
        vo.setDuration(formatDuration(toLong(row.get("durationMinutes"))));
        vo.setAmount(toBigDecimal(row.get("totalAmount")));
        vo.setDiscount(toBigDecimal(row.get("discountAmount")));
        vo.setPayAmount(toBigDecimal(row.get("payAmount")));

        int payStatus = toInt(row.get("payStatus"));
        int orderStatus = toInt(row.get("orderStatus"));
        // 订单状态 4 = 已完成，才算「已完成」；否则已支付过就显示已支付
        vo.setStatus(orderStatus == 4 ? 2 : (payStatus >= 1 ? 1 : 0));
        vo.setStatusText(orderStatus == 4 ? "已完成" : (payStatus >= 1 ? "已支付" : "待支付"));
        return vo;
    }

    private String formatDuration(long minutes) {
        if (minutes <= 0) {
            return null;
        }
        long hours = minutes / 60;
        long rest = minutes % 60;
        if (hours > 0 && rest > 0) {
            return hours + "小时" + rest + "分钟";
        }
        if (hours > 0) {
            return hours + "小时";
        }
        return rest + "分钟";
    }

    private String money(BigDecimal value) {
        return value == null ? "0" : value.stripTrailingZeros().toPlainString();
    }

    private String str(Object value) {
        return value == null ? null : value.toString();
    }

    private int toInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString().trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        try {
            return new BigDecimal(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
