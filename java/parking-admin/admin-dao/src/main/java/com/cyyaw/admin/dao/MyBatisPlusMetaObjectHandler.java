package com.cyyaw.admin.dao;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.cyyaw.admin.entity.utils.LoginInfo;
import com.cyyaw.admin.entity.utils.LoginInfoContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 审计字段自动填充。
 * <p>
 * 替代 JPA @PrePersist/@PreUpdate：项目持久层为 MyBatis-Plus，不触发 JPA 生命周期回调，
 * 故由 BaseEntity 上的 @TableField(fill=...) 驱动本 handler 填充：
 * insert 填充 enId（取自登录上下文 LoginInfoContext）、createTime、updateTime；update 填充 updateTime。
 * <p>
 * admin-dao 不被各应用组件扫描，本 Bean 由 MyBatisPlusAutoConfiguration（Spring Boot 自动配置）注册，
 * 见 META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports。
 */
@Slf4j
public class MyBatisPlusMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LoginInfo loginInfo = LoginInfoContext.getLoginInfo();
        if (null != loginInfo) {
            // 填充所属企业ID
            strictInsertFill(metaObject, "enId", Long.class, loginInfo.getEnId());
        }
        // 填充生成日期
        strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        // 填充更新日期
        strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 填充更新日期
        strictInsertFill(metaObject, "update_time", LocalDateTime.class, LocalDateTime.now());
    }
}
