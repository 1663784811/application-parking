package com.cyyaw.admin.entity.utils;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @PrePersist：插入数据库前执行（新增操作）
 * @PreUpdate：更新数据库前执行（修改操作）
 * @PostPersist：插入后执行
 * @PostUpdate：更新后执行
 */
@Data
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    @Id
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint PRIMARY KEY NOT NULL COMMENT 'id'")
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "en_id", columnDefinition = "bigint not null COMMENT '所属企业ID'")
    private Long enId;

    @TableField(fill = FieldFill.INSERT)
    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "create_time", columnDefinition = "datetime default now() COMMENT '创建时间'")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @LastModifiedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "update_time", columnDefinition = "datetime default now() COMMENT '更新时间'")
    private LocalDateTime updateTime;

    @Basic
    @Column(name = "note", columnDefinition = "varchar(255) default '' COMMENT '备注'")
    private String note;

    @Column(name = "del_time", columnDefinition = "int not null default 0 COMMENT '删除时间'")
    private Integer delTime;


    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        LoginInfo loginInfo = LoginInfoContext.getLoginInfo();
        if (null != loginInfo && this.enId == null) {
            this.enId = loginInfo.getEnId();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateTime = LocalDateTime.now();
    }

} 