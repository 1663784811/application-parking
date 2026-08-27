package com.cyyaw.netty.mqtt;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(
        basePackages = {"com.cyyaw.netty.mqtt.**"}
)
public class NettyMqttApplication {


    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(NettyMqttApplication.class, args);
    }

}
