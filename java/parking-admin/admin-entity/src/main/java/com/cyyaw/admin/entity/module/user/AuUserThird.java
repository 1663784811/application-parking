package com.cyyaw.admin.entity.module.user;

import com.cyyaw.admin.entity.utils.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Schema(description = "用户第三方登录绑定")
@Table(name = "au_user_third")
@EqualsAndHashCode(callSuper = true)
public class AuUserThird extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "app_id", columnDefinition = "bigint COMMENT '所属APPID'")
    private Long appId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "user_id", columnDefinition = "bigint COMMENT '用户ID'")
    private Long userId;
    // =======================================================
    // =======================================================
    // =======================================================

    @Column(name = "platform", columnDefinition = "varchar(20) COMMENT '平台{wechat_ma:微信小程序,wechat_mp:微信公众号,alipay:支付宝}'")
    private String platform;

    @Column(name = "open_id", columnDefinition = "varchar(64) COMMENT '平台内唯一标识'")
    private String openId;

    @Column(name = "union_id", columnDefinition = "varchar(64) COMMENT '微信开放平台unionId(跨小程序/公众号同一个人)'")
    private String unionId;

    @Column(name = "nick_name", columnDefinition = "varchar(50) COMMENT '昵称'")
    private String nickName;

    @Column(name = "face", columnDefinition = "text COMMENT '头像'")
    private String face;

}