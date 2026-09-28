-- ============================================================================
-- 停车场管理系统 · 全量数据库建表语句
-- 生成依据：admin-entity 各 @Entity / @Table 实体类的 @Column(columnDefinition)
-- 共 35 张表，按模块分组：用户与系统 / 停车场 / 会员 / 订单 / 设备
-- ----------------------------------------------------------------------------
-- 约定：
--   1. id 由 MyBatis-Plus 雪花算法分配（id-type: ASSIGN_ID），非自增。
--   2. 审计字段 en_id / create_time / update_time / note / del_time 均设 DB 级默认值：
--      因 MetaObjectHandler 未注册，@TableField(fill=...) 自动填充不生效，
--      而 MP 插入时省略 null 字段，故由 DB 默认值兜底。
--   3. 逻辑删除：del_time = 0 表示未删除，> 0 表示已删除时间戳。
--   4. 全部 InnoDB + utf8mb4；无外键约束，关联由应用层维护。
--   5. 业务列类型/注释取自实体 columnDefinition；可空列补 DEFAULT NULL，
--      int 状态列(NOT NULL) DEFAULT N；text 列不设默认值。
-- ============================================================================

SET NAMES utf8mb4;


-- ============================================================
-- 一、用户与系统模块 (au_* / conf_sys)
-- ============================================================

-- 企业
CREATE TABLE `au_enterprise` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `name` varchar(100) DEFAULT NULL COMMENT '企业名称',
  `logo` varchar(255) DEFAULT NULL COMMENT 'logo',
  `code` varchar(50) NOT NULL COMMENT '企业编码',
  `person` varchar(50) DEFAULT NULL COMMENT '联系人',
  `root_account` varchar(50) DEFAULT NULL COMMENT '企业管理员账号',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态{0:禁用,1:启用}',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业';

-- 企业APP
CREATE TABLE `au_app` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `code` varchar(50) DEFAULT NULL COMMENT 'APP编号',
  `name` varchar(100) DEFAULT NULL COMMENT 'APP名称',
  `logo` varchar(255) DEFAULT NULL COMMENT 'logo',
  `type` varchar(64) DEFAULT NULL COMMENT '类型',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业APP';

-- 门店
CREATE TABLE `au_store` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `app_id` bigint DEFAULT NULL COMMENT '所属APPID',
  `name` varchar(100) DEFAULT NULL COMMENT '门店名称',
  `code` varchar(50) DEFAULT NULL COMMENT '门店编码',
  `contact_person` varchar(50) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `introduction` text COMMENT '个人简介',
  `certificate_type` int DEFAULT NULL COMMENT '证件类型{0:身份证,1:营业执照}',
  `certificate` text COMMENT '证件',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态{0:提交审核,1:正常,2:禁用}',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_app_id` (`app_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店';

-- 企业管理员
CREATE TABLE `au_admin` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `account` varchar(50) DEFAULT NULL COMMENT '账号',
  `phone` varchar(15) DEFAULT NULL COMMENT '手机号',
  `nick_name` varchar(50) DEFAULT NULL COMMENT '昵称',
  `password` varchar(100) DEFAULT NULL COMMENT '密码',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `gender` int DEFAULT NULL COMMENT '性别{0:未知,1:男,2:女}',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态{0:禁用,1:启用}',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_account` (`account`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业管理员';

-- 门店管理员
CREATE TABLE `au_store_admin` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `app_id` bigint DEFAULT NULL COMMENT '所属APPID',
  `store_id` bigint DEFAULT NULL COMMENT '所属门店ID',
  `account` varchar(50) DEFAULT NULL COMMENT '账号',
  `phone` varchar(15) DEFAULT NULL COMMENT '手机号',
  `nick_name` varchar(50) DEFAULT NULL COMMENT '昵称',
  `password` varchar(100) DEFAULT NULL COMMENT '密码',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `gender` int DEFAULT NULL COMMENT '性别{0:未知,1:男,2:女}',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态{0:禁用,1:启用}',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_account` (`account`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店管理员';

