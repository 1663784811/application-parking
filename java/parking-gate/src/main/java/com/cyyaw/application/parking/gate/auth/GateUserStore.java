package com.cyyaw.application.parking.gate.auth;

import com.cyyaw.application.parking.gate.auth.model.GateUser;
import com.cyyaw.application.parking.gate.auth.model.UserInfo;
import com.cyyaw.application.parking.gate.config.GateProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 保安亭登录用户仓库。不连接数据库，用户名/密码来自 application.yml 的 gate.users。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GateUserStore {

    private final GateProperties props;
    private final Map<String, GateUser> users = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        List<GateUser> list = props.getUsers();
        if (list != null) {
            for (GateUser u : list) {
                if (u.getUsername() != null) {
                    users.put(u.getUsername(), u);
                }
            }
        }
        log.info("加载保安亭用户 {} 个", users.size());
    }

    /**
     * 按账号密码校验，成功返回用户，否则 null。
     */
    public GateUser findByCredentials(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        GateUser user = users.get(username);
        if (user != null && password.equals(user.getPassword())) {
            return user;
        }
        return null;
    }

    public GateUser findByUsername(String username) {
        return users.get(username);
    }

    public UserInfo toUserInfo(GateUser user) {
        UserInfo info = new UserInfo();
        info.setId(user.getId());
        info.setName(user.getName());
        info.setRole(user.getRole());
        info.setPhone(user.getPhone());
        info.setParkingName(user.getParkingName());
        info.setParkingId(user.getParkingId());
        return info;
    }
}
