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
@Schema(description = "企业部门")
@Table(name = "au_department")
@EqualsAndHashCode(callSuper = true)
public class AuDepartment extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "pid", columnDefinition = "bigint COMMENT '上级部门ID'")
    private Long pid;

    @Column(name = "name", columnDefinition = "varchar(50) COMMENT '名称'")
    private String name;

    @Column(name = "code", columnDefinition = "varchar(50) COMMENT '编码'")
    private String code;
    
}