-- 企业用户
CREATE TABLE `au_user` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `app_id` bigint DEFAULT NULL COMMENT '所属APPID',
  `store_id` bigint DEFAULT NULL COMMENT '所属门店ID',
  `nick_name` varchar(50) DEFAULT NULL COMMENT '昵称',
  `account` varchar(50) NOT NULL COMMENT '账号',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `face` text COMMENT '头像',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `gender` int DEFAULT NULL COMMENT '性别{0:未知,1:男,2:女}',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '电话',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态{0:禁用,1:启用}',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `introduction` varchar(255) DEFAULT NULL COMMENT '个人简介',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_account` (`account`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业用户';

-- 用户地址
CREATE TABLE `au_user_address` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `name` varchar(20) DEFAULT NULL COMMENT '名称',
  `phone` varchar(15) DEFAULT NULL COMMENT '手机号',
  `country` varchar(15) DEFAULT NULL COMMENT '国家',
  `province` varchar(15) DEFAULT NULL COMMENT '省',
  `city` varchar(15) DEFAULT NULL COMMENT '城市',
  `town` varchar(15) DEFAULT NULL COMMENT '城镇(地级市)',
  `address` varchar(255) DEFAULT NULL COMMENT '详细地址',
  `def` int NOT NULL DEFAULT 0 COMMENT '是否默认地址{0:不默认,1:默认}',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户地址';

-- 用户第三方登录绑定
CREATE TABLE `au_user_third` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `app_id` bigint DEFAULT NULL COMMENT '所属APPID',
  `user_id` bigint DEFAULT NULL COMMENT '关联 au_user.id',
  `platform` varchar(20) NOT NULL COMMENT '平台{wechat_ma:微信小程序,wechat_mp:微信公众号,alipay:支付宝}',
  `open_id` varchar(64) NOT NULL COMMENT '平台内唯一标识',
  `union_id` varchar(64) DEFAULT NULL COMMENT '微信开放平台unionId(跨小程序/公众号同一个人)',
  `nick_name` varchar(50) DEFAULT NULL COMMENT '昵称',
  `face` text COMMENT '头像',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_platform_openid_app` (`platform`,`open_id`,`app_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_union_id` (`union_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户第三方登录绑定';

-- 角色
CREATE TABLE `au_role` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `name` varchar(50) DEFAULT NULL COMMENT '名称',
  `code` varchar(50) DEFAULT NULL COMMENT '编码',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `is_system` int NOT NULL DEFAULT 0 COMMENT '是否系统角色{0:自定义,1:系统}',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

-- 企业管理员角色（关联表）
CREATE TABLE `au_admin_role` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `admin_id` bigint DEFAULT NULL COMMENT '管理员ID',
  `role_id` bigint DEFAULT NULL COMMENT '角色ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_role` (`admin_id`, `role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业管理员角色';

-- 角色权限（关联表）
CREATE TABLE `au_role_permission` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `role_id` bigint DEFAULT NULL COMMENT '角色ID',
  `permission_id` bigint DEFAULT NULL COMMENT '权限ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限';

