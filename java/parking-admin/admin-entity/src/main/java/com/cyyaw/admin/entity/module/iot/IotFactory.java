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
@Schema(description = "厂商")
@Table(name = "iot_factory")
@EqualsAndHashCode(callSuper = true)
public class IotFactory extends BaseEntity {

    @Basic
    @Column(name = "name", columnDefinition = "varchar(255) default '' COMMENT '名称'")
    private String name;

    @Basic
    @Column(name = "address", columnDefinition = "varchar(255) default '' COMMENT '地址'")
    private String address;

    @Basic
    @Column(name = "contact", columnDefinition = "varchar(64) default '' COMMENT '联系人'")
    private String contact;

    @Basic
    @Column(name = "phone", columnDefinition = "varchar(20) default '' COMMENT '电话'")
    private String phone;


}
