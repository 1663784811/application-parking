package com.cyyaw.admin.entity.dto.user.login;

import com.cyyaw.admin.entity.utils.Reg;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

/**
 * 验证码
 */
@Data
public class VerifyRequest implements Serializable {

    @Schema(description = "验证码指纹", example = "fingerprint")
    @NotBlank(message = "使用的设备有问题")
    @Pattern(regexp = Reg.Fingerprint_REGEX, message = "使用的设备有问题")
    private String fingerprint;

}
