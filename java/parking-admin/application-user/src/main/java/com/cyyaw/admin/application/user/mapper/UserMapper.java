package com.cyyaw.admin.application.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyyaw.admin.entity.module.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户 Mapper：继承 MyBatis-Plus {@link BaseMapper}（自带 insert/selectById 等），
 * 自定义查询用 {@code @Select} 注解，无 XML。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM `user` WHERE account = #{account} LIMIT 1")
    User findByAccount(@Param("account") String account);

    @Select("SELECT * FROM `user` WHERE tid = #{tid} LIMIT 1")
    User findByTid(@Param("tid") String tid);

}
