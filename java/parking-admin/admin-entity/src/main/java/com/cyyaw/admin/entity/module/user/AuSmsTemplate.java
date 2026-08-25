package com.cyyaw.admin.entity.module.user;

import com.cyyaw.admin.entity.utils.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Schema(description = "短信模板")
@Table(name = "au_sms_template")
@EqualsAndHashCode(callSuper = true)
public class AuSmsTemplate extends BaseEntity {

    @Column(name = "name", columnDefinition = "varchar(255) COMMENT '模板名称'")
    private String name;

    @Column(name = "type", columnDefinition = "int COMMENT '模板类型{1:到期提醒,2:欠费催缴,3:入场通知,4:出场通知}'")
    private Integer type;

    @Column(name = "content", columnDefinition = "varchar(500) COMMENT '短信内容'")
    private String content;

    @Column(name = "status", columnDefinition = "int default 1 COMMENT '状态{0:停用,1:启用}'")
    private Integer status;

}
