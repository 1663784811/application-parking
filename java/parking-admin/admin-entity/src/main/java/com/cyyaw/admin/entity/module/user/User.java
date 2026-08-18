package com.cyyaw.admin.entity.module.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体（对应 user 表，下划线字段 -> 驼峰属性）。
 * <p>
 * 实体类统一存放于 parking-entity 模块，按服务分包：${groupId}.entity.${模块名}。
 */
@Data
@TableName("user")
public class User {

    /** 自增主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 业务主键 */
    private String tid;

    /** 账号 */
    private String account;

    /** BCrypt 密码哈希 */
    private String password;

    /** 姓名 */
    private String name;

    /** 手机号 */
    private String phone;

    /** 1正常 0禁用 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
