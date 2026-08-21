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
@Schema(description = "操作日志")
@Table(name = "au_log")
@EqualsAndHashCode(callSuper = true)
public class AuLog extends BaseEntity {

    @Column(name = "admin_id", columnDefinition = "bigint COMMENT '操作人ID'")
    private Long adminId;

    @Column(name = "admin_name", columnDefinition = "varchar(50) COMMENT '操作人账号'")
    private String adminName;

    @Column(name = "log_type", columnDefinition = "varchar(20) COMMENT '日志类型{login:登录日志,operation:操作日志}'")
    private String logType;

    @Column(name = "action_type", columnDefinition = "varchar(30) COMMENT '操作类型{login,edit,delete,export,open_gate,refund,edit_rule,blacklist}'")
    private String actionType;

    @Column(name = "description", columnDefinition = "varchar(500) COMMENT '操作描述'")
    private String description;

    @Column(name = "ip", columnDefinition = "varchar(50) COMMENT '操作IP地址'")
    private String ip;

    @Column(name = "user_agent", columnDefinition = "varchar(500) COMMENT '浏览器/设备信息'")
    private String userAgent;

    @Column(name = "status", columnDefinition = "int default 1 COMMENT '状态{0:失败,1:成功}'")
    private Integer status;

}