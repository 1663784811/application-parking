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
} 