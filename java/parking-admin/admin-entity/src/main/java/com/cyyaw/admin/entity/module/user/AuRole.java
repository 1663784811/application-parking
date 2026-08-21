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
@Schema(description = "角色")
@Table(name = "au_role")
@EqualsAndHashCode(callSuper = true)
public class AuRole extends BaseEntity {

    @Column(name = "name", columnDefinition = "varchar(50) COMMENT '名称'")
    private String name;

    @Column(name = "code", columnDefinition = "varchar(50) COMMENT '编码'")
    private String code;

    @Column(name = "description", columnDefinition = "varchar(255) COMMENT '描述'")
    private String description;

    @Column(name = "is_system", columnDefinition = "int default 0 COMMENT '是否系统角色{0:自定义,1:系统}'")
    private Integer isSystem;

}