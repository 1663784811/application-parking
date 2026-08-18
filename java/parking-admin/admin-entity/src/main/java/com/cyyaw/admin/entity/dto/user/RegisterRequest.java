package com.cyyaw.admin.entity.dto.user;

import lombok.Data;

@Data
public class RegisterRequest {

    private String username;

    private String password;

    private String name;

    private String phone;

}
