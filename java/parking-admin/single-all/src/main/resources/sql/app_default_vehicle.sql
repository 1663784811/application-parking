-- 我的车辆表
-- H5「我的车辆」的落地表。
--
-- 为什么不复用 me_member：me_member 是「会员卡」——card_type 月卡/季卡/年卡、expire_time 到期时间，
-- 是付费订阅；「我的车辆」是车主把车牌登记到自己的账号下，两者生命周期完全不同（换车、注销车牌都
-- 不该动会员卡）。所以单独一张表，me_member 继续只承载会员卡 / 卡包。
--
-- 归属规则：user_id 优先（账号维度），未登录时退回 phone（手机号维度）。
-- 两个维度都要，是因为车牌识别在出入口没有登录态，需要按 en_id 找到车主、再匹配手机号或账号 ID。
--
-- id 由 MyBatis-Plus 雪花算法分配（id-type: ASSIGN_ID），非自增。
-- 审计字段（en_id/create_time/update_time/del_time）均设 DB 级默认值：
-- MetaObjectHandler 未注册，@TableField(fill=...) 自动填充不生效，
-- 而 MP 插入时省略 null 字段（FieldStrategy.NOT_NULL），故由 DB 默认值兜底。
--
-- 列名用 plate（不是 plate_number）：与 me_member 的车牌列名保持一致；
-- app_default_vehicle 只表示「我登记了这辆车」，me_member 才是付费会员卡，两个 plate 是不同语义、不同表。
-- del_time 语义：0 未删除，>0 已删除时间戳。与库内其他表一致，手工过滤（未开 MP 逻辑删除）。
CREATE TABLE `app_default_vehicle` (
  `id` bigint NOT NULL COMMENT 'id',
  `en_id` bigint NOT NULL DEFAULT 0 COMMENT '所属企业ID',
  `app_id` bigint DEFAULT NULL COMMENT '所属APPID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID（au_user.id，未登录时为 NULL）',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号（未登录时的归属维度）',
  `plate` varchar(20) NOT NULL COMMENT '车牌号',
  `vehicle_type` varchar(50) DEFAULT '小型汽车' COMMENT '车辆类型',
  `is_default` int NOT NULL DEFAULT 0 COMMENT '是否默认车辆{0:否,1:是}',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note` varchar(255) DEFAULT '' COMMENT '备注',
  `del_time` int NOT NULL DEFAULT 0 COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_en_user` (`en_id`, `user_id`),
  KEY `idx_phone` (`phone`),
  KEY `idx_plate` (`plate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='我的车辆';
