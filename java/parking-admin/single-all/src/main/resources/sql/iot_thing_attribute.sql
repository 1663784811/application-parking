-- 物模型-属性表（完整 TSL：属性）
-- id 由 MyBatis-Plus 雪花算法分配（id-type: ASSIGN_ID），非自增
-- 注意：列名使用 prop_key 而非 key，因 key 是 MySQL 保留字，
-- MP 不会自动加引号，SELECT ... key ... 会触发 SQL 语法错误；
-- 实体 IotThingAttribute.propKey 字段（驼峰转下划线=prop_key）映射到此列；字段名与列名一致，MP 不拼 AS 别名
-- 审计字段（en_id/create_time/update_time/del_time）均设 DB 级默认值，
-- 因 MetaObjectHandler 未注册，@TableField(fill=...) 自动填充不生效，
-- 而 MP 插入时省略 null 字段，故由 DB 默认值兜底
CREATE TABLE IF NOT EXISTS `iot_thing_attribute` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `thing_model_id` bigint DEFAULT NULL COMMENT '物模型ID',
  `name` varchar(64) DEFAULT '' COMMENT '名称',
  `prop_key` varchar(64) DEFAULT NULL COMMENT '键(key)',
  `unit` varchar(32) DEFAULT NULL COMMENT '单位',
  `data_type` varchar(32) DEFAULT NULL COMMENT '数据类型{number:数值,string:字符串,enumeration:枚举}',
  `min_value` decimal(18,10) DEFAULT NULL COMMENT '最小值',
  `max_value` decimal(18,10) DEFAULT NULL COMMENT '最大值',
  `data` varchar(255) DEFAULT NULL COMMENT '枚举或字符串类型列表',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_thing_model_id` (`thing_model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物模型-属性';
