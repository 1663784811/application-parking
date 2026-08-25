-- 短信模板表
-- id 由 MyBatis-Plus 雪花算法分配（id-type: ASSIGN_ID），非自增
-- 审计字段（en_id/create_time/update_time/del_time）均设 DB 级默认值，
-- 因 MetaObjectHandler 未注册，@TableField(fill=...) 自动填充不生效，
-- 而 MP 插入时省略 null 字段，故由 DB 默认值兜底
CREATE TABLE `au_sms_template` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `name` varchar(255) DEFAULT NULL COMMENT '模板名称',
  `type` int DEFAULT NULL COMMENT '模板类型{1:到期提醒,2:欠费催缴,3:入场通知,4:出场通知}',
  `content` varchar(500) DEFAULT NULL COMMENT '短信内容',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态{0:停用,1:启用}',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信模板';
