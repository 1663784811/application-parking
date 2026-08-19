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
@Schema(description = "企业")
@Table(name = "au_enterprise")
@EqualsAndHashCode(callSuper = true)
public class AuEnterprise extends BaseEntity {

    @Column(name = "name", columnDefinition = "varchar(100) COMMENT '企业名称'")
    private String name;

    @Column(name = "logo", columnDefinition = "varchar(255) COMMENT 'logo'")
    private String logo;

    @Column(name = "code", columnDefinition = "varchar(50) unique not null  COMMENT '企业编码'")
    private String code;

    @Column(name = "person", columnDefinition = "varchar(50) COMMENT '联系人'")
    private String person;

    @Column(name = "root_account", columnDefinition = "varchar(50) COMMENT '企业管理员账号'")
    private String rootAccount;

    @Column(name = "phone", columnDefinition = "varchar(20) unique COMMENT '联系电话'")
    private String phone;

    @Column(name = "address", columnDefinition = "varchar(255) COMMENT '地址'")
    private String address;

    @Column(name = "status", columnDefinition = "int default 1 COMMENT '状态{0:禁用,1:启用}'")
    private Integer status;
} 