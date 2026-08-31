package com.cyyaw.netty.mqtt;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class NettyMqttApplication {


    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(NettyMqttApplication.class, args);
    }

}
