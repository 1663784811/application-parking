package com.cyyaw.admin.entity.module.user;

import com.cyyaw.admin.entity.utils.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@Entity
@Schema(description = "门店")
@Table(name = "au_store")
@EqualsAndHashCode(callSuper = true)
public class AuStore extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "app_id", columnDefinition = "bigint COMMENT '所属APPID'")
    private Long appId;

    @Column(name = "name", columnDefinition = "varchar(100) COMMENT '门店名称'")
    private String name;

    @Column(name = "code", columnDefinition = "varchar(50) COMMENT '门店编码'")
    private String code;

    @Column(name = "contact_person", columnDefinition = "varchar(50) COMMENT '联系人'")
    private String contactPerson;

    @Column(name = "contact_phone", columnDefinition = "varchar(20) COMMENT '联系电话'")
    private String contactPhone;

    @Column(name = "address", columnDefinition = "varchar(255) COMMENT '地址'")
    private String address;

    @Column(name = "introduction", columnDefinition = "text COMMENT '个人简介'")
    private String introduction;

    @Column(name = "certificate_type", columnDefinition = "int COMMENT '证件类型{0:身份证,1:营业执照}'")
    private String certificateType;

    @Column(name = "certificate", columnDefinition = "text COMMENT '证件'")
    private String certificate;

    @Column(name = "status", columnDefinition = "int default 1 COMMENT '状态{0:提交审核,1:正常,2:禁用}'")
    private Integer status;
} 