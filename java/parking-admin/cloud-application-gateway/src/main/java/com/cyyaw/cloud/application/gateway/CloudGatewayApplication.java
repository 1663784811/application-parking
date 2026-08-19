package com.cyyaw.cloud.application.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
@EnableDiscoveryClient
@SpringBootApplication
@LoadBalancerClients({@LoadBalancerClient("spring-gateway")})
@EnableFeignClients
public class CloudGatewayApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(CloudGatewayApplication.class, args);
        log.info("===============   启动成功  ====================");
        log.info("http://127.0.0.1:9000");
    }

}
