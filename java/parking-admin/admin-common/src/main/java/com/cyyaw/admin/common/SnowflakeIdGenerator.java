package com.cyyaw.admin.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * 雪花算法分布式ID生成器
 * 保证分布式环境下ID全局唯一、趋势递增
 * <p>
 * 共64位： ( 0 保证是正数 )  + ( 43 位时间 可以有两百多年 ) + ( 8位机器 有255台 ) + ( 12位序列数  )
 */
public class SnowflakeIdGenerator {
    // ============================== 常量配置 ==============================
    // 开始时间
    private static final long START_TIMESTAMP;

    static {
        try {
            START_TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2026-01-01 00:00:00").getTime();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 机器ID所占位数
     */
    private static final long WORKER_ID_BITS = 8L;
    /**
     * 序列号所占位数
     */
    private static final long SEQUENCE_BITS = 12L;
    // ============================================================================================
    /**
     * 机器ID最大值 (0~255)   负数保存的是补码
     * -1 的二进制   1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111
     * 先左移 8位    1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 0000 0000
     * 再取反        0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 1111 1111
     */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    /**
     * 时间戳左移位数 (12+8=20位)
     */
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /**
     * 序列号掩码 (0~4095)
     */
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    // ============================== 实例变量 ==============================
    /**
     * 机器ID
     */
    private final long workerId;
    /**
     * 序列号
     */
    private long sequence = 0L;
    /**
     * 上一次生成ID的时间戳
     */
    private long lastTimestamp = -1L;

    // ============================== 构造方法 ==============================

    /**
     * 构造函数
     *
     * @param workerId 机器ID (0~31)
     */
    public SnowflakeIdGenerator(long workerId) {
        // 校验参数合法性
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("机器ID必须在0~255之间");
        }
        this.workerId = workerId;
    }

    // ============================== 核心方法 ==============================

    /**
     * 生成下一个唯一ID (线程安全)
     *
     * @return 唯一ID
     */
    public synchronized long nextId() {
        long currentTimestamp = System.currentTimeMillis();
        // 禁止时钟回拨（服务器时间不准时抛出异常）
        if (currentTimestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨，拒绝生成ID");
        }
        // 同一毫秒内，序列号自增
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            // 序列号耗尽，等待下一毫秒
            if (sequence == 0) {
                currentTimestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒，序列号重置为0
            sequence = 0L;
        }
        lastTimestamp = currentTimestamp;
        // 拼接ID：时间戳 | 数据中心ID | 机器ID | 序列号
        return ((currentTimestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT) | (workerId << SEQUENCE_BITS) | sequence;
    }

    /**
     * 等待下一毫秒
     *
     * @param lastTimestamp 上一次时间戳
     * @return 新的时间戳
     */
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}