package com.cyyaw.admin.entity.module.user;

import com.cyyaw.admin.entity.utils.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Schema(description = "企业用户")
@Table(name = "au_user")
@EqualsAndHashCode(callSuper = true)
public class AuUser extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "app_id", columnDefinition = "bigint COMMENT '所属APPID'")
    private Long appId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "store_id", columnDefinition = "bigint COMMENT '所属门店ID'")
    private Long storeId;

    // =======================================================
    // =======================================================
    // =======================================================

    @Column(name = "nick_name", columnDefinition = "varchar(50) COMMENT '昵称'")
    private String nickName;

    @Column(name = "account", columnDefinition = "varchar(50) not null COMMENT '账号'")
    private String account;

    @JsonIgnore
    @Column(name = "password", columnDefinition = "varchar(100) not null COMMENT '密码'")
    private String password;

    @Column(name = "face", columnDefinition = "text COMMENT '头像'")
    private String face;

    @Column(name = "real_name", columnDefinition = "varchar(50) COMMENT '真实姓名'")
    private String realName;

    @Column(name = "gender", columnDefinition = "int COMMENT '性别{0:未知,1:男,2:女}'")
    private Integer gender;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "birthday", columnDefinition = "date COMMENT '生日'")
    private LocalDate birthday;

    @Column(name = "email", columnDefinition = "varchar(100) COMMENT '邮箱'")
    private String email;

    @Column(name = "phone", columnDefinition = "varchar(20) COMMENT '电话'")
    private String phone;

    @Column(name = "status", columnDefinition = "int default 1 COMMENT '状态{0:禁用,1:启用}'")
    private Integer status;

    @Column(name = "last_login_time", columnDefinition = "datetime COMMENT '最后登录时间'")
    private LocalDateTime lastLoginTime;

    @Column(name = "introduction", columnDefinition = "varchar(255) COMMENT '个人简介'")
    private String introduction;

} 