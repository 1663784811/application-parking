package com.cyyaw.admin.entity.module.parking;

import com.cyyaw.admin.entity.utils.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@Entity
@Schema(description = "车位")
@Table(name = "pk_space")
@EqualsAndHashCode(callSuper = true)
public class PkSpace extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "app_id", columnDefinition = "bigint COMMENT '应用ID'")
    private Long appId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "parking_id", columnDefinition = "bigint COMMENT '停车场ID'")
    private Long parkingId;

    @Column(name = "space_no", columnDefinition = "varchar(64) COMMENT '车位编号'")
    private String spaceNo;

    @Column(name = "area", columnDefinition = "varchar(32) COMMENT '区域'")
    private String area;

    @Column(name = "space_type", columnDefinition = "int COMMENT '车位类型{1:固定,2:临时,3:无障碍}'")
    private Integer spaceType;

    @Column(name = "status", columnDefinition = "int default 0 COMMENT '状态{0:空闲,1:占用,2:故障}'")
    private Integer status;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "member_id", columnDefinition = "bigint COMMENT '绑定会员ID'")
    private Long memberId;

    @Column(name = "member_name", columnDefinition = "varchar(64) COMMENT '绑定车主姓名'")
    private String memberName;

    @Column(name = "plate", columnDefinition = "varchar(32) COMMENT '绑定车牌'")
    private String plate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "expire_date", columnDefinition = "date COMMENT '有效期至'")
    private LocalDate expireDate;

}
