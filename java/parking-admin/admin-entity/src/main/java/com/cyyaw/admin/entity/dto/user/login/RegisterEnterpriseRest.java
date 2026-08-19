package com.cyyaw.admin.entity.dto.user.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;


@Data
public class RegisterEnterpriseRest implements Serializable {

    @Schema( description = "企业ID", example = "id")
    private Long id;

    @Schema( description = "企业名", example = "name")
    private String name;

    @Schema( description = "logo", example = "logo")
    private String logo;

    @Schema( description = "企业编号", example = "code")
    private String eCode;

    @Schema( description = "联系人", example = "联系人")
    private String person;

    @Schema( description = "手机号", example = "12345678901")
    private String phone;

    @Schema( description = "管理员账号", example = "username")
    private String username;

}
