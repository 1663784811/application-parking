# 智慧停车场管理系统

面向停车场运营企业的 SaaS 化综合管理平台，覆盖车牌识别、道闸控制、停车计费、线上缴费、会员月卡、数据报表的完整业务闭环。

- **后台管理端（parking_admin）**：停车场档案、通行管理、收费管理、会员管理、设备管理、数据报表、系统设置
- **门口管理端（gate_admin）**：出入口值守人员操作终端，实时监控、车辆通行记录、道闸操作
- **用户端 H5（parking_h5）**：附近停车场查询、扫码缴费、订单管理、优惠券、卡包管理、车辆管理
- **车牌识别摄像头（parkingcamera）**：Android 端摄像头 App，本地识别车牌并上报云端

## 技术栈

| 层次 | 组件 |
| --- | --- |
| 后端 | Spring Boot 4.0.2 · Java 21 · MyBatis-Plus 3.5.15 · Spring Security · JWT (jjwt) · knife4j 4.5.0 · MySQL · Redis · MQTT |
| 微服务（云平台） | Spring Cloud Gateway (WebFlux) · Nacos · OpenFeign · LoadBalancer |
| 后台管理前端 | Vue 3 (script setup) · Vite · View UI Plus · Pinia · vue-router · Less · ECharts |
| 门口管理前端 | Vue 3 · Vite · View UI Plus |
| 用户端 H5 | Vue 3 · Vite · Vant 4 |
| 摄像头 App | Android (minSdk 30 / targetSdk 36) · onnxruntime · HyperLPR3 本地车牌识别 |

## 目录结构

```text
java  # Java 后端
    parking-admin                # 核心服务（多模块 Maven 工程）
        admin-entity             # 实体
        admin-dao                # 数据访问（@MapperScan）
        admin-common             # 通用工具
        admin-config             # 配置
        admin-interface          # 内部接口定义
        application-common       # 公共业务模块
        application-common-mqtt-client  # MQTT 客户端封装
        application-user         # 用户/权限/企业
        application-parking      # 停车场/车位/通道/开闸/停车记录
        application-order        # 订单/费率/支付
        application-member       # 会员/月卡
        application-iot          # 物联网设备
        cloud-parent             # 云平台微服务父工程
            cloud-application-gateway   # 云网关（SC Gateway + Nacos）
        cloud-interface-impl / cloud-application-user  # 云端服务
        netty-application-mqtt   # MQTT Broker（Netty 实现，tcp 1883 / ws 8083）
        single-all               # 单机版启动聚合模块（port 10000, context-path /api）
        single-interface-impl    # 单机版内部接口实现
    parking-gate                 # 保安亭（门口岗亭）服务
        # port 18080，内嵌 MQTT（tcp 11883 / ws 38083）
        # /api/plate/recognize 接收摄像头识别，/api/vehicle/records 通行记录

web   # 前端
    gate_admin      # 停车场门口管理端
    parking_admin   # 停车场后台管理
    parking_h5      # 停车场用户端 H5

android
    parkingcamera   # 车牌识别摄像头 App

业务流程            # 业务流程图文档
```

## 接口路径规范

```text
后台接口  /api/admin/模块/**      例：/api/admin/parking/parking
用户接口  /api/app/项目/**        例：/api/app/parking/exit/channelVehicle
内部接口  /api/internal/**        无库兄弟进程（broker/gate）间调用，permit-all
```

响应统一为 `BaseResult`：`{ result, data, msg, code }`，`code=2000` 表示成功。

## 数据库

- MySQL 库名 `t_parking`，多租户隔离字段 `en_id`（企业ID）
- 表前缀按业务域划分：`au_`（用户权限）、`pk_`（停车场）、`or_`（订单）、`me_`（会员）、`iot_`（物联网设备）、`conf_`（系统配置）
- 所有业务表继承公共基类字段：`id` / `en_id` / `create_time` / `update_time` / `note` / `del_time`（逻辑删除）
- 表结构详见根目录《数据库_停车场.md》《数据库_用户.md》《数据库_订单.md》《01数据库_通用规范.md》
- 建表 SQL 在 `java/parking-admin/single-all/src/main/resources/sql/`

## 快速启动（单机版）

环境准备：JDK 21、MySQL、Redis、Node.js。

```bash
# 1. 初始化数据库：执行 single-all/src/main/resources/sql/ 下的建表 SQL

# 2. 后端（spring-boot-maven-plugin 被 skip，用 classpath 方式启动）
cd java/parking-admin
mvn install -Dspring-boot.repackage.skip=true
mvn -pl single-all dependency:build-classpath -Dmdep.outputFile=cp.txt
java -cp "single-all/target/classes;$(cat single-all/cp.txt)" com.cyyaw.sigle.SigleAllApplication
# 服务端口 10000，接口前缀 /api

# 3. MQTT Broker（本地联调需要）
java -cp ... com.cyyaw.netty.mqtt.NettyMqttApplication   # tcp 1883 / ws 8083 (/mqtt)

# 4. 保安亭服务（摄像头联调需要）
cd java/parking-gate
mvn spring-boot:run    # port 18080

# 5. 前端
cd web/parking_admin && npm install && npm run dev   # 后台管理
cd web/parking_h5 && npm install && npm run dev      # 用户端 H5
cd web/gate_admin && npm install && npm run dev      # 门口管理端
```

