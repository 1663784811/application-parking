package com.cyyaw.admin.dao.or;

import com.cyyaw.admin.dao.BaseMapperPlus;
import com.cyyaw.admin.entity.module.or.OrOrderDetail;
import org.apache.ibatis.annotations.Select;

public interface OrOrderDetailDao extends BaseMapperPlus<OrOrderDetailDao, OrOrderDetail> {

    /**
     * 按业务ID查订单明细。停车场景下 business_id 存的是 pk_car_log.id，
     * 这是停车记录与订单之间的唯一关联（or_order 本身没有 car_log_id 字段）。
     */
    @Select("select * from or_order_detail where business_id = #{businessId} limit 1")
    OrOrderDetail selectByBusinessId(Long businessId);

}
