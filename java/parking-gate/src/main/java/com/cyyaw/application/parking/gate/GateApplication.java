package com.cyyaw.application.parking.gate;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;

@Slf4j
@EnableAsync
@SpringBootApplication
public class GateApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(GateApplication.class, args);
        Environment env = run.getEnvironment();
        String port = env.getProperty("server.port");
        log.info("接口文档 http://127.0.0.1:" + port + "/doc.html");
    }

}
