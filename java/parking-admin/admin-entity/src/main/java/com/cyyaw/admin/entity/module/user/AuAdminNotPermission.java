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
@Schema(description = "禁用管理员权限")
@Table(name = "au_admin_not_permission")
@EqualsAndHashCode(callSuper = true)
public class AuAdminNotPermission extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "admin_id", columnDefinition = "bigint COMMENT '管理员ID'")
    private Long adminId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "permission_id", columnDefinition = "bigint COMMENT '权限ID'")
    private Long permissionId;

} 