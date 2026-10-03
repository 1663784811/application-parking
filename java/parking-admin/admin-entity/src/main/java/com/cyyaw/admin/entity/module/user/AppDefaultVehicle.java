package com.cyyaw.admin.entity.module.user;

import com.cyyaw.admin.entity.utils.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 我的车辆（H5「我的车辆」）。
 * <p>
 * 与 {@code me_member} 分开：me_member 是付费会员卡（card_type / expire_time），
 * 这里只是车主把车牌登记到自己的账号下。
 * <p>
 * 归属走 user_id 优先、phone 兜底 —— 车牌识别在出入口没有登录态，
 * 需要按 en_id 找到账号、再拿账号的手机号或用户 ID 去匹配这条记录。
 */
@Data
@Entity
@Schema(description = "我的车辆")
@Table(name = "app_default_vehicle")
@EqualsAndHashCode(callSuper = true)
public class AppDefaultVehicle extends BaseEntity {

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING)
    @Column(name = "app_id", columnDefinition = "bigint COMMENT '所属APPID'")
    private Long appId;

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING)
    @Column(name = "user_id", columnDefinition = "bigint COMMENT '用户ID（au_user.id，未登录时为 NULL）'")
    private Long userId;

    @Column(name = "phone", columnDefinition = "varchar(20) COMMENT '手机号（未登录时的归属维度）'")
    private String phone;

    @Column(name = "plate", columnDefinition = "varchar(20) COMMENT '车牌号'")
    private String plate;

    @Column(name = "vehicle_type", columnDefinition = "varchar(50) default '小型汽车' COMMENT '车辆类型'")
    private String vehicleType;

    @Column(name = "is_default", columnDefinition = "int default 0 COMMENT '是否默认车辆{0:否,1:是}'")
    private Integer isDefault;

}
