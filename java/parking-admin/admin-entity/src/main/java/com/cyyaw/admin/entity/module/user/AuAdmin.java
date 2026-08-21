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
@Schema(description = "企业管理员")
@Table(name = "au_admin")
@EqualsAndHashCode(callSuper = true)
public class AuAdmin extends BaseEntity {

    @Column(name = "account", columnDefinition = "varchar(50) COMMENT '账号'")
    private String account;

    @Column(name = "phone", columnDefinition = "varchar(15) COMMENT '手机号'")
    private String phone;

    @Column(name = "nick_name", columnDefinition = "varchar(50) COMMENT '昵称'")
    private String nickName;

    @JsonIgnore
    @Column(name = "password", columnDefinition = "varchar(100) COMMENT '密码'")
    private String password;

    @Column(name = "avatar", columnDefinition = "varchar(255) COMMENT '头像'")
    private String avatar;

    @Column(name = "real_name", columnDefinition = "varchar(50) COMMENT '真实姓名'")
    private String realName;

    @Column(name = "gender", columnDefinition = "int COMMENT '性别{0:未知,1:男,2:女}'")
    private Integer gender;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "birthday", columnDefinition = "date COMMENT '生日'")
    private LocalDate birthday;

    @Column(name = "email", columnDefinition = "varchar(100) COMMENT '邮箱'")
    private String email;

    @Column(name = "status", columnDefinition = "int default 1 COMMENT '状态{0:禁用,1:启用}'")
    private Integer status;

    @Column(name = "last_login_time", columnDefinition = "datetime COMMENT '最后登录时间'")
    private LocalDateTime lastLoginTime;

}