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
@Schema(description = "系统配置")
@Table(name = "conf_sys")
@EqualsAndHashCode(callSuper = true)
public class ConfSys extends BaseEntity {

    @Column(name = "code", columnDefinition = "varchar(128) unique COMMENT '系统配置'")
    private String code;

    @Column(name = "val", columnDefinition = "varchar(255) unique COMMENT '配置值'")
    private String val;

} 