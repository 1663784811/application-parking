package com.cyyaw.admin.entity.dto.user.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author why
 */
@Data
@ToString
@NoArgsConstructor
public class LoginRest implements Serializable {

    @Schema(description = "jwt令牌")
    private String jwtToken;

    @Schema(description = "长jwt令牌")
    private String refreshToken;


}
