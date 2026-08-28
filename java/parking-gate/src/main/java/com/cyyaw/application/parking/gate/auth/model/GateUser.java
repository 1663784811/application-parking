package com.cyyaw.application.parking.gate.auth.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户记录（含密码，不直接返回前端）。
 */
@Data
@NoArgsConstructor
public class GateUser {
    private Integer id;
    private String username;
    private String password;
    private String name;
    private String role;
    private String phone;
    private String parkingName;
    private Integer parkingId;
}
