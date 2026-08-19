package com.cyyaw.admin.entity.module.user;

import com.cyyaw.admin.entity.utils.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@Entity
@Schema(description = "菜单")
@Table(name = "au_menu")
@EqualsAndHashCode(callSuper = true)
public class AuMenu extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "app_id", columnDefinition = "bigint COMMENT '所属APPID'")
    private Long appId;

    //===========================
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "pid", columnDefinition = "bigint COMMENT '上级'")
    private Long pid;

    @JsonIgnore
    @Column(name = "system_role", columnDefinition = "varchar(10) COMMENT '系统角色'")
    private String systemRole;

    @Column(name = "name", columnDefinition = "varchar(255) COMMENT '标题'")
    private String name;

    @Column(name = "icon", columnDefinition = "varchar(255) COMMENT 'icon'")
    private String icon;

    @Column(name = "route_name", columnDefinition = "varchar(255) COMMENT '路由'")
    private String routeName;

    @Column(name = "params", columnDefinition = "text COMMENT 'params参数'")
    private String params;

    @Column(name = "sort", columnDefinition = "int COMMENT '排序'")
    private Integer sort;

    @Column(name = "show_menu", columnDefinition = "int default 1 COMMENT '显示菜单{0:隐藏,1:显示}'")
    private Integer showMenu;

} 