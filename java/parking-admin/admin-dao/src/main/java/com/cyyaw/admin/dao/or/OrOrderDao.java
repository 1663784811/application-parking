package com.cyyaw.admin.dao.or;

import com.cyyaw.admin.dao.BaseMapperPlus;
import com.cyyaw.admin.entity.module.or.OrOrder;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface OrOrderDao extends BaseMapperPlus<OrOrderDao, OrOrder> {

    /**
     * 按「我的车辆」车牌集合查订单，并把展示用的时间、停车场名称一起带出来。
     * <p>
     * 为什么按车牌而不是按 or_order.user_id 过滤：入场建单时没有登录态，
     * 历史订单的 user_id / app_id 全是 NULL（实测 26 行全空），按 user_id 过滤会漏掉所有未付款的单子。
     * 按车牌过滤同时覆盖「已绑定用户」和「未绑定用户」两种单子，也天然不会把同企业别的车主的订单串进来。
     * <p>
     * 停车订单的 or_order_detail.business_id 存的是 pk_car_log.id，这是订单与停车记录唯一的关联链路，
     * 所以车牌和入场出场时间要从 detail 再 join 到 pk_car_log 上。
     * 停车场名称走 or_order.store_id（= pk_parking.id），出场缴费时才会回填，
     * 因此是 left join：还没缴费过场的历史订单 parkingName 会是 null。
     * <p>
     * plates 为空（车主还没登记任何车辆）时退化为按企业维度查 ——
     * 单账号企业里这些订单本来就都属于这个车主，别让页面一空就什么都看不到。
     * <p>
     * 返回 Map 而不是实体：一条订单可能有多条明细，join 之后行数会膨胀，服务层再拼 VO。
     * 时间列一律 date_format 成字符串，避免 Map 里拿到 java.sql.Timestamp 再转。
     */
    @Select("<script>" +
            "select o.id as id, o.order_no as orderNo, o.user_id as userId, o.pay_status as payStatus, " +
            "o.order_status as orderStatus, o.total_amount as totalAmount, o.discount_amount as discountAmount, " +
            "o.pay_amount as payAmount, date_format(o.create_time, '%Y-%m-%d %H:%i:%s') as createTime, " +
            "d.business_id as carLogId, p.name as parkingName, p.address as parkingAddress, " +
            "c.car_number as plateNumber, c.parking_id as parkingId, " +
            "TIMESTAMPDIFF(MINUTE, c.entry_time, c.out_time) as durationMinutes, " +
            "date_format(c.entry_time, '%Y-%m-%d %H:%i:%s') as entryTime, " +
            "date_format(c.out_time, '%Y-%m-%d %H:%i:%s') as exitTime " +
            "from or_order o " +
            "left join or_order_detail d on d.order_id = o.id and d.del_time = 0 " +
            "left join pk_parking p on p.id = o.store_id and p.del_time = 0 " +
            "left join pk_car_log c on c.id = d.business_id " +
            "where o.del_time = 0 and d.business_id is not null " +
            "<if test='enId != null'> and o.en_id = #{enId} </if>" +
            "<if test='plates != null and plates.size() > 0'>" +
            " and c.car_number in <foreach collection='plates' item='p' open='(' separator=',' close=')'>#{p}</foreach>" +
            "</if>" +
            " order by o.create_time desc limit #{limit}</script>")
    List<Map<String, Object>> selectByPlates(Long enId, List<String> plates, Integer limit);

    /**
     * 同范围下的订单数，供「我的」页面统计位用。条件与 {@link #selectByPlates} 完全一致，只是不查明细列。
     */
    @Select("<script>" +
            "select count(*) from or_order o " +
            "left join or_order_detail d on d.order_id = o.id and d.del_time = 0 " +
            "left join pk_car_log c on c.id = d.business_id " +
            "where o.del_time = 0 and d.business_id is not null " +
            "<if test='enId != null'> and o.en_id = #{enId} </if>" +
            "<if test='plates != null and plates.size() > 0'>" +
            " and c.car_number in <foreach collection='plates' item='p' open='(' separator=',' close=')'>#{p}</foreach>" +
            "</if></script>")
    Long selectCountByPlates(Long enId, List<String> plates);
}
