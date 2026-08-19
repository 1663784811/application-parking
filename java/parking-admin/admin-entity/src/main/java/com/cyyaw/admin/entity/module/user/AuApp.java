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
@Schema(description = "企业APP")
@Table(name = "au_app")
@EqualsAndHashCode(callSuper = true)
public class AuApp extends BaseEntity {

    @Column(name = "code", columnDefinition = "varchar(50) unique COMMENT 'APP编号'")
    private String code;

    @Column(name = "name", columnDefinition = "varchar(100) COMMENT 'APP名称'")
    private String name;

    @Column(name = "logo", columnDefinition = "varchar(255) COMMENT 'logo'")
    private String logo;

    @Column(name = "type", columnDefinition = "varchar(64) COMMENT '类型'")
    private String type;
} 