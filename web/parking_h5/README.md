# parking_h5 — 停车 H5 客户端

面向车主的手机网页端（微信/支付宝内置浏览器扫码进入），覆盖 **扫码缴费出场 → 车辆/订单/卡包/优惠券管理** 场景。

## 技术栈

| 类别 | 选型 |
| --- | --- |
| 构建 | Vite 8（`@vitejs/plugin-vue` 6） |
| 框架 | Vue 3.5（`<script setup>` + `reactive`） |
| 路由 | vue-router 5，**hash 模式**（`createWebHashHistory`） |
| 状态 | Pinia 4 |
| UI | Vant 4（全量引入）+ Less（`lang="less"`） |
| 请求 | axios（双实例：业务 `axios` / 上传 `upLoadFile`） |
| 运行时 | Node 24 |

样式统一走 `src/styles/variables.css` 的 CSS 变量，品牌主色 `--brand-primary: #10b981`，页面不写裸色值。

## 快速开始

```bash
npm install
npm run dev        # http://localhost:3108
npm run build      # 产物 dist/
npm run preview
```

- 开发端口固定 `3108`，`host: 0.0.0.0`（方便手机真机连内网地址调试）。
- `@` 别名指向 `src`。

### 目录结构

```text
src/
├── api/
│   ├── app.js              # 全部后端接口定义（唯一接口层）
│   ├── axiosRequest.js     # 双 axios 实例 + 拦截器
│   └── browser.js          # 浏览器指纹（canvas/webgl 特征 → 8 位 hash）
├── components/
│   ├── PlateInput.vue      # 车牌输入框（省前缀 + 7 位，第 7 位为新能源位）
│   ├── PlateKeyboard.vue   # 自绘车牌键盘（省份 / 字母数字两面板）
│   └── TabBar.vue          # 底部 tab（按路由 meta.showTabBar 显隐）
├── stores/
│   ├── app.js              # appId / appInfo（fetchApp）
│   └── loginInfo.js        # 双 token / userInfo / 短 token 刷新
├── views/                  # 见「页面清单」
└── styles/variables.css    # 设计变量（品牌/辅助/中性/状态/安全区）
```

## 环境与接口代理

`.env`：

```ini
VITE_BASE_URL=          # 留空即走 vite 代理（推荐，无跨域）
VITE_APP_TYPE=parking   # 传给 /app/login/findApp 的 appType
```

`vite.config.js` 代理：

| 前缀 | 目标 |
| --- | --- |
| `/api/admin/workflow` | `http://localhost:10000` |
| `/api` | `http://localhost:10000` |
| `/file` | `http://localhost:80` |

`10000` 即后端单体 `single-all`（`server.servlet.context-path: /api`）。若联调微服务模式，把 `/api` 的 `target` 改到 `cloud-application-user`（`9001`）或网关 `9000` 即可。

> 两处待修正的旧注释：`.env` 里写的 `/api -> 18080` 与实际代理目标 `10000` 不符；`/file -> localhost:80` 没有对应服务，图片类接口目前用不到（`au_app.logo` 存的是完整 URL，不经代理）。

### 前置数据

欢迎页 `findApp` 依赖库里有一条 `au_app` 记录，且 `type` 等于 `VITE_APP_TYPE`（`parking`）；查不到会卡在「重新加载」。建表 DDL 在 `java/parking-admin/single-all/src/main/resources/sql/z_parking.sql`，但**没有自带种子数据**，需自己插一条：

```sql
INSERT INTO au_app (id, en_id, code, name, logo, type, del_time)
VALUES (1, 0, 'parking', '智慧停车', '', 'parking', 0);
```

## 整体流程

```text
/welcome                 查 app 信息（免登录）
   ↓ appStore.fetchApp(VITE_APP_TYPE)  →  /api/app/login/findApp?appType=parking
/app/:appId/login        手机验证码登录
   ↓ /api/common/verify/getVerifyPhoneCode  →  /api/app/login/phoneLogin
/app/:appId/main         App 布局（含 TabBar）
   ├── mainIndex         首页：附近停车场 + 扫码入口
   └── me                我的：资料 / 统计 / 菜单
   ↓
order | order/:orderId | vehicle | vehicle/add | vehicle/edit/:id
cardPackage | coupon | parkingExit | scanExit
```

