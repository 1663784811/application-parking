-- 优惠券表
-- id 由 MyBatis-Plus 雪花算法分配（id-type: ASSIGN_ID），非自增
-- 审计字段（en_id/create_time/update_time/del_time）均设 DB 级默认值，
-- 因 MetaObjectHandler 未注册，@TableField(fill=...) 自动填充不生效，
-- 而 MP 插入时省略 null 字段，故由 DB 默认值兜底
CREATE TABLE `or_coupon` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `app_id` bigint DEFAULT NULL COMMENT '所属APPID',
  `name` varchar(255) DEFAULT NULL COMMENT '优惠券名称',
  `type` int DEFAULT NULL COMMENT '优惠券类型{1:满减券,2:折扣券,3:免费时长券}',
  `threshold` decimal(18,2) DEFAULT NULL COMMENT '使用门槛金额(0表示无门槛)',
  `discount` decimal(18,2) DEFAULT NULL COMMENT '优惠值:满减金额/折扣率/免费时长(分钟)',
  `total_count` int DEFAULT NULL COMMENT '发放数量',
  `used_count` int NOT NULL DEFAULT 0 COMMENT '已使用数量',
  `valid_days` int DEFAULT NULL COMMENT '领取后有效天数',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态{0:停用,1:启用}',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券';
