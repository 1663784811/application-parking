-- 用户第三方登录绑定表（微信小程序 / 微信公众号 / 支付宝）
-- id 由 MyBatis-Plus 雪花算法分配（id-type: ASSIGN_ID），非自增
-- 审计字段（en_id/create_time/update_time/del_time）均设 DB 级默认值，
-- 因 MetaObjectHandler 未注册，@TableField(fill=...) 自动填充不生效，
-- 而 MP 插入时省略 null 字段，故由 DB 默认值兜底
CREATE TABLE `au_user_third` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `app_id` bigint DEFAULT NULL COMMENT '所属APPID',
  `user_id` bigint DEFAULT NULL COMMENT '关联 au_user.id',
  `platform` varchar(20) NOT NULL COMMENT '平台{wechat_ma:微信小程序,wechat_mp:微信公众号,alipay:支付宝}',
  `open_id` varchar(64) NOT NULL COMMENT '平台内唯一标识',
  `union_id` varchar(64) DEFAULT NULL COMMENT '微信开放平台unionId(跨小程序/公众号同一个人)',
  `nick_name` varchar(50) DEFAULT NULL COMMENT '昵称',
  `face` text COMMENT '头像',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  -- 唯一键带上 app_id，与 findByPlatformAndOpenIdAndAppId 的查询范围一致；
  -- 同一个微信号在不同 appId 下可以各自绑定本地用户。并发首登靠它兜底，避免一个微信号建出两个账号。
  UNIQUE KEY `uk_platform_openid_app` (`platform`,`open_id`,`app_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_union_id` (`union_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户第三方登录绑定';
