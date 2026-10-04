package com.cyyaw.admin.dao.user;

import com.cyyaw.admin.dao.BaseMapperPlus;
import com.cyyaw.admin.entity.module.user.AppDefaultVehicle;

/**
 * 我的车辆。
 * <p>
 * 归属维度是「账号 ID 优先、手机号兜底」，所以列表查询走 QueryWrapper 动态条件，
 * 这里只声明一张表的基础映射；需要跨表关联的统计都在 AppOrderDao 里。
 */
public interface AppDefaultVehicleDao extends BaseMapperPlus<AppDefaultVehicleDao, AppDefaultVehicle> {
}
