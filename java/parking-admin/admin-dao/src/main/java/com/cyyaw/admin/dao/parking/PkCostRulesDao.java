package com.cyyaw.admin.dao.parking;

import com.cyyaw.admin.dao.BaseMapperPlus;
import com.cyyaw.admin.entity.module.parking.PkCostRules;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PkCostRulesDao extends BaseMapperPlus<PkCostRulesDao, PkCostRules> {

    /**
     * 查询某个停车场配置的全部收费规则（完整规则行，已过滤已删除的规则与关联）。
     * 规则通过 pk_parking_cost_rules 多对多挂到停车场，一条规则可被多个停车场复用。
     * <p>
     * 注意：必须显式映射 rule_time。MyBatis 开了 map-underscore-to-camel-case，
     * 原生 @Select 会把列 rule_time 自动映射到属性 ruleTime，而实体字段名就是带下划线的
     * rule_time，找不到对应属性时该列会被静默丢弃（值为 null），计费结果就错了。
     */
    @Results({
            @Result(column = "rule_time", property = "rule_time")
    })
    @Select("select r.* from pk_cost_rules r " +
            "inner join pk_parking_cost_rules pr on pr.cost_rules_id = r.id " +
            "where pr.parking_id = #{parkingId} and pr.del_time = 0 and r.del_time = 0")
    List<PkCostRules> selectByParkingId(Long parkingId);

}
