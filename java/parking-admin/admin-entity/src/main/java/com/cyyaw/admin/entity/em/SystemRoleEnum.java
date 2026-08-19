package com.cyyaw.admin.entity.em;

import lombok.Getter;

/**
 * 系统角色
 */
@Getter
public enum SystemRoleEnum {

    Root("root"), Admin("admin"), Store("store"), User("user");

    /**
     * 角色
     */
    private String role;

    SystemRoleEnum(String role) {
        this.role = role;
    }

}
