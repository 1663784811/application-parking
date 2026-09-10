package com.cyyaw.admin.entity.em;

import lombok.Getter;

/**
 * 通道方向/类型：入口、出口、出入口。
 * <p>实体字段（{@code PkChannel.type}、{@code PkParkingDevice.channelType}）仍为 String，
 * 取本枚举的 {@link #getType()} 值，与 DB/JSON/前端保持一致（用法同 {@link IotDeviceTypeEnum}）。
 */
@Getter
public enum ChannelTypeEnum {
    IN("in", "入口"),
    OUT("out", "出口"),
    INOUT("inout", "出入口");

    private String type;
    private String note;

    ChannelTypeEnum(String type, String note) {
        this.type = type;
        this.note = note;
    }

    /**
     * 字符串 → 枚举；未匹配或空返回 null（用于校验/switch）。
     */
    public static ChannelTypeEnum fromType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        for (ChannelTypeEnum e : values()) {
            if (e.type.equals(type)) {
                return e;
            }
        }
        return null;
    }
}
