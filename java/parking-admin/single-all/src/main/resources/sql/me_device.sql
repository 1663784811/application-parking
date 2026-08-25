-- 设备表
-- id 由 MyBatis-Plus 雪花算法分配（id-type: ASSIGN_ID），非自增
-- 审计字段（en_id/create_time/update_time/del_time）均设 DB 级默认值，
-- 因 MetaObjectHandler 未注册，@TableField(fill=...) 自动填充不生效，
-- 而 MP 插入时省略 null 字段，故由 DB 默认值兜底
CREATE TABLE `me_device` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `app_id` bigint DEFAULT NULL COMMENT '所属APPID',
  `code` varchar(64) DEFAULT NULL COMMENT '设备编号',
  `name` varchar(128) DEFAULT NULL COMMENT '设备名称',
  `type` varchar(32) DEFAULT NULL COMMENT '设备类型{camera:摄像头,gate:道闸,screen:显示屏,sensor:地感}',
  `parking_id` bigint DEFAULT NULL COMMENT '所属停车场ID',
  `channel` varchar(64) DEFAULT NULL COMMENT '安装通道',
  `ip` varchar(64) DEFAULT NULL COMMENT 'IP地址',
  `online_status` int NOT NULL DEFAULT 0 COMMENT '在线状态{0:离线,1:在线}',
  `last_online` datetime DEFAULT NULL COMMENT '最后在线时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备';
