-- ============================================================
-- application-user 模块数据库脚本
-- 数据库：parking（与 parking-gate 共用）
-- 手动执行，不在应用启动时自动运行
-- ============================================================

CREATE TABLE IF NOT EXISTS `user` (
  `id`          bigint        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tid`         varchar(32)   NOT NULL COMMENT '业务主键',
  `account`     varchar(64)   NOT NULL COMMENT '账号',
  `password`    varchar(128)  NOT NULL COMMENT '密码（BCrypt）',
  `name`        varchar(64)   DEFAULT NULL COMMENT '姓名',
  `phone`       varchar(20)   DEFAULT NULL COMMENT '手机号',
  `status`      tinyint       NOT NULL DEFAULT 1 COMMENT '状态：1 正常，0 禁用',
  `create_time` datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account` (`account`),
  UNIQUE KEY `uk_tid` (`tid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- 种子用户：admin / 123456（与 gate_admin 演示账号一致）
INSERT INTO `user` (`tid`, `account`, `password`, `name`)
VALUES ('u_admin_0001', 'admin',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWe',
        '管理员')
ON DUPLICATE KEY UPDATE `account` = `account`;
