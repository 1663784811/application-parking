package com.cyyaw.admin.entity.dto.user.login;

import com.cyyaw.admin.entity.utils.Reg;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;


@Data
public class RegisterEnterpriseRequest extends VerifyRequest implements Serializable {

    @Schema( description = "企业名", example = "name")
    @NotBlank(message = "企业名称不能为空")
    @Pattern(regexp = "^.{3,}$", message = "企业名称,【3位以上长度】")
    private String name;

    @Schema( description = "logo", example = "logo")
    @NotBlank(message = "请上传企业Logo")
    private String logo;

    @Schema( description = "联系人", example = "联系人")
    @NotBlank(message = "联系人不能为空")
    @Pattern(regexp = "^.{2,}$", message = "联系人,【2位以上长度】")
    private String person;

    @Schema( description = "手机号", example = "12345678901")
    @NotBlank(message = "联系人手机号不能为空")
    @Pattern(regexp = Reg.PHONE_REGEX, message = "手机号格式不正确,【11位手机号】")
    private String phone;

    @Schema(description = "密码", example = "password")
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = Reg.PASSWORD_REGEX, message = "密码格式不正确,【6位数字或字母或特殊字符】")
    private String password;

    @Schema( description = "验证码", example = "123456")
    @NotBlank(message = "验证码不能为空")
    private String code;

}
