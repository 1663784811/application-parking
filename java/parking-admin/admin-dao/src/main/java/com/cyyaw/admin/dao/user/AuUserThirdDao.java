package com.cyyaw.admin.dao.user;

import com.cyyaw.admin.dao.BaseMapperPlus;
import com.cyyaw.admin.entity.module.user.AuUserThird;
import org.apache.ibatis.annotations.Select;

public interface AuUserThirdDao extends BaseMapperPlus<AuUserThirdDao, AuUserThird> {

    @Select("SELECT * FROM au_user_third WHERE platform = #{platform} AND open_id = #{openId} AND app_id = #{appId}")
    AuUserThird findByPlatformAndOpenIdAndAppId(String platform, String openId, Long appId);

    /**
     * 微信小程序与公众号同一个开放平台账号下 union_id 相同，用它把两个渠道并到同一个人。
     */
    @Select("SELECT * FROM au_user_third WHERE union_id = #{unionId} AND app_id = #{appId} AND union_id IS NOT NULL LIMIT 1")
    AuUserThird findByUnionIdAndAppId(String unionId, Long appId);

}
