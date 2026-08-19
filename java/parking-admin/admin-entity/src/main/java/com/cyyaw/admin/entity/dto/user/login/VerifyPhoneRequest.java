package com.cyyaw.admin.entity.dto.user.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 验证码
 */
@Data
public class VerifyPhoneRequest extends VerifyRequest implements Serializable {

    @Schema( description = "手机号", example = "phone")
    private String phone;

}
