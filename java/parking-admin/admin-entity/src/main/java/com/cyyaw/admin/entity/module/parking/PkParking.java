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
@Schema(description = "停车场")
@Table(name = "pk_parking")
@EqualsAndHashCode(callSuper = true)
public class PkParking extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "app_id", columnDefinition = "bigint COMMENT '应用ID'")
    private Long appId;

    @Column(name = "name", columnDefinition = "varchar(255) COMMENT '停车场名称'")
    private String name;

    @Column(name = "long_lat", columnDefinition = "varchar(64) COMMENT '经纬度'")
    private String longLat;

    @Column(name = "address", columnDefinition = "varchar(255) COMMENT '位置'")
    private String address;

    @Column(name = "image", columnDefinition = "varchar(255) COMMENT '停车场图片'")
    private String image;

    @Column(name = "capacity", columnDefinition = "int COMMENT '车位容量'")
    private Integer capacity;

    @Column(name = "opening_up", columnDefinition = "int COMMENT '是否对外开放{0:对外开放,1:不开放}'")
    private Integer openingUp;

}