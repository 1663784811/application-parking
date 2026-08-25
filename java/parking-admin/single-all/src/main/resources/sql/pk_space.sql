-- 车位表（平面图视图）
-- id 由 MyBatis-Plus 雪花算法生成（id-type: ASSIGN_ID），非自增
CREATE TABLE `pk_space` (
  `id`          bigint        NOT NULL                    COMMENT 'id',
  `en_id`       bigint        NOT NULL DEFAULT 0          COMMENT '所属企业ID',
  `app_id`      bigint        DEFAULT NULL                COMMENT '应用ID',
  `parking_id`  bigint        DEFAULT NULL                COMMENT '停车场ID',
  `space_no`    varchar(64)   DEFAULT NULL                COMMENT '车位编号',
  `area`        varchar(32)   DEFAULT NULL                COMMENT '区域',
  `space_type`  int           DEFAULT NULL                COMMENT '车位类型{1:固定,2:临时,3:无障碍}',
  `status`      int           NOT NULL DEFAULT 0          COMMENT '状态{0:空闲,1:占用,2:故障}',
  `member_id`   bigint        DEFAULT NULL                COMMENT '绑定会员ID',
  `member_name` varchar(64)   DEFAULT NULL                COMMENT '绑定车主姓名',
  `plate`       varchar(32)   DEFAULT NULL                COMMENT '绑定车牌',
  `expire_date` date          DEFAULT NULL                COMMENT '有效期至',
  `create_time` datetime      DEFAULT CURRENT_TIMESTAMP              COMMENT '创建时间',
  `update_time` datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `note`        varchar(255)  DEFAULT ''                  COMMENT '备注',
  `del_time`    int           NOT NULL DEFAULT 0          COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_parking`  (`parking_id`),
  KEY `idx_space_no` (`space_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车位';

-- 示例数据（城西停车场 id=1）：A 区 5 个车位
-- 实际 parking_id 请替换为 pk_parking 中真实 id
INSERT INTO `pk_space` (`id`, `en_id`, `parking_id`, `space_no`, `area`, `space_type`, `status`, `member_id`, `member_name`, `plate`, `expire_date`)
VALUES
  (1001, 0, 1, 'A001', 'A区', 1, 0, NULL, NULL, NULL, NULL),
  (1002, 0, 1, 'A002', 'A区', 1, 0, NULL, NULL, NULL, NULL),
  (1003, 0, 1, 'A003', 'A区', 2, 0, NULL, NULL, NULL, NULL),
  (1004, 0, 1, 'A004', 'A区', 2, 0, NULL, NULL, NULL, NULL),
  (1005, 0, 1, 'A005', 'A区', 3, 2, NULL, NULL, NULL, NULL),
  (1006, 0, 1, 'B001', 'B区', 1, 0, NULL, NULL, NULL, NULL),
  (1007, 0, 1, 'B002', 'B区', 2, 0, NULL, NULL, NULL, NULL),
  (1008, 0, 1, 'B003', 'B区', 2, 0, NULL, NULL, NULL, NULL);
