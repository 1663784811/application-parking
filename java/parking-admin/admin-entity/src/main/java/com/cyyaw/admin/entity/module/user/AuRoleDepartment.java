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
@Schema(description = "角色部门")
@Table(name = "au_role_department")
@EqualsAndHashCode(callSuper = true)
public class AuRoleDepartment extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "role_id", columnDefinition = "bigint COMMENT '角色ID'")
    private Long roleId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "department_id", columnDefinition = "bigint COMMENT '部门ID'")
    private Long departmentId;
} 