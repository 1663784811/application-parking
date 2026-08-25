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
@Schema(description = "会员续费记录")
@Table(name = "me_renewal_record")
@EqualsAndHashCode(callSuper = true)
public class MeRenewalRecord extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "app_id", columnDefinition = "bigint COMMENT '所属APPID'")
    private Long appId;

    @Column(name = "order_no", columnDefinition = "varchar(64) COMMENT '订单号'")
    private String orderNo;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "member_id", columnDefinition = "bigint COMMENT '会员ID'")
    private Long memberId;

    @Column(name = "plate", columnDefinition = "varchar(32) COMMENT '车牌号'")
    private String plate;

    @Column(name = "name", columnDefinition = "varchar(64) COMMENT '车主姓名'")
    private String name;

    @Column(name = "card_type", columnDefinition = "int COMMENT '卡类型{1:月卡,2:季卡,3:年卡}'")
    private Integer cardType;

    @Column(name = "amount", columnDefinition = "decimal(18,2) COMMENT '续费金额'")
    private BigDecimal amount;

    @Column(name = "operator", columnDefinition = "varchar(64) COMMENT '操作员'")
    private String operator;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "renewal_time", columnDefinition = "datetime COMMENT '续费时间'")
    private LocalDateTime renewalTime;

}
