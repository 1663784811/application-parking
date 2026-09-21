# App用户登录接口文档

## 通用响应格式
所有接口返回的通用响应格式如下：
```json
{
  "code": 2000,           // 状态码
  "msg": "操作成功",      // 提示信息
  "data": {},           // 返回数据, 数组、对象、字符串等等
  "result": null        // 分页信息（如果适用）
}
```

## 用户登录模块

### 1. 用户登录-用户名密码登录

**接口地址**: `POST /app/login/login`

**接口描述**: 用户名密码登录

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| fingerprint | String | 是 | 验证码指纹 | fingerprint |
| code | String | 是 | 验证码 | code |
| username | String | 是 | 用户名 | userName |
| password | String | 是 | 密码 | password |
| appId | Long | 是 | 应用ID | appId |
| storeId | Long | 否 | 门店ID | storeId |

**请求示例**:
```json
{
  "fingerprint": "example_fingerprint",
  "code": "123456",
  "username": "testuser",
  "password": "123456",
  "appId": 1,
  "storeId": 1001
}
```

**响应示例**:
```json
{
  "code": 2000,
  "msg": "登录成功",
  "data": {
    "jwtToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "refresh_token_example"
  }
}
```

### 2. 用户登录-手机验证码登录(或注册)

**接口地址**: `POST /app/login/phoneLogin`

**接口描述**: 手机验证码登录(或注册)

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| fingerprint | String | 是 | 验证码指纹 | fingerprint |
| phone | String | 是 | 手机号 | phone |
| code | String | 是 | 验证码 | code |
| appId | Long | 是 | 应用ID | appId |
| storeId | Long | 否 | 门店ID | storeId |

**请求示例**:
```json
{
  "fingerprint": "example_fingerprint",
  "phone": "13800138000",
  "code": "123456",
  "appId": 1,
  "storeId": 1001
}
```

**响应示例**:
```json
{
  "code": 2000,
  "msg": "登录成功",
  "data": {
    "jwtToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "refresh_token_example"
  }
}
```

### 3. 用户名密码注册

**接口地址**: `POST /app/login/register`

**接口描述**: 用户名密码注册

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| fingerprint | String | 是 | 验证码指纹 | fingerprint |
| username | String | 是 | 用户名 | userName |
| password | String | 是 | 密码 | password |
| phone | String | 是 | 手机号 | phone |
| code | String | 是 | 验证码 | code |
| appId | Long | 是 | 应用ID | appId |
| storeId | Long | 否 | 门店ID | storeId |
| email | String | 否 | 邮箱 | email |

**请求示例**:
```json
{
  "fingerprint": "example_fingerprint",
  "username": "newuser",
  "password": "123456",
  "phone": "13800138000",
  "code": "123456",
  "appId": 1,
  "storeId": 1001,
  "email": "user@example.com"
}
```

**响应示例**:
```json
{
  "code": 2000,
  "msg": "注册成功",
  "data": {
    "id": 123456789,
    "username": "newuser",
    "phone": "13800138000",
    "email": "user@example.com",
    "appId": 1,
    "createTime": "2023-01-01T00:00:00"
  }
}
```

### 4. 忘记密码

**接口地址**: `POST /app/login/forgetPassword`

**接口描述**: 忘记密码

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| fingerprint | String | 是 | 验证码指纹 | fingerprint |
| phone | String | 是 | 手机号 | phone |
| code | String | 是 | 验证码 | code |
| password | String | 是 | 新密码 | password |
| appId | Long | 是 | 应用ID | appId |

**请求示例**:
```json
{
  "fingerprint": "example_fingerprint",
  "phone": "13800138000",
  "code": "123456",
  "password": "newpassword123",
  "appId": 1
}
```

**响应示例**:
```json
{
  "code": 2000,
  "msg": "操作成功",
  "data": null
}
```

### 5. 修改密码

**接口地址**: `POST /app/login/updatePassword`

**接口描述**: 修改密码

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| oldPassword | String | 是 | 旧密码 | oldpassword |
| newPassword | String | 是 | 新密码 | newpassword |

**请求示例**:
```json
{
  "oldPassword": "oldpassword123",
  "newPassword": "newpassword123"
}
```

**响应示例**:
```json
{
  "code": 2000,
  "msg": "操作成功",
  "data": null
}
```

### 6. 验证用户名是否可用

**接口地址**: `POST /app/login/accountUse`

**接口描述**: 验证用户名是否可用

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| username | String | 是 | 用户名 | userName |
| appId | Long | 是 | 应用ID | appId |

