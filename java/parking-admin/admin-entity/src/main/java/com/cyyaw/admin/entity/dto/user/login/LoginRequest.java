package com.cyyaw.admin.entity.dto.user.login;

import com.cyyaw.admin.entity.utils.Reg;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

@Data
public class LoginRequest extends VerifyRequest implements Serializable {

    @Schema( description = "企业ID", example = "enId")
    private Long enId;

    @Schema(description = "userName", example = "userName")
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = Reg.UserName_REGEX, message = "用户名不正确,【用户名3至18位】")
    private String username;

    @Schema(description = "password", example = "password")
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = Reg.PASSWORD_REGEX, message = "密码格式不正确,【6位数字或字母或特殊字符】")
    private String password;

    @Schema(description = "code", example = "code")
    @NotBlank(message = "验证码不能为空")
    private String code;


}
