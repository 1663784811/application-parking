package com.cyyaw.admin.entity.dto.user.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class TokenRequest implements Serializable {

    @Schema(description = "token", example = "token")
    private String token;

    @Schema(description = "refreshToken", example = "refreshToken")
    private String refreshToken;

}
