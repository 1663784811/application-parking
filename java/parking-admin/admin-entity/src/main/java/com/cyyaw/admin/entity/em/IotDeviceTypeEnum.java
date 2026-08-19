package com.cyyaw.admin.entity.em;

import lombok.Getter;

/**
 * 设备类型
 */
@Getter
public enum IotDeviceTypeEnum {
   LIGHT("light", "灯")
    ,SWITCH("switch", "开关")
    ,AIRCONDITIONER("airConditioner", "空调")
    ;

    private String type;
    private String note;

    private IotDeviceTypeEnum(String type, String note) {
        this.type = type;
        this.note = note;
    }

}
