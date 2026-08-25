package com.cyyaw.admin.entity.module.parking;

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
@Schema(description = "停车场通道")
@Table(name = "pk_channel")
@EqualsAndHashCode(callSuper = true)
public class PkChannel extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "app_id", columnDefinition = "bigint COMMENT '应用ID'")
    private Long appId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "parking_id", columnDefinition = "bigint COMMENT '所属停车场ID'")
    private Long parkingId;

    @Column(name = "code", columnDefinition = "varchar(64) COMMENT '通道编号'")
    private String code;

    @Column(name = "name", columnDefinition = "varchar(255) COMMENT '通道名称'")
    private String name;

    @Column(name = "type", columnDefinition = "varchar(16) COMMENT '通道类型{in:入口,out:出口,inout:出入口}'")
    private String type;

    @Column(name = "ip", columnDefinition = "varchar(64) COMMENT '设备IP地址'")
    private String ip;

    @Column(name = "status", columnDefinition = "int not null default 1 COMMENT '状态{0:故障,1:正常}'")
    private Integer status;

}
