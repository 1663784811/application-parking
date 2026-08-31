-- 设备表（IoT 设备）
-- id 由 MyBatis-Plus 雪花算法分配（id-type: ASSIGN_ID），非自增
-- 审计字段（en_id/create_time/update_time/del_time）均设 DB 级默认值，
-- 因 MetaObjectHandler 未注册，@TableField(fill=...) 自动填充不生效，
-- 而 MP 插入时省略 null 字段，故由 DB 默认值兜底
CREATE TABLE IF NOT EXISTS `iot_device` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `app_id` bigint DEFAULT NULL COMMENT '所属APPID',
  `business_id` bigint DEFAULT NULL COMMENT '所属业务ID,比如停车场ID',
  `classification_id` bigint DEFAULT NULL COMMENT '分类表ID',
  `thing_model_id` bigint DEFAULT NULL COMMENT '物模型ID',
  `product_id` bigint DEFAULT NULL COMMENT '产品ID',
  `name` varchar(100) DEFAULT NULL COMMENT '设备名称',
  `code` varchar(50) DEFAULT NULL COMMENT '设备编码',
  `type` varchar(50) DEFAULT NULL COMMENT '设备类型{switch:开关,light:电灯,aircondition:空调,refrigerator:冰箱}',
  `model` varchar(50) DEFAULT NULL COMMENT '设备型号',
  `serial_no` varchar(50) DEFAULT NULL COMMENT '设备序列号',
  `mac_address` varchar(50) DEFAULT NULL COMMENT 'MAC地址',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `firmware_version` varchar(50) DEFAULT NULL COMMENT '固件版本',
  `device_type` varchar(50) DEFAULT NULL COMMENT '设备类型{connect:直连,gateway:网关,subDevice:子设备}',
  `connect_type` varchar(50) DEFAULT NULL COMMENT '连接方式{1:WiFi,2:蓝牙,3:有线,4:4G/5G,5:其他}',
  `online_status` int NOT NULL DEFAULT 0 COMMENT '在线状态{0:离线,1:在线}',
  `work_status` int NOT NULL DEFAULT 0 COMMENT '工作状态{0:停用,1:正常,2:故障,3:维护中}',
  `location` varchar(255) DEFAULT NULL COMMENT '安装位置',
  `location_type` int DEFAULT NULL COMMENT '位置类型{1:停车场入口,2:停车场出口}',
  `long_lat` varchar(64) DEFAULT NULL COMMENT '经纬度',
  `activate_time` datetime DEFAULT NULL COMMENT '激活时间',
  `last_online_time` datetime DEFAULT NULL COMMENT '最后在线时间',
  `description` varchar(500) DEFAULT NULL COMMENT '设备描述',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态{0:禁用,1:启用}',
  `icon` varchar(20) DEFAULT NULL COMMENT 'icon',
  `username` varchar(20) DEFAULT NULL COMMENT '设备用户名',
  `password` varchar(64) DEFAULT NULL COMMENT '密码',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备';
