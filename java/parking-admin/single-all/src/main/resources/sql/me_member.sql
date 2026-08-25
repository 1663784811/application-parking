-- 会员表
-- id 由 MyBatis-Plus 雪花算法分配（id-type: ASSIGN_ID），非自增
-- 审计字段（en_id/create_time/update_time/del_time）均设 DB 级默认值，
-- 因 MetaObjectHandler 未注册，@TableField(fill=...) 自动填充不生效，
-- 而 MP 插入时省略 null 字段，故由 DB 默认值兜底
CREATE TABLE `me_member` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `app_id` bigint DEFAULT NULL COMMENT '所属APPID',
  `plate` varchar(32) DEFAULT NULL COMMENT '车牌号',
  `name` varchar(64) DEFAULT NULL COMMENT '车主姓名',
  `phone` varchar(32) DEFAULT NULL COMMENT '手机号',
  `card_type` int DEFAULT NULL COMMENT '卡类型{1:月卡,2:季卡,3:年卡}',
  `expire_time` datetime DEFAULT NULL COMMENT '到期时间',
  `frozen` int NOT NULL DEFAULT 0 COMMENT '是否冻结{0:正常,1:冻结}',
  `total_amount` decimal(18,2) DEFAULT 0.00 COMMENT '累计充值',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员';
