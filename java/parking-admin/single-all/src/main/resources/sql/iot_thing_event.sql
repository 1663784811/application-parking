-- 物模型-事件表（完整 TSL：事件）
-- id 由 MyBatis-Plus 雪花算法分配（id-type: ASSIGN_ID），非自增
-- 注意：列名使用 prop_key 而非 key，因 key 是 MySQL 保留字，
-- MP 不会自动加引号，SELECT ... key ... 会触发 SQL 语法错误；
-- 实体 IotThingEvent.key 字段通过 @TableField("prop_key") 显式映射到此列
-- 审计字段（en_id/create_time/update_time/del_time）均设 DB 级默认值，
-- 因 MetaObjectHandler 未注册，@TableField(fill=...) 自动填充不生效，
-- 而 MP 插入时省略 null 字段，故由 DB 默认值兜底
CREATE TABLE IF NOT EXISTS `iot_thing_event` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `thing_model_id` bigint DEFAULT NULL COMMENT '物模型ID',
  `name` varchar(64) DEFAULT '' COMMENT '事件名称',
  `prop_key` varchar(64) DEFAULT NULL COMMENT '属性键(key)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_thing_model_id` (`thing_model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物模型-事件';
