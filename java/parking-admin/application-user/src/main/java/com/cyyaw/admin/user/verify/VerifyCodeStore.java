package com.cyyaw.admin.user.verify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证码内存存储：verifyKey -> Entry{text, fingerprint, expireAt}。
 * <p>
 * TTL 由 verify.expire-seconds 配置；懒过期 + 定时清扫；校验时一次性删除。
 */
@Slf4j
@Component
public class VerifyCodeStore {

    @Value("${verify.expire-seconds:300}")
    private long expireSeconds;

    private final ConcurrentHashMap<String, Entry> store = new ConcurrentHashMap<>();

    public static class Entry {

        private final String text;
        private final String fingerprint;
        private final long expireAt;

        Entry(String text, String fingerprint, long expireAt) {
            this.text = text;
            this.fingerprint = fingerprint;
            this.expireAt = expireAt;
        }

        public String getText() {
            return text;
        }

        public String getFingerprint() {
            return fingerprint;
        }
    }

    /** 存入验证码，返回生成的 verifyKey */
    public String put(String text, String fingerprint) {
        String verifyKey = UUID.randomUUID().toString().replace("-", "");
        long expireAt = System.currentTimeMillis() + expireSeconds * 1000L;
        store.put(verifyKey, new Entry(text, fingerprint, expireAt));
        return verifyKey;
    }

    /**
     * 校验：忽略大小写比对，无论成功失败都删除（一次性使用）。
     */
    public boolean validate(String verifyKey, String code) {
        if (verifyKey == null || code == null) {
            return false;
        }
        Entry entry = store.remove(verifyKey);
        if (entry == null || isExpired(entry)) {
            return false;
        }
        return code.equalsIgnoreCase(entry.text);
    }

    /** 读取（不删除），供 getVerifyImg 渲染使用；过期返回 null */
    public Entry get(String verifyKey) {
        if (verifyKey == null) {
            return null;
        }
        Entry entry = store.get(verifyKey);
        if (entry == null || isExpired(entry)) {
            return null;
        }
        return entry;
    }

    private boolean isExpired(Entry entry) {
        return System.currentTimeMillis() > entry.expireAt;
    }

    /** 每 5 分钟清扫过期项 */
    @Scheduled(fixedDelay = 300_000L)
    public void cleanup() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Entry>> it = store.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Entry> e = it.next();
            if (now > e.getValue().expireAt) {
                it.remove();
            }
        }
        log.debug("验证码清扫完成，剩余 {}", store.size());
    }

}