**请求示例**:
```json
{
  "username": "testuser",
  "appId": 1
}
```

**响应示例**:
```json
{
  "code": 2000,
  "msg": "操作成功",
  "data": true  // true表示用户名可用，false表示用户名已存在
}
```

## 第三方登录模块

三个渠道（微信小程序 / 微信公众号 / 支付宝）走的是同一套逻辑：

1. 前端拿到平台授权 `code`，调对应登录接口
2. 服务端拿 `code` 去平台换用户标识（openid / user_id）
3. 查 `au_user_third` 绑定表：
   - 绑过 → 直接登录
   - 没绑过但 `unionId` 命中（同一个人从微信另一个渠道来过）→ 补一条绑定，登录同一个账号
   - 完全没来过 → 自动注册一个本地账号再登录
4. 返回的 `data` 与用户名密码登录**完全一致**，前端登录态逻辑不用改

> **注意**：返回的 `jwtToken` 已经自带 `Bearer ` 前缀，客户端不要再拼一次。
> **注意**：请求里的 `appId` 是本系统的 `au_app.id`，不是微信/支付宝的 appId（那两个配在服务端 `cyyaw.third-login` 里）。
> **注意**：未配置对应平台凭据时，接口返回 `code=6000`、`msg=xxx登录未配置`，不会 500。

### 7. 用户登录-微信小程序登录(或注册)

**接口地址**: `POST /app/login/wechatMaLogin`

**接口描述**: 微信小程序登录(或注册)。`code` 由前端 `wx.login()` 获取

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| code | String | 是 | 微信登录凭证 | 081Xxx... |
| appId | Long | 是 | 本系统应用ID | 1 |
| storeId | Long | 否 | 门店ID | 1001 |
| nickName | String | 否 | 昵称（`wx.getUserProfile` 拿到的） | 微信用户 |
| face | String | 否 | 头像 | https://thirdwx.qlogo.cn/xxx |

**请求示例**:
```json
{
  "code": "081Xxx...",
  "appId": 1,
  "storeId": 1001,
  "nickName": "微信用户",
  "face": "https://thirdwx.qlogo.cn/xxx"
}
```

**响应示例**:
```json
{
  "code": 2000,
  "msg": "登录成功",
  "data": {
    "jwtToken": "Bearer eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "Bearer eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

### 8. 获取微信公众号网页授权链接

**接口地址**: `GET /app/login/wechatMpAuthUrl`

**接口描述**: 返回公众号网页授权跳转链接。前端跳过去，微信授权后带 `code` 回跳到 `redirectUri`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| appId | Long | 是 | 本系统应用ID | 1 |
| redirectUri | String | 是 | 前端回调页面地址 | https://xxx.com/#/app/1/login |
| state | String | 否 | 透传参数，原样带回 | 1 |

**响应示例**:
```json
{
  "code": 2000,
  "msg": "操作成功",
  "data": "https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx...&redirect_uri=...&response_type=code&scope=snsapi_userinfo&state=1#wechat_redirect"
}
```

> `redirectUri` 的域名必须已在公众号后台「网页授权域名」里配置过。

### 9. 用户登录-微信公众号登录(或注册)

**接口地址**: `POST /app/login/wechatMpLogin`

**接口描述**: 微信公众号网页授权登录(或注册)。昵称头像由微信直接返回，不需要前端上传

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| code | String | 是 | 网页授权回调带回的code | 081Xxx... |
| appId | Long | 是 | 本系统应用ID | 1 |
| storeId | Long | 否 | 门店ID | 1001 |

**请求示例**:
```json
{
  "code": "081Xxx...",
  "appId": 1,
  "storeId": 1001
}
```

**响应示例**: 同第 7 节

### 10. 用户登录-支付宝登录(或注册)

**接口地址**: `POST /app/login/alipayLogin`

**接口描述**: 支付宝登录(或注册)。`code` 由支付宝授权获取

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| code | String | 是 | 支付宝授权code | 081Xxx... |
| appId | Long | 是 | 本系统应用ID | 1 |
| storeId | Long | 否 | 门店ID | 1001 |
| nickName | String | 否 | 昵称（服务端取不到用户信息时用） | 支付宝用户 |
| face | String | 否 | 头像 | https://tfs.alipayobjects.com/xxx |

**请求示例**:
```json
{
  "code": "081Xxx...",
  "appId": 1,
  "storeId": 1001
}
```

**响应示例**: 同第 7 节



