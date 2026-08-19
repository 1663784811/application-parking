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
@Schema(description = "企业管理员角色")
@Table(name = "au_admin_role")
@EqualsAndHashCode(callSuper = true)
public class AuAdminRole extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "admin_id", columnDefinition = "bigint COMMENT '管理员ID'")
    private Long adminId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "role_id", columnDefinition = "bigint COMMENT '角色ID'")
    private Long roleId;
} 