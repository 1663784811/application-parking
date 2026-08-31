package com.cyyaw.sigle;


import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;


@Slf4j
@SpringBootApplication(scanBasePackages = {"com.cyyaw.sigle", "com.cyyaw.admin.application.user", "com.cyyaw.admin.application.order", "com.cyyaw.admin.application.parking", "com.cyyaw.admin.application.member", "com.cyyaw.admin.application.device", "com.cyyaw.admin.entity.redis"})
@MapperScan(basePackages = {"com.cyyaw.admin.dao.**"})
public class SigleAllApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(SigleAllApplication.class, args);
        Environment env = run.getEnvironment();
        String port = env.getProperty("server.port");
        log.info("接口文档 http://127.0.0.1:" + port + "/api/doc.html");
        log.info("启动应用完成后");
        log.info("1.注册新企业: 打开前端页面, http://127.0.0.1");
    }
}
