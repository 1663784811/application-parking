package com.cyyaw.admin.application.iot.controller.internal;

import com.cyyaw.admin.application.iot.service.IotDeviceService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.module.iot.IotDevice;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MQTT Broker 内部接口
 * <p>
 * broker（netty-application-mqtt）不连接数据库，连接校验时把 MQTT 连接的
 * username/password/clientId 转发到这里，由 admin 查 iot_device 表后返回
 * {allowConnect, role}，broker 据此决定是否放行。
 * <p>
 * 路径 /internal/** 在 SecurityConfig 中放行（仅供内部服务调用，
 * 生产环境建议绑定 loopback 或加共享密钥）。
 */
@RestController
@RequestMapping("/internal/mqtt")
@RequiredArgsConstructor
public class InternalMqttController {

    private final IotDeviceService iotDeviceService;

    @PostMapping("/validate")
    public BaseResult<ValidateResult> validate(@RequestBody ValidateRequest req) {
        ValidateResult result = new ValidateResult();
        result.setAllowConnect(false);
        if (req == null || !StringUtils.hasText(req.getClientId())) {
            return BaseResult.ok(result, "clientId 为空");
        }
        // clientId 即设备 code（设备编码）
        IotDevice device = iotDeviceService.findByCode(req.getClientId());
        if (device == null) {
            return BaseResult.ok(result, "设备不存在");
        }
        // status: 1=启用, 0=禁用
        if (device.getStatus() == null || device.getStatus() != 1) {
            return BaseResult.ok(result, "设备已禁用");
        }
        // 校验设备预共享凭据（明文比对；如改用 BCrypt 可换 passwordEncoder.matches）
        if (StringUtils.hasText(device.getUsername()) && device.getUsername().equals(req.getUsername()) && device.getPassword() != null && device.getPassword().equals(req.getPassword())) {
            result.setAllowConnect(true);
            result.setRole("device");
            return BaseResult.ok(result, "校验通过");
        }
        return BaseResult.ok(result, "用户名或密码错误");
    }

    @Data
    public static class ValidateRequest {
        private String username;
        private String password;
        private String clientId;
    }

    @Data
    public static class ValidateResult {
        private Boolean allowConnect;
        private String role;
    }

}