- **appId 贯穿全路由**：所有页面路径都带 `:appId`，跳转时手动透传 `params: { appId }`。
- **hash 路由**：产物可直接丢到任意静态服务器，无需服务端 rewrite。
- **路由守卫**只做两件事：按 `meta.title` 设置 `document.title`；`appId` 缺失时重定向 `/welcome`。**没有登录态守卫**（下详）。
- TabBar 只显示在 `meta.showTabBar: true` 的 `mainIndex` / `me` 上。

## 请求约定

`src/api/axiosRequest.js`：

- 请求头自动带 `Authorization`（取自 `loginInfo` store）、`X-Requested-With`，POST 默认 JSON；`withCredentials: true`。
- 响应：`rest.data.code === 2000` 视为成功，直接返回 `rest.data`（即 `BaseResult`）。
- `code === 6010 / 6001`（token 失效）→ 调 `refreshShortToken()` 用 `refreshToken` 换新 token，**自动重放原请求**一次；失败才 reject。
- 上传走独立实例 `upLoadFile`，无 Authorization 外重试逻辑。

已对接后端（当前 H5 实际用到）：

| 接口 | 用途 | 白名单 |
| --- | --- | --- |
| `GET  /api/app/login/findApp?appType=` | 查应用信息（名称/logo/id） | ✅ 免登录 |
| `POST /api/common/verify/getVerifyPhoneCode` | 发短信验证码（手机号+指纹） | ✅ 免登录 |
| `POST /api/app/login/phoneLogin` | 手机验证码登录/注册 | ✅ 免登录 |
| `POST /api/common/token/refreshToken` | 刷新短 token | ✅（`/common/**`） |
| `GET  /api/app/parking/list?appId=&lng=&lat=` | 首页附近停车场（剩余车位/距离） | ❌ 需登录 |

> `/app/parking/list` 的 `lng`/`lat` 可选：两者都传后端才用 `GeographicUtil` 算距离并按距离升序，
> 缺省则按创建时间倒序、不返回 `distance`。定位拿不到（拒绝授权/超时/浏览器不支持）走的就是缺省路径。

免登录白名单在后端 `SecurityConfig.java`，还有 `/admin/login/**`、`/store/login/**`、`/internal/**`、`/app/product/**` 等。

## 页面清单与对接状态

**已接后端：** `welcome`、`login`（含指纹、60s 倒计时、记住手机号）、
`parkingExit`（扫码缴费出场：通道带出车牌、按车牌查费用、支付）、
`scanExit`（重新设计的出场缴费页：品牌头图 + 悬浮卡片，车牌核对 → 查费用 → 吸底支付）、
`mainIndex` 首页附近停车场列表。
`scanExit` 与 `parkingExit` 后端接口完全相同（都走 `src/api/parkingExit.js` 的三个接口），
区别只在交互与视觉；**通道二维码已改指 `scanExit`**（`parking_admin` 的 `channelList.vue`），
旧链接 `parkingExit` 保留可用，已印出去的二维码不受影响。

首页列表接口在 `src/api/appParking.js`（`app.js` 禁改，新接口不往里加）。
卡片带真实 `pk_parking.id`，**点击卡片直接进该停车场的出场缴费页**
（`/app/:appId/scanExit?parkingId=<真实ID>`），是除扫二维码之外的第二个入口。
卡片不显示价格：费率是首段/时段/封顶的阶梯组合，没有单一单价可展示，真实金额以出场查询为准。

**仍是本地 mock 数据**（`reactive` 里硬编码 + `TODO` 注释），UI 已完成、接口未接：

| 页面 | 文件 | 待接 |
| --- | --- | --- |
| 首页 | `views/main/MainIndex.vue` | 地图导航（需地图 SDK 凭证）、扫码 |
| 我的 | `views/me/Me.vue` | 用户信息 + 停车次数/时长/优惠券等统计 |
| 订单列表/详情 | `views/order/Order.vue`、`OrderDetail.vue` | 列表、详情、删除、支付 |
| 车辆列表/新增/编辑 | `views/vehicle/Vehicle.vue`、`VehicleAdd.vue`、`VehicleEdit.vue` | 增删改查（可用 `commonSave`） |
| 卡包 | `views/cardPackage/CardPackage.vue` | 卡列表（次卡/月卡/季卡/年卡） |
| 优惠券 | `views/coupon/Coupon.vue` | 列表、删除 |

