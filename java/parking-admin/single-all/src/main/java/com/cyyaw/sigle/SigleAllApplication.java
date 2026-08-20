package com.cyyaw.sigle;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = {
                "com.cyyaw.sigle",
                "com.cyyaw.admin.application.user"
        }
)
@MapperScan(basePackages = {"com.cyyaw.admin.dao.**"})
public class SigleAllApplication {


    public static void main(String[] args) {
        SpringApplication.run(SigleAllApplication.class, args);
    }


}
