package com.cyyaw.admin.entity.module.iot;

import com.cyyaw.admin.entity.utils.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@Entity
@Schema(description = "mqtt账号信息")
@Table(name = "iot_mqtt_info")
@EqualsAndHashCode(callSuper = true)
public class IotMqttInfo extends BaseEntity {

    @Column(name = "app_id", columnDefinition = "bigint COMMENT '所属APPID'")
    private Long appId;

    @Column(name = "store_id", columnDefinition = "bigint COMMENT '门店ID'")
    private Long storeId;

    @Basic
    @Column(name = "user_name", columnDefinition = "varchar(32) default '' COMMENT '用户名'")
    private String userName;

    @Basic
    @Column(name = "passwrod", columnDefinition = "varchar(64) default '' COMMENT '密码'")
    private String passwrod;

    @Basic
    @Column(name = "business", columnDefinition = "varchar(32) default '' COMMENT '业务'")
    private String business;

    @Basic
    @Column(name = "role", columnDefinition = "varchar(32) default '' COMMENT '角色'")
    private String role;

}
