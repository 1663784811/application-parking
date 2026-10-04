package com.cyyaw.admin.dao.parking;

import com.cyyaw.admin.dao.BaseMapperPlus;
import com.cyyaw.admin.entity.module.parking.PkCostRules;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PkCostRulesDao extends BaseMapperPlus<PkCostRulesDao, PkCostRules> {

    /**
     * 查询某个停车场配置的全部收费规则（完整规则行，已过滤已删除的规则与关联）。
     * 规则通过 pk_parking_cost_rules 多对多挂到停车场，一条规则可被多个停车场复用。
     * <p>
     * 注意：必须显式映射 ruleTime。MyBatis 开了 map-underscore-to-camel-case，
     * 原生 @Select 会把列 ruleTime 自动映射到属性 ruleTime，而实体字段名就是带下划线的
     * ruleTime，找不到对应属性时该列会被静默丢弃（值为 null），计费结果就错了。
     */
    @Select("select r.* from pk_cost_rules r " + "inner join pk_parking_cost_rules pr on pr.cost_rules_id = r.id " + "where pr.parking_id = #{parkingId} and pr.del_time = 0 and r.del_time = 0")
    List<PkCostRules> selectByParkingId(Long parkingId);

    /**
     * 查询某个停车场、指定车型适用的收费规则。
     */
    default List<PkCostRules> selectByParkingIdAndCarType(Long parkingId, String carType) {
        return matchCarType(selectByParkingId(parkingId), carType);
    }

    /**
     * 按车型筛规则：规则 {@code car_type} 为空视为不限车型（所有车命中），否则须等于该车型。
     * <p>
     * {@code carType} 为空（null 或空白）表示不限车型，原样返回 ——
     * {@code pk_car_log.car_type} 可空，车型未知时若把限定车型的规则全排除，费用会恒为 0。
     * <p>
     * 供看板复用：规则按停车场查一次后逐车在内存里筛，不必每辆车查一次库。
     */
    static List<PkCostRules> matchCarType(List<PkCostRules> rules, String carType) {
        if (rules == null) {
            return List.of();
        }
        if (carType == null || carType.isBlank()) {
            return rules;
        }
        return rules.stream().filter(r -> r.getCarType() == null || r.getCarType().isBlank() || r.getCarType().trim().equalsIgnoreCase(carType.trim())).toList();
    }

}
