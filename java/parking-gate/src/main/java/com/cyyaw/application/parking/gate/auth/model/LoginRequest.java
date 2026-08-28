package com.cyyaw.application.parking.gate.auth.model;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
