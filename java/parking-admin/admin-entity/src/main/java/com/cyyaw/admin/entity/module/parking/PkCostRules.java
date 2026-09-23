package com.cyyaw.admin.entity.module.parking;

import com.cyyaw.admin.entity.utils.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
@Schema(description = "停车场收费规则")
@Table(name = "pk_cost_rules")
@EqualsAndHashCode(callSuper = true)
public class PkCostRules extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "app_id", columnDefinition = "bigint COMMENT '应用ID'")
    private Long appId;

    // ===============================================================

    @Column(name = "name", columnDefinition = "varchar(255) COMMENT '规则名称'")
    private String name;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "effective_start_time", columnDefinition = "date COMMENT '规则有效开始日期'")
    private LocalDate effectiveStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "effective_end_time", columnDefinition = "date COMMENT '规则有效结束日期'")
    private LocalDate effectiveEndTime;

    @Column(name = "car_type", columnDefinition = "varchar(32) not null COMMENT '车辆类型{0:小型汽车,1:中型汽车,2:大型汽车}'")
    private String carType;

    @Column(name = "type", columnDefinition = "int not null COMMENT '收费类型{0:首段收费,2:计费时段,3:每天封顶金额,5:每次封顶金额}'")
    private Integer type;

    @Column(name = "week", columnDefinition = "varchar(255) COMMENT '星期{Monday:周一,Tuesday:周二,Wednesday:周三,Thursday:周四,Friday:周五,Saturday:周六,Sunday:周日}'")
    private String week;

    @JsonFormat(pattern = "HH:mm:ss")
    @Column(name = "start_time", columnDefinition = "time COMMENT '开始时间'")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm:ss")
    @Column(name = "end_time", columnDefinition = "time COMMENT '结束时间'")
    private LocalTime endTime;

    @Column(name = "rule_time", columnDefinition = "int COMMENT '规则时长(分钟)'")
    private Integer ruleTime;

    @Column(name = "amount", columnDefinition = "decimal(18,2) COMMENT '金额'")
    private BigDecimal amount;

}