数据库连接、账号密码见 `java/parking-admin/doc/账号密码.md`。

## 总体流程

``` mermaid

flowchart TD
    A[车辆入场] --> B{识别车牌}
    B -->|识别成功| C[抬杆放行，记录入场时间、车牌、车位]
    B -->|识别失败| D[人工确认/取卡，登记车辆信息]
    D --> C
    C --> E[车辆进入场内停放]
    
    E --> F[车辆出场]
    F --> G{识别车牌}
    G -->|识别成功| H[调取入场记录，计算停车费用]
    G -->|识别失败| I[人工输入车牌/刷卡，调取入场记录]
    I --> H
    
    H --> J{缴费方式}
    J -->|线上缴费：小程序/公众号| K[线上完成支付]
    J -->|线下缴费：出口岗亭/自助机| L[现场扫码/现金缴费]
    
    K & L --> M{缴费校验}
    M -->|已缴费| N[抬杆放行，记录出场时间，释放车位]
    M -->|未缴费/欠费| O[提示补缴费用，禁止抬杆]
    O --> J
    
    N --> P[数据归档：停车记录、收费记录存入数据库]
    
    %% 特殊流程：月卡/会员车
    Q[月卡/会员车辆入场] --> R[校验月卡有效性]
    R -->|有效| S[直接抬杆放行，记录入场]
    R -->|过期无效| T[转为临时车计费流程]
    S --> E
    
    U[后台管理端] --> V[车位监控、报表统计、车辆管理、月卡开通、收费配置、异常记录查询]

```

## 车牌识别流程

``` mermaid
flowchart TD
    A[摄像头识别车牌] --> |调用保安亭服务/plate/recognize | B[车牌识别回调]
    B -->|上传车辆图片和车牌图片| C[后台服务器]
    B -->|发送识别消息| D[后台MQTT服务]
```

## 进场、出场 开闸流程

``` mermaid
flowchart TD
    开始 --> A
    A[后台MQTT服务器接收到车牌识别消息] --> |调用后台服务接口| B{是否进/出场摄像头}
    
    B --> | 进场摄像头、发送MQTT消息 | C[ 保安亭服务 ]
    C --> D[道闸开闸]
    D --> 结束
    
    B --> | 出场摄像头 | E{是否已经的缴费}
    E --> | 已缴费、发送MQTT消息 | C
    E --> | 未缴费 | F[等待缴费]
    F --> G[结束]
    
```

出场策略为「待缴费不开闸」：出口摄像头识别车牌后登记该车正在通道等待缴费，
车主通过 H5 扫通道二维码（`/app/1/scanExit?parkingId=&channelId=`）带出车牌并完成线上缴费，
支付回调触发 `completeExit` 开闸放行；详见《业务流程_h5.md》。

## MQTT 通信

云平台与停车场设备通过 MQTT 通信（协议详见《MQTT停车场与云平台通信.md》）：

| 方向 | 主题 | 用途 |
| --- | --- | --- |
| 设备 → 云 | `/server/parking/${设备编码}/thing/event/{事件}/post` | 上报设备上/下线、车牌识别 |
| 云 → 设备 | `/device/parking/${设备编码}/thing/service/{事件}/set` | 下发开闸/关闸 |

## 文档索引

| 文档 | 说明 |
| --- | --- |
| [需求规格说明书.md](需求规格说明书.md) | 功能/非功能需求、角色权限、技术栈总览 |
| [01数据库_通用规范.md](01数据库_通用规范.md) | 全项目数据表通用设计规范 |
| [数据库_停车场.md](数据库_停车场.md) / [数据库_用户.md](数据库_用户.md) / [数据库_订单.md](数据库_订单.md) | 各模块表结构、枚举、索引 |
| [MQTT停车场与云平台通信.md](MQTT停车场与云平台通信.md) | 设备通信协议与报文格式 |
| [业务流程_h5.md](业务流程_h5.md) | H5 扫码缴费出场流程与接口定义 |
| [后台页面业务规范.md](后台页面业务规范.md) | 后台管理端页面开发约束 |
| [微信H5授权流程.md](微信H5授权流程.md) | 微信公众号/支付宝授权登录 |
| [业务流程/01系统初始化.md](业务流程/01系统初始化.md) | 企业注册初始化流程 |
| [java/parking-admin/doc/](java/parking-admin/doc/) | 部署文档、单机/微服务设计规范、账号密码 |
