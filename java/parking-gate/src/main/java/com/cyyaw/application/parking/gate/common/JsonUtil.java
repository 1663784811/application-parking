package com.cyyaw.application.parking.gate.common;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class JsonUtil {

    // ===== JSON 序列化（Jackson 3） =====

    /**
     * Jackson 3 序列化器（SB4 默认即 Jackson 3）；手动 new 而非注入，避免误注入
     * jackson2 ObjectMapper 导致启动崩（见 memory spring-boot-4-jackson-3）
     */
    private static final ObjectMapper mapper = new ObjectMapper();


    public static String toJson(Object object) {
        return mapper.writeValueAsString(object);
    }

    public static JsonNode readTree(String body) {
        return mapper.readTree(body);
    }
}
