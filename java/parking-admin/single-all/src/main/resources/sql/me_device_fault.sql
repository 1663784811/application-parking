-- 设备故障工单表
-- id 由 MyBatis-Plus 雪花算法分配（id-type: ASSIGN_ID），非自增
-- 审计字段（en_id/create_time/update_time/del_time）均设 DB 级默认值，
-- 因 MetaObjectHandler 未注册，@TableField(fill=...) 自动填充不生效，
-- 而 MP 插入时省略 null 字段，故由 DB 默认值兜底
CREATE TABLE `me_device_fault` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `app_id` bigint DEFAULT NULL COMMENT '所属APPID',
  `order_no` varchar(64) DEFAULT NULL COMMENT '工单号',
  `device_id` bigint DEFAULT NULL COMMENT '设备ID',
  `device_name` varchar(128) DEFAULT NULL COMMENT '设备名称(快照)',
  `device_type` varchar(32) DEFAULT NULL COMMENT '设备类型(快照)',
  `parking_id` bigint DEFAULT NULL COMMENT '停车场ID(快照)',
  `parking_name` varchar(128) DEFAULT NULL COMMENT '停车场名称(快照)',
  `fault_type` varchar(128) DEFAULT NULL COMMENT '故障类型',
  `report_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上报时间',
  `repairer` varchar(64) DEFAULT NULL COMMENT '处理人',
  `expect_complete_time` datetime DEFAULT NULL COMMENT '预计完成时间',
  `complete_time` datetime DEFAULT NULL COMMENT '实际完成时间',
  `status` varchar(32) NOT NULL DEFAULT 'pending' COMMENT '处理状态{pending:待处理,processing:处理中,completed:已完成}',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备故障工单';
