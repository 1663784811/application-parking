-- 停车场通道表
-- id 由 MyBatis-Plus 雪花算法分配（id-type: ASSIGN_ID），非自增
-- 审计字段（en_id/create_time/update_time/del_time）均设 DB 级默认值，
-- 因 MetaObjectHandler 未注册，@TableField(fill=...) 自动填充不生效，
-- 而 MP 插入时省略 null 字段，故由 DB 默认值兜底
CREATE TABLE `pk_channel` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `app_id` bigint DEFAULT NULL COMMENT '应用ID',
  `parking_id` bigint DEFAULT NULL COMMENT '所属停车场ID',
  `code` varchar(64) DEFAULT NULL COMMENT '通道编号',
  `name` varchar(255) DEFAULT NULL COMMENT '通道名称',
  `type` varchar(16) DEFAULT NULL COMMENT '通道类型{in:入口,out:出口,inout:出入口}',
  `ip` varchar(64) DEFAULT NULL COMMENT '设备IP地址',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态{0:故障,1:正常}',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_parking_id` (`parking_id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='停车场通道';
