-- 会员续费记录表
-- id 由 MyBatis-Plus 雪花算法分配（id-type: ASSIGN_ID），非自增
-- 审计字段设 DB 级默认值（MetaObjectHandler 未注册，由 DB 默认值兜底）
CREATE TABLE `me_renewal_record` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `app_id` bigint DEFAULT NULL COMMENT '所属APPID',
  `order_no` varchar(64) DEFAULT NULL COMMENT '订单号',
  `member_id` bigint DEFAULT NULL COMMENT '会员ID',
  `plate` varchar(32) DEFAULT NULL COMMENT '车牌号',
  `name` varchar(64) DEFAULT NULL COMMENT '车主姓名',
  `card_type` int DEFAULT NULL COMMENT '卡类型{1:月卡,2:季卡,3:年卡}',
  `amount` decimal(18,2) DEFAULT NULL COMMENT '续费金额',
  `operator` varchar(64) DEFAULT NULL COMMENT '操作员',
  `renewal_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '续费时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员续费记录';
