package com.cyyaw.admin.entity.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void setString(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void setString(String key, String value, long expire) {
        redisTemplate.opsForValue().set(key, value, expire, TimeUnit.MILLISECONDS);
    }


    public String getString(String key) {
        Object o = redisTemplate.opsForValue().get(key);
        if (o == null) {
            return null;
        } else {
            return o.toString();
        }
    }

    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }
}
