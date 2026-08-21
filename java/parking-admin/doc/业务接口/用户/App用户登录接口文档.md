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



