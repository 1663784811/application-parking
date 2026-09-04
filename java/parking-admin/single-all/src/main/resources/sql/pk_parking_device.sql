-- 停车设备表：通道与设备的绑定关系（一台设备全局唯一归属一条通道）
-- id 由 MyBatis-Plus 雪花算法分配（id-type: ASSIGN_ID），非自增
-- 审计字段（en_id/create_time/update_time/del_time）均设 DB 级默认值，
-- 因 MetaObjectHandler 未注册，@TableField(fill=...) 自动填充不生效，
-- 而 MP 插入时省略 null 字段，故由 DB 默认值兜底
-- device_id 设唯一约束：服务层已做"设备全局唯一绑定"校验，DB 层唯一索引兜底防并发
CREATE TABLE IF NOT EXISTS `pk_parking_device` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `app_id` bigint DEFAULT NULL COMMENT '应用ID',
  `parking_id` bigint DEFAULT NULL COMMENT '所属停车场ID',
  `channel_id` bigint DEFAULT NULL COMMENT '所属通道ID',
  `device_id` bigint DEFAULT NULL COMMENT '设备ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_id` (`device_id`),
  KEY `idx_channel_id` (`channel_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='停车设备（通道-设备绑定）';
