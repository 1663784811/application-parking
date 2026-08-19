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
@Schema(description = "用户地址")
@Table(name = "au_user_address")
@EqualsAndHashCode(callSuper = true)
public class AuUserAddress extends BaseEntity {


    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "user_id", columnDefinition = "bigint COMMENT '用户ID'")
    private Long userId;
    // =======================================================
    // =======================================================
    // =======================================================

    @Column(name = "name", columnDefinition = "varchar(20) COMMENT '名称'")
    private String name;

    @Column(name = "phone", columnDefinition = "varchar(15) COMMENT '手机号'")
    private String phone;

    @Column(name = "country", columnDefinition = "varchar(15) COMMENT '国家'")
    private String country;

    @Column(name = "province", columnDefinition = "varchar(15) COMMENT '省'")
    private String province;

    @Column(name = "city", columnDefinition = "varchar(15) COMMENT '城市'")
    private String city;

    @Column(name = "town", columnDefinition = "varchar(15) COMMENT '城镇(地级市)'")
    private String town;

    @Column(name = "address", columnDefinition = "varchar(255) COMMENT '详细地址'")
    private String address;

    @Column(name = "def", columnDefinition = "int default 0 COMMENT '是否默认地址{0:不默认,1:默认}'")
    private Integer def;

} 