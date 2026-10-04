package com.cyyaw.admin.dao.or;

import com.cyyaw.admin.dao.BaseMapperPlus;
import com.cyyaw.admin.entity.module.or.OrCoupon;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface OrCouponDao extends BaseMapperPlus<OrCouponDao, OrCoupon> {

    /**
     * 按企业/APP 查优惠券。
     * <p>
     * 不用 QueryWrapper 是因为 status 是 MySQL 保留字、MP 不会自动加引号；
     * 这里只查不筛状态（前端要的是「可用/已使用/已过期」，由服务端按已用数量与有效期推导），
     * SELECT * 不碰 status 列名，写 SQL 就安全了。
     * del_time 手工过滤，与库内其他表口径一致。
     * <p>
     * app_id 的写法是「等于传入值 或 为 NULL」：建券的代码根本没写 app_id（实测 4 行全 NULL），
     * 照 or_order.user_id 那个坑来写硬等值条件，用户券列表会永远是空的。
     * 现在按企业归口，app_id 有值时仍然收窄到该 APP。
     */
    @Select("<script>" +
            "select * from or_coupon where del_time = 0 " +
            "<if test='enId != null'> and en_id = #{enId} </if>" +
            "<if test='appId != null'> and (app_id = #{appId} or app_id is null) </if>" +
            "order by create_time desc</script>")
    List<OrCoupon> selectByOwner(@Param("enId") Long enId, @Param("appId") Long appId);
}
