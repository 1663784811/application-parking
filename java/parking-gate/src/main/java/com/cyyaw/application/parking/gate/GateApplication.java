package com.cyyaw.application.parking.gate;


import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@Slf4j
@EnableAsync
@MapperScan("com.cyyaw.application.parking.gate.mapper")
public class GateApplication {

    public static void main(String[] args) {
        SpringApplication.run(GateApplication.class, args);
    }

}
