package com.cyyaw.admin.entity.module.member;

import com.cyyaw.admin.entity.utils.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Schema(description = "会员")
@Table(name = "me_member")
@EqualsAndHashCode(callSuper = true)
public class MeMember extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "app_id", columnDefinition = "bigint COMMENT '所属APPID'")
    private Long appId;

    @Column(name = "plate", columnDefinition = "varchar(32) COMMENT '车牌号'")
    private String plate;

    @Column(name = "name", columnDefinition = "varchar(64) COMMENT '车主姓名'")
    private String name;

    @Column(name = "phone", columnDefinition = "varchar(32) COMMENT '手机号'")
    private String phone;

    @Column(name = "card_type", columnDefinition = "int COMMENT '卡类型{1:月卡,2:季卡,3:年卡}'")
    private Integer cardType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "expire_time", columnDefinition = "datetime COMMENT '到期时间'")
    private LocalDateTime expireTime;

    @Column(name = "frozen", columnDefinition = "int default 0 COMMENT '是否冻结{0:正常,1:冻结}'")
    private Integer frozen;

    @Column(name = "total_amount", columnDefinition = "decimal(18,2) default 0 COMMENT '累计充值'")
    private BigDecimal totalAmount;

}
