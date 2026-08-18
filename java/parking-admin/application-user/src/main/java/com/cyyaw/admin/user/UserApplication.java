package com.cyyaw.admin.user;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 用户应用启动类。
 * <p>
 * 端口 18080，与 web/parking_h5 的 vite proxy（/api -> http://localhost:18080）对齐。
 */
@SpringBootApplication
@MapperScan("com.cyyaw.admin.user.mapper")
@EnableScheduling
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }

}
