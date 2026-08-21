package com.cyyaw.admin.dao;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.cyyaw.admin.entity.utils.LoginInfo;
import com.cyyaw.admin.entity.utils.LoginInfoContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
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
