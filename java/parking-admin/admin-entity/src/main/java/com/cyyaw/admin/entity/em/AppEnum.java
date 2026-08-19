package com.cyyaw.admin.entity.em;

import lombok.Getter;

@Getter
public enum AppEnum {

    System("system", "企业系统", "整合核心信息，助决策者掌动态、定规划。标准化流程，促协作提效。", "http://localhost:3100/enterprise/src/assets/logo_64.png"),
    Shopping("shopping", "购物商城", "精选商品齐聚，便捷购好物，售后有保障，舒心购物首选。", "http://localhost:3100/enterprise/src/assets/logo_64.png"),
    Travel("travel", "旅行和预订APP", "预订机票酒店，规划行程，旅行便捷无忧。", "http://localhost:3100/enterprise/src/assets/logo_64.png"),
    Furniture("furniture", "家具概念应用", "家具展示和销售平台，3D展示和AR试用功能", "http://localhost:3100/enterprise/src/assets/logo_64.png"),
    EnglishWord("englishWord", "英语词典", "智能记忆发音评测例句拼写错题本", "http://localhost:3100/enterprise/src/assets/logo_64.png"),
    Iot("iot", "物联网", "物物互联的智能网络，实现设备互联、数据共享与远程管控、赋能远程控制与数据", "http://localhost:3100/enterprise/src/assets/logo_64.png"),
    Food("food", "点餐系统", "支持用户线上下单、商家接单配餐，集成支付与订单管理，高效便捷优化餐饮全流程服务", "http://localhost:3100/enterprise/src/assets/food.png")
    ;
    private String appType;
    private String title;
    private String introduction;
    private String logo;


    AppEnum(String appType, String title, String introduction, String logo) {
        this.appType = appType;
        this.title = title;
        this.introduction = introduction;
        this.logo = logo;
    }
}
