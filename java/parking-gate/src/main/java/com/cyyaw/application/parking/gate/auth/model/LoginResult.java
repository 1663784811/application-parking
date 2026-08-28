package com.cyyaw.application.parking.gate.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResult {
    private String token;
    private UserInfo userInfo;
}