> 停车出场的三个接口定义见仓库根目录 `业务流程_h5.md`；接口层在 `src/api/parkingExit.js`
> （`app.js` 是禁改文件，新接口不往里加）。**接口3 支付当前返回「支付通道未接入」**，
> 支付宝/微信统一下单尚未实现。
> 后端 C 端模块目前有 `/app/login`、`/app/user/address`、`/app/parking/exit`；
> 车辆/订单/卡包/优惠券仍没有 `/app/**` 接口。`src/api/app.js` 里的
> `commonQuery`/`commonSave`/`commonDel` 是通用 SQL 通道，可临时兜底。
> `Login.vue` 的 `MOCK_LOGIN` 现为 `false`（走真实登录）。

## 已知问题 / 待办

1. **首页扫码按钮不能扫码**：全项目**没有扫码能力**（无相机、无 `weixin-js-sdk`/`alipayjsapi`）。调起相机要公众号/应用凭证 + 后端签名接口，两者都不具备，所以按钮现在只弹提示让车主用相机扫通道二维码。真实的二维码场景是靠 URL 带参进入 `parkingExit`（`parkingId` 必带，`channelId` 可缺省）。接入扫码后把 `MainIndex.handleScan` 换成「解析扫码结果 URL → 跳 parkingExit」。**已修**：原先跳的是不存在的路由名 `scanExit`，会落到 `NotFound`。
2. **无登录守卫**：直接访问 `/app/xxx/main` 不会跳登录页；`Me.vue` 里 nickname 会显示"未登录"。建议在 `router.beforeEach` 里按 `isLogin` 判断。
3. **token 不持久化**：`loginInfo` store 纯内存，刷新页面即丢登录态，会回到欢迎页。
4. **支付未实现**：`parkingExit` 调接口3 会拿到「支付通道未接入」；`OrderDetail` 的支付仍是 `setTimeout` 假成功。微信/支付宝统一下单（含商户号、回调验签）全项目不存在。
5. **`userInfo` 字段未取**：登录成功后只存了 token，没有调 `/api/common/token/findUserInfo` 回填 `userInfo`。
6. **`uploadFile` 暂无调用方**（出场/车辆图片未接入），上传接口与 `/file` 代理待验证。
7. ~~**附近停车场是 mock 数据**~~ **已修**：改调 `GET /api/app/parking/list`，卡片带真实
   `pk_parking.id`，可点进出场缴费页。遗留两点：
   - **`pk_parking.long_lat` 是自由文本，无格式约定**。接口按 `"经度,纬度"` 解析（与
     `parkingList.vue` 的经/纬度两个输入框拼串的口径一致），解析失败或超出合法范围的当「无坐标」，
     不参与距离排序。**但这道范围校验挡不住占位值**：库里现有数据是 `"23,12"`，两个数各自都在
     合法范围内，会被当成真实坐标参与计算（实测广州到该点算出 9537.8km）。要让距离可用，
     得先在管理端把坐标填成真值。
   - **卡片图依赖 `pk_parking.image`**（本次新增字段），管理端用 URL 输入框录入、不是上传
     （后端上传接口 `/common/file/upload` 实测返回 4000 操作失败，且全项目无 Java 端上传代码，
     该接口不在本仓库源码内）。没配图时卡片回落品牌色渐变占位。

## 构建与部署

```bash
npm run build      # 输出 dist/（index.html + assets/，hash 路由无需服务端配置）
```

`dist/` 已在 `.gitignore`。部署要点：

- hash 路由，静态托管即可；刷新、深链都不会 404。
- 生产环境要么把 `VITE_BASE_URL` 设成后端域名（需后端开 CORS + 允许带凭证），要么在 Nginx 层把 `/api`、`/file` 反代到后端——**别留空**，否则请求会打到 H5 自身。
- 真机验证时用 `http://<内网IP>:3108`（`host: 0.0.0.0` 已开），微信扫码场景需 HTTPS 才能调 JSAPI，本地 `localhost` 调试建议用 Chrome。
