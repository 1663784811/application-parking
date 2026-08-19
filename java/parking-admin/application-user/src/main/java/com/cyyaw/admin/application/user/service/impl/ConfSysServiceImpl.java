package com.cyyaw.admin.application.user.service.impl;

import com.cyyaw.admin.application.user.service.ConfSysService;
import com.cyyaw.admin.dao.service.BaseService;
import com.cyyaw.admin.dao.user.ConfSysDao;
import com.cyyaw.admin.entity.module.user.ConfSys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service
public class ConfSysServiceImpl extends BaseService<ConfSysDao, ConfSys> implements ConfSysService {

    private static final char[] CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();


    @Autowired
    private ConfSysDao confSysDao;



    public static String generateCode(long number) {
        // 计算第一位字母（A-Z 循环）
        char firstChar = (char) ('A' + (number / (36 * 36 * 36 * 36)) % 26);
        // 计算后 4 位（36 进制）
        long remaining = number % (36 * 36 * 36 * 36);
        StringBuilder code = new StringBuilder().append(firstChar);
        for (int i = 3; i >= 0; i--) {
            long divisor = (long) Math.pow(36, i);
            int digit = (int) (remaining / divisor);
            code.append(CHARS[digit]);
            remaining %= divisor;
        }
        return code.toString();
    }

    @Override
    public String createNewCode() {
        long time = new Date().getTime();
        return generateCode(time);
    }
}