-- 角色部门（关联表）
CREATE TABLE `au_role_department` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `role_id` bigint DEFAULT NULL COMMENT '角色ID',
  `department_id` bigint DEFAULT NULL COMMENT '部门ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_department` (`role_id`, `department_id`),
  KEY `idx_department_id` (`department_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色部门';

-- 禁用管理员权限（关联表）
CREATE TABLE `au_admin_not_permission` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `admin_id` bigint DEFAULT NULL COMMENT '管理员ID',
  `permission_id` bigint DEFAULT NULL COMMENT '权限ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_permission` (`admin_id`, `permission_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='禁用管理员权限';

-- 企业部门
CREATE TABLE `au_department` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `pid` bigint DEFAULT NULL COMMENT '上级部门ID',
  `name` varchar(50) DEFAULT NULL COMMENT '名称',
  `code` varchar(50) DEFAULT NULL COMMENT '编码',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_pid` (`pid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业部门';

-- 企业权限
CREATE TABLE `au_permission` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `name` varchar(50) DEFAULT NULL COMMENT '名称',
  `code` varchar(50) DEFAULT NULL COMMENT '编码',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业权限';

-- 菜单
CREATE TABLE `au_menu` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `app_id` bigint DEFAULT NULL COMMENT '所属APPID',
  `pid` bigint DEFAULT NULL COMMENT '上级',
  `system_role` varchar(10) DEFAULT NULL COMMENT '系统角色',
  `name` varchar(255) DEFAULT NULL COMMENT '标题',
  `icon` varchar(255) DEFAULT NULL COMMENT 'icon',
  `route_name` varchar(255) DEFAULT NULL COMMENT '路由',
  `params` text COMMENT 'params参数',
  `sort` int DEFAULT NULL COMMENT '排序',
  `show_menu` int NOT NULL DEFAULT 1 COMMENT '显示菜单{0:隐藏,1:显示}',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_app_id` (`app_id`),
  KEY `idx_pid` (`pid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单';

-- 管理员通知设置
CREATE TABLE `au_admin_notify_config` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `admin_id` bigint DEFAULT NULL COMMENT '管理员ID',
  `bill` int NOT NULL DEFAULT 1 COMMENT '账单通知{0:关闭,1:开启}',
  `alert` int NOT NULL DEFAULT 1 COMMENT '异常告警{0:关闭,1:开启}',
  `announcement` int NOT NULL DEFAULT 1 COMMENT '系统公告{0:关闭,1:开启}',
  `security` int NOT NULL DEFAULT 1 COMMENT '安全提醒{0:关闭,1:开启}',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_id` (`admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员通知设置';

-- 操作日志（登录日志 + 操作日志）
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

-- 短信模板
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

-- 系统配置
CREATE TABLE `conf_sys` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `code` varchar(128) DEFAULT NULL COMMENT '系统配置',
  `val` varchar(255) DEFAULT NULL COMMENT '配置值',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  UNIQUE KEY `uk_val` (`val`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置';


-- ============================================================
-- 二、停车场模块 (pk_*)
-- ============================================================

-- 停车场
CREATE TABLE `pk_parking` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `app_id` bigint DEFAULT NULL COMMENT '应用ID',
  `name` varchar(255) DEFAULT NULL COMMENT '停车场名称',
  `long_lat` varchar(64) DEFAULT NULL COMMENT '经纬度',
  `address` varchar(255) DEFAULT NULL COMMENT '位置',
  `image` varchar(255) DEFAULT NULL COMMENT '停车场图片',
  `capacity` int DEFAULT NULL COMMENT '车位容量',
  `opening_up` int DEFAULT NULL COMMENT '是否对外开放{0:对外开放,1:不开放}',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='停车场';

-- 停车场通道
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

-- 停车场收费规则
CREATE TABLE `pk_cost_rules` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `app_id` bigint DEFAULT NULL COMMENT '应用ID',
  `name` varchar(255) DEFAULT NULL COMMENT '规则名称',
  `effective_start_time` date DEFAULT NULL COMMENT '规则有效开始日期',
  `effective_end_time` date DEFAULT NULL COMMENT '规则有效结束日期',
  `car_type` varchar(32) NOT NULL COMMENT '车辆类型{0:小型汽车,1:中型汽车,2:大型汽车}',
  `type` int NOT NULL COMMENT '收费类型{0:首段收费,2:计费时段,3:每天封顶金额,5:每次封顶金额}',
  `week` varchar(255) DEFAULT NULL COMMENT '星期{Monday:周一,Tuesday:周二,Wednesday:周三,Thursday:周四,Friday:周五,Saturday:周六,Sunday:周日}',
  `start_time` time DEFAULT NULL COMMENT '开始时间',
  `end_time` time DEFAULT NULL COMMENT '结束时间',
  `rule_time` int DEFAULT NULL COMMENT '规则时长(分钟)',
  `amount` decimal(18,2) DEFAULT NULL COMMENT '金额',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='停车场收费规则';

-- 停车场收费规则关联（多对多中间表）
CREATE TABLE `pk_parking_cost_rules` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `parking_id` bigint DEFAULT NULL COMMENT '停车场ID',
  `cost_rules_id` bigint DEFAULT NULL COMMENT '收费规则ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_parking_cost_rules` (`parking_id`, `cost_rules_id`),
  KEY `idx_cost_rules_id` (`cost_rules_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='停车场收费规则关联';

-- 停车记录
CREATE TABLE `pk_car_log` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `app_id` bigint DEFAULT NULL COMMENT '应用ID',
  `parking_id` bigint DEFAULT NULL COMMENT '停车场ID',
  `car_number` varchar(20) DEFAULT NULL COMMENT '车牌号码',
  `entry_time` datetime DEFAULT NULL COMMENT '入场时间',
  `out_time` datetime DEFAULT NULL COMMENT '出场时间',
  `status` int DEFAULT NULL COMMENT '状态{0:场内,1:已出场}',
  `car_type` varchar(20) DEFAULT NULL COMMENT '车辆类型',
  `out_channel_id` bigint DEFAULT NULL COMMENT '出场通道ID',
  `out_device_code` varchar(64) DEFAULT NULL COMMENT '出场识别设备编码',
  `out_recognize_time` datetime DEFAULT NULL COMMENT '出场识别时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_parking_id` (`parking_id`),
  KEY `idx_car_number` (`car_number`),
  KEY `idx_status` (`status`),
  KEY `idx_entry_time` (`entry_time`),
  KEY `idx_out_channel_id` (`out_channel_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='停车记录';


-- ============================================================
-- 三、会员模块 (me_*)
-- ============================================================

-- 会员
CREATE TABLE `me_member` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `app_id` bigint DEFAULT NULL COMMENT '所属APPID',
  `plate` varchar(32) DEFAULT NULL COMMENT '车牌号',
  `name` varchar(64) DEFAULT NULL COMMENT '车主姓名',
  `phone` varchar(32) DEFAULT NULL COMMENT '手机号',
  `card_type` int DEFAULT NULL COMMENT '卡类型{1:月卡,2:季卡,3:年卡}',
  `expire_time` datetime DEFAULT NULL COMMENT '到期时间',
  `frozen` int NOT NULL DEFAULT 0 COMMENT '是否冻结{0:正常,1:冻结}',
  `total_amount` decimal(18,2) DEFAULT 0.00 COMMENT '累计充值',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_plate` (`plate`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员';

-- 会员套餐
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
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员套餐';

-- 会员续费记录
CREATE TABLE `me_renewal_record` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `app_id` bigint DEFAULT NULL COMMENT '所属APPID',
  `order_no` varchar(64) DEFAULT NULL COMMENT '订单号',
  `member_id` bigint DEFAULT NULL COMMENT '会员ID',
  `plate` varchar(32) DEFAULT NULL COMMENT '车牌号',
  `name` varchar(64) DEFAULT NULL COMMENT '车主姓名',
  `card_type` int DEFAULT NULL COMMENT '卡类型{1:月卡,2:季卡,3:年卡}',
  `amount` decimal(18,2) DEFAULT NULL COMMENT '续费金额',
  `operator` varchar(64) DEFAULT NULL COMMENT '操作员',
  `renewal_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '续费时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员续费记录';


-- ============================================================
-- 四、订单模块 (or_*)
-- ============================================================

-- 订单
CREATE TABLE `or_order` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `order_no` varchar(50) DEFAULT NULL COMMENT '订单编号',
  `app_id` bigint DEFAULT NULL COMMENT '所属APPID',
  `store_id` bigint DEFAULT NULL COMMENT '所属门店ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `total_amount` decimal(18,2) DEFAULT NULL COMMENT '总订单总金额 = 所有订单详情总计',
  `discount_amount` decimal(18,2) DEFAULT NULL COMMENT '总优惠金额 = 所有订单详情优惠 + 订单优惠',
  `pay_amount` decimal(18,2) DEFAULT NULL COMMENT '实付金额 = 总订单总金额-总优惠金额',
  `pay_amounted` decimal(18,2) DEFAULT NULL COMMENT '已付金额',
  `pay_status` int NOT NULL DEFAULT 0 COMMENT '支付状态{0:未支付,1:支付部分,2:已支付,3:部分退款,4:全部退款}',
  `pay_time` datetime DEFAULT NULL COMMENT '最后支付时间',
  `delivery_status` int NOT NULL DEFAULT 0 COMMENT '发货状态{1:待发货,2:已发货,3:已签收,4:退货中,5:退货签收}',
  `delivery_time` datetime DEFAULT NULL COMMENT '发货时间',
  `order_status` int NOT NULL DEFAULT 0 COMMENT '订单状态{0:待付款,2:待发货,3:待收货,4:已完成,5:申请售后,6:取消中,7:已取消}',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '订单备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_order_status` (`order_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单';

-- 订单详情
CREATE TABLE `or_order_detail` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `order_id` bigint DEFAULT NULL COMMENT '订单ID',
  `business_id` bigint DEFAULT NULL COMMENT '所属业务ID,比如停车场ID、商品ID',
  `product_name` varchar(100) DEFAULT NULL COMMENT '商品名称',
  `product_image` varchar(255) DEFAULT NULL COMMENT '商品图片',
  `product_price` decimal(18,2) DEFAULT NULL COMMENT '商品单价',
  `quantity` int DEFAULT NULL COMMENT '购买数量',
  `total_amount` decimal(18,2) DEFAULT NULL COMMENT '订单总金额',
  `discount_amount` decimal(18,2) DEFAULT NULL COMMENT '优惠金额',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单详情';

-- 订单支付记录
CREATE TABLE `or_order_pay` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `order_id` bigint DEFAULT NULL COMMENT '订单ID',
  `pay_no` varchar(50) DEFAULT NULL COMMENT '支付流水号',
  `pay_type` int DEFAULT NULL COMMENT '支付方式{1:支付宝,2:微信支付,3:银行卡,4:现金}',
  `pay_amount` decimal(18,2) DEFAULT NULL COMMENT '支付金额',
  `pay_status` int NOT NULL DEFAULT 0 COMMENT '支付状态{0:未支付,1:支付中,2:支付成功,3:支付失败}',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `callback_time` datetime DEFAULT NULL COMMENT '回调时间',
  `callback_content` text COMMENT '回调内容',
  `refund_no` varchar(50) DEFAULT NULL COMMENT '退款流水号',
  `refund_amount` decimal(18,2) DEFAULT NULL COMMENT '退款金额',
  `refund_time` datetime DEFAULT NULL COMMENT '退款时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_pay_no` (`pay_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单支付记录';

-- 订单状态日志
CREATE TABLE `or_order_status_log` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `order_id` bigint DEFAULT NULL COMMENT '订单ID',
  `order_status` int DEFAULT NULL COMMENT '订单状态',
  `before_status` int DEFAULT NULL COMMENT '变更前状态',
  `remark` varchar(500) DEFAULT NULL COMMENT '变更说明',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `operator_type` int DEFAULT NULL COMMENT '操作人类型{1:系统,2:用户,3:管理员}',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单状态日志';

-- 优惠券
CREATE TABLE `or_coupon` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `app_id` bigint DEFAULT NULL COMMENT '所属APPID',
  `name` varchar(255) DEFAULT NULL COMMENT '优惠券名称',
  `type` int DEFAULT NULL COMMENT '优惠券类型{1:满减券,2:折扣券,3:免费时长券}',
  `threshold` decimal(18,2) DEFAULT NULL COMMENT '使用门槛金额(0表示无门槛)',
  `discount` decimal(18,2) DEFAULT NULL COMMENT '优惠值:满减金额/折扣率/免费时长(分钟)',
  `total_count` int DEFAULT NULL COMMENT '发放数量',
  `used_count` int NOT NULL DEFAULT 0 COMMENT '已使用数量',
  `valid_days` int DEFAULT NULL COMMENT '领取后有效天数',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态{0:停用,1:启用}',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券';


-- ============================================================
-- 五、设备模块 (me_device*)
-- ============================================================

-- 设备
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
  PRIMARY KEY (`id`),
  KEY `idx_parking_id` (`parking_id`),
  KEY `idx_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备';

-- 设备故障工单
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
  PRIMARY KEY (`id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_parking_id` (`parking_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备故障工单';
