package com.cyyaw.admin.entity.em;

import lombok.Getter;

/**
 * mqtt 业务
 */
@Getter
public enum MqttBusiness {


    ParkingGate(MqttBusiness.PARKING, "gate", "停车场门口设备") //
    , FoodStore(MqttBusiness.FOOD, "store", "点餐系统门店设备") //
    , FoodH5(MqttBusiness.FOOD, "h5", "点餐手机客户端") //
    ;

    public final static String PARKING = "parking";
    public final static String FOOD = "food";

    // 业务
    private String business;
    // 角色
    private String role;
    //
    private String note;

    MqttBusiness(String business, String role, String note) {
        this.business = business;
        this.role = role;
        this.note = note;
    }

    public String getPath() {
        return "/server/" + business + "/" + role;
    }

}
