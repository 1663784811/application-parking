package com.cyyaw.admin.application.user.controller.user;

import com.cyyaw.admin.application.user.service.AuUserAddressService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.em.SystemRoleEnum;
import com.cyyaw.admin.entity.module.user.AuUserAddress;
import com.cyyaw.admin.entity.utils.LoginInfo;
import com.cyyaw.admin.entity.utils.LoginInfoContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "APP-用户-地址")
@RestController
@RequestMapping("/app/user/address")
public class AppUserAddressController {


    @Autowired
    private AuUserAddressService auUserAddressService;


    @Operation(summary = "保存用户地址", description = "保存用户地址")
    @PostMapping("/saveAddress")
    public BaseResult<AuUserAddress> saveAddress(@RequestBody AuUserAddress auUserAddress) {
        LoginInfo loginInfo = LoginInfoContext.getLoginInfo();
        String systemRole = loginInfo.getSystemRole();
        if (SystemRoleEnum.User.getRole().equals(systemRole)) {
            auUserAddress.setEnId(loginInfo.getEnId());
            auUserAddress.setUserId(loginInfo.getId());
            AuUserAddress address = auUserAddressService.saveAddress(auUserAddress);
            return BaseResult.ok(address);
        } else {
            return BaseResult.fail("角色不对");
        }
    }

}
