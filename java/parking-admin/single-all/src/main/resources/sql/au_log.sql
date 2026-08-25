-- 系统日志表（登录日志 + 操作日志）
-- id 由 MyBatis-Plus 雪花算法分配（id-type: ASSIGN_ID），非自增
-- 审计字段（en_id/create_time/update_time/del_time）均设 DB 级默认值，
-- 因 MetaObjectHandler 未注册，@TableField(fill=...) 自动填充不生效，
-- 而 MP 插入时省略 null 字段，故由 DB 默认值兜底
CREATE TABLE `au_log` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `admin_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `admin_name` varchar(50) DEFAULT NULL COMMENT '操作人账号',
  `log_type` varchar(20) DEFAULT NULL COMMENT '日志类型{login:登录日志,operation:操作日志}',
  `action_type` varchar(30) DEFAULT NULL COMMENT '操作类型{login,edit,delete,export,open_gate,refund,edit_rule,blacklist}',
  `description` varchar(500) DEFAULT NULL COMMENT '操作描述',
  `ip` varchar(50) DEFAULT NULL COMMENT '操作IP地址',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '浏览器/设备信息',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态{0:失败,1:成功}',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_log_type` (`log_type`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志';
