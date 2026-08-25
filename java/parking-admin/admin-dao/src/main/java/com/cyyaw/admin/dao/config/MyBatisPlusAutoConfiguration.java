package com.cyyaw.admin.dao.config;

import com.cyyaw.admin.dao.MyBatisPlusMetaObjectHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * admin-dao 自动配置。
 * <p>
 * admin-dao 仅被各应用 @MapperScan 扫描 Mapper 接口，普通 @Component 不被组件扫描，
 * 因此 MetaObjectHandler 等框架钩子 Bean 必须通过 Spring Boot 自动配置注册才能生效。
 * 由 META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports 触发加载。
 */
@AutoConfiguration
public class MyBatisPlusAutoConfiguration {

    @Bean
    public MyBatisPlusMetaObjectHandler myBatisPlusMetaObjectHandler() {
        return new MyBatisPlusMetaObjectHandler();
    }
}
