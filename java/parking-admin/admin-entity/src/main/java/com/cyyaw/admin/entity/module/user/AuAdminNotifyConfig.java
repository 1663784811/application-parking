package com.cyyaw.admin.entity.module.user;

import com.cyyaw.admin.entity.utils.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Schema(description = "管理员通知设置")
@Table(name = "au_admin_notify_config")
@EqualsAndHashCode(callSuper = true)
public class AuAdminNotifyConfig extends BaseEntity {

    @Column(name = "admin_id", columnDefinition = "bigint COMMENT '管理员ID'")
    private Long adminId;

    @Column(name = "bill", columnDefinition = "int default 1 COMMENT '账单通知{0:关闭,1:开启}'")
    private Integer bill;

    @Column(name = "alert", columnDefinition = "int default 1 COMMENT '异常告警{0:关闭,1:开启}'")
    private Integer alert;

    @Column(name = "announcement", columnDefinition = "int default 1 COMMENT '系统公告{0:关闭,1:开启}'")
    private Integer announcement;

    @Column(name = "security", columnDefinition = "int default 1 COMMENT '安全提醒{0:关闭,1:开启}'")
    private Integer security;

}