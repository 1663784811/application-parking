package com.cyyaw.admin.common;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 */
public class WhyStringUtil {

    private static final String numberStr = "0123456789";

    private static final String string = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" + numberStr;

    /**
     * 获取UUID
     */
    public static String getUUID() {
        String str = UUID.randomUUID().toString();
        return str.replace("-", "");
    }

    /**
     * 生成随机数
     */
    public static String getRandomString(int length, String str) {
        StringBuffer sb = new StringBuffer();
        int len = str.length();
        for (int i = 0; i < length; i++) {
            sb.append(str.charAt(getRandom(len - 1)));
        }
        return sb.toString();
    }

    /**
     * 生成随机数
     */
    public static String getRandomString(int length) {
        return getRandomString(length, string);
    }

    public static String verifyCode(int length) {
        return getRandomString(length, numberStr);
    }


    private static int getRandom(int count) {
        return (int) Math.round(Math.random() * (count));
    }

    /**
     * 生成订单号  2019 07 07 16 16 10
     */
    public static String createOrderNum() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String format = sdf.format(new Date());
        for (int i = 0; i < 6; i++) {
            format += getRandom(10);
        }
        return format;
    }

    /**
     * 生成字符串
     */
    public static String createStr(int l, String str, String str2) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < l; i++) {
            if (i != 0 && null != str2) {
                sb.append(str2);
            }
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * 生成字符串
     *
     * @param o
     * @param l
     * @param str
     * @return
     */
    public static String createStrLength(String o, int l, String str) {
        if (null == o) {
            o = "";
        }
        if (null == str) {
            str = "0";
        }
        for (int i = o.length(); i < l; i++) {
            o = str + o;
        }
        return o;
    }

    /**
     * 提取网址
     *
     * @param address
     * @return
     */
    public static String getWebAdd(String address) {
        String regex = "(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";
        StringBuffer sb = new StringBuffer();
        Pattern p = Pattern.compile(regex);
        Matcher matcher = p.matcher(address);
        if (matcher.find()) {
            sb.append(matcher.group());
        }
        return sb.toString();
    }

    public static String getWebNotParam(String address) {
        String ad = getWebAdd(address);
        int i = ad.indexOf("?");
        if (i != -1) {
            return ad.substring(0, i);
        } else {
            return ad;
        }
    }


}
