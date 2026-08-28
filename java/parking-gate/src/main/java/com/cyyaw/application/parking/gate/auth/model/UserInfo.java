package com.cyyaw.application.parking.gate.auth.model;

import lombok.Data;

/**
 * 返回前端的用户信息（不含密码）。
 */
@Data
public class UserInfo {
    private Integer id;
    private String name;
    private String role;
    private String phone;
    private String parkingName;
    private Integer parkingId;
}
