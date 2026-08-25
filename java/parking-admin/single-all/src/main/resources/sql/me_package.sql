-- 会员套餐表
-- 注意：id 由 MyBatis-Plus 雪花算法生成（id-type: ASSIGN_ID），故非自增；
--       en_id / create_time / update_time / del_time 给 DB 默认值，规避
--       @PrePersist / MetaObjectHandler 未生效导致审计字段为空的问题。
CREATE TABLE `me_package` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `app_id` bigint DEFAULT NULL COMMENT '所属APPID',
  `name` varchar(255) DEFAULT NULL COMMENT '套餐名称',
  `type` int DEFAULT NULL COMMENT '套餐类型{1:月卡,2:季卡,3:年卡}',
  `valid_days` int DEFAULT NULL COMMENT '有效期天数',
  `price` decimal(18,2) DEFAULT NULL COMMENT '售价',
  `parking_ids` varchar(255) DEFAULT NULL COMMENT '适用停车场ID(逗号分隔)',
  `sales` int NOT NULL DEFAULT 0 COMMENT '已售数量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员套餐';
