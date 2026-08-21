<template>
  <div class="register-page">
    <!-- 背景装饰元素 -->
    <div class="register-bg-decoration">
      <div class="bg-circle bg-circle--1"></div>
      <div class="bg-circle bg-circle--2"></div>
      <div class="bg-circle bg-circle--3"></div>
    </div>

    <div class="register-container">
      <!-- 左侧品牌展示区 -->
      <div class="register-left">
        <div class="register-banner">
          <div class="banner-content">
            <div class="banner-logo">
              <Icon type="ios-car" size="48" />
            </div>
            <h1 class="banner-title">智慧停车场管理平台</h1>
            <p class="banner-desc">高效 · 智能 · 便捷</p>
            <div class="banner-divider"></div>
            <p class="banner-subtitle">注册企业账号即可享受</p>
            <div class="banner-features">
              <div class="feature-item">
                <Icon type="ios-checkmark-circle" />
                <span>多车场统一管理</span>
              </div>
              <div class="feature-item">
                <Icon type="ios-checkmark-circle" />
                <span>7×24 智能监控</span>
              </div>
              <div class="feature-item">
                <Icon type="ios-checkmark-circle" />
                <span>自动计费对账</span>
              </div>
              <div class="feature-item">
                <Icon type="ios-checkmark-circle" />
                <span>专业数据报表</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧注册表单区 -->
      <div class="register-right">
        <div class="register-form-wrapper">
          <div class="register-form-header">
            <h2 class="form-title">注册企业账号</h2>
            <p class="form-desc">填写企业信息，开通管理平台账号</p>
          </div>

          <Form
            ref="formRef"
            :model="state.formData"
            :rules="state.rules"
            label-position="top"
            class="register-form"
          >
            <FormItem label="企业名称" prop="name" class="span-2">
              <Input
                v-model="state.formData.name"
                size="large"
                placeholder="请输入企业名称"
                maxlength="50"
              >
                <template #prefix>
                  <Icon type="ios-business" />
                </template>
              </Input>
            </FormItem>

            <FormItem label="企业 Logo" class="span-2">
              <Input
                v-model="state.formData.logo"
                size="large"
                placeholder="Logo URL，选填，注册后可在设置中上传"
              >
                <template #prefix>
                  <Icon type="ios-image" />
                </template>
              </Input>
            </FormItem>

            <FormItem label="联系人" prop="person">
              <Input
                v-model="state.formData.person"
                size="large"
                placeholder="请输入联系人姓名"
                maxlength="20"
              >
                <template #prefix>
                  <Icon type="ios-person-outline" />
                </template>
              </Input>
            </FormItem>

            <FormItem label="联系电话" prop="phone">
              <Input
                v-model="state.formData.phone"
                size="large"
                placeholder="请输入手机号"
                maxlength="11"
              >
                <template #prefix>
                  <Icon type="ios-call" />
                </template>
              </Input>
            </FormItem>

            <FormItem label="管理员账号" prop="username">
              <Input
                v-model="state.formData.username"
                size="large"
                placeholder="3-18位字母数字下划线"
                maxlength="18"
              >
                <template #prefix>
                  <Icon type="ios-person-outline" />
                </template>
              </Input>
            </FormItem>

            <FormItem label="登录密码" prop="password">
              <Input
                v-model="state.formData.password"
                type="password"
                size="large"
                placeholder="6-16位数字或字母"
                password
                maxlength="16"
              >
                <template #prefix>
                  <Icon type="ios-lock-outline" />
                </template>
              </Input>
            </FormItem>

            <FormItem label="确认密码" prop="confirmPassword" class="span-2">
              <Input
                v-model="state.formData.confirmPassword"
                type="password"
                size="large"
                placeholder="请再次输入密码"
                password
                maxlength="16"
              >
                <template #prefix>
                  <Icon type="ios-lock-outline" />
                </template>
              </Input>
            </FormItem>

            <FormItem label="手机验证码" prop="code" class="span-2">
              <div class="code-row">
                <Input
                  v-model="state.formData.code"
                  size="large"
                  placeholder="请输入手机验证码"
                  maxlength="6"
                >
                  <template #prefix>
                    <Icon type="ios-text" />
                  </template>
                </Input>
                <Button
                  type="primary"
                  size="large"
                  class="code-btn"
                  :loading="state.sending"
                  :disabled="state.countdown > 0"
                  @click="handleSendCode"
                >
                  {{ state.countdown > 0 ? `${state.countdown}s` : '获取验证码' }}
                </Button>
              </div>
            </FormItem>

            <FormItem prop="agree" class="span-2">
              <Checkbox v-model="state.formData.agree">
                我已阅读并同意 <a class="protocol-link" href="javascript:void(0)">《企业服务协议》</a>
              </Checkbox>
            </FormItem>

            <FormItem class="span-2">
              <Button
                type="primary"
                size="large"
                :loading="state.loading"
                long
                @click="handleRegister"
              >
                {{ state.loading ? '提交中...' : '提交注册' }}
              </Button>
            </FormItem>
          </Form>

          <div class="register-footer">
            <span>已有账号？</span>
            <router-link :to="{ name: 'login' }" class="footer-link">立即登录</router-link>
            <span class="footer-divider">·</span>
            <router-link :to="{ name: 'welcome' }" class="footer-link">返回首页</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Form, FormItem, Input, Button, Checkbox, Icon, Message
} from 'view-ui-plus'
import { enterpriseApi } from '@/api'
import { getFingerprint } from '@/utils/fingerprint'

const router = useRouter()
const formRef = ref(null)

// 验证码倒计时定时器（句柄，非展示状态）
let codeTimer = null

const state = reactive({
  formData: {
    name: '',
    logo: '',
    person: '',
    phone: '',
    username: '',
    password: '',
    confirmPassword: '',
    code: '',
    agree: false
  },
  // 设备指纹：发送验证码与提交注册须使用同一指纹，否则验证码校验失败
  fingerprint: getFingerprint(),
  loading: false,
  sending: false,
  countdown: 0
})

// 校验：联系人（2 位以上）
const validatePerson = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入联系人姓名'))
  } else if (value.length < 2) {
    callback(new Error('联系人至少 2 个字符'))
  } else {
    callback()
  }
}

// 校验：联系电话（11 位手机号）
const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入联系电话'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

// 校验：管理员账号（3-18 位字母数字下划线）
const validateUsername = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入管理员账号'))
  } else if (!/^[a-zA-Z0-9_]{3,18}$/.test(value)) {
    callback(new Error('账号为 3-18 位字母、数字或下划线'))
  } else {
    callback()
  }
}

// 校验：登录密码（6-16 位数字或字母）
const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入登录密码'))
  } else if (!/^[a-zA-Z0-9]{6,16}$/.test(value)) {
    callback(new Error('密码为 6-16 位数字或字母'))
  } else {
    callback()
  }
}

// 校验：确认密码
const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== state.formData.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 校验：协议
const validateAgree = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请阅读并同意服务协议'))
  } else {
    callback()
  }
}

state.rules = {
  name: [
    { required: true, message: '请输入企业名称', trigger: 'blur' },
    { min: 3, max: 50, message: '企业名称长度为 3-50 个字符', trigger: 'blur' }
  ],
  person: [
    { required: true, validator: validatePerson, trigger: 'blur' }
  ],
  phone: [
    { required: true, validator: validatePhone, trigger: 'blur' }
  ],
  username: [
    { required: true, validator: validateUsername, trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入手机验证码', trigger: 'blur' }
  ],
  agree: [
    { required: true, validator: validateAgree, trigger: 'change' }
  ]
}

// 发送手机验证码并启动 60s 倒计时
const handleSendCode = async () => {
  if (!/^1[3-9]\d{9}$/.test(state.formData.phone)) {
    Message.warning('请先输入正确的手机号')
    return
  }
  if (state.countdown > 0 || state.sending) return
  try {
    state.sending = true
    await enterpriseApi.sendRegisterPhoneCode({
      fingerprint: state.fingerprint,
      phone: state.formData.phone
    })
    Message.success('验证码已发送，请查收')
    state.countdown = 60
    codeTimer = setInterval(() => {
      state.countdown--
      if (state.countdown <= 0) {
        clearInterval(codeTimer)
        codeTimer = null
      }
    }, 1000)
  } catch (e) {
    // publicRequest 拦截器已弹出错误提示
  } finally {
    state.sending = false
  }
}

// 提交注册：组装真实接口所需字段并调用 registerEnterprise
const handleRegister = async () => {
  try {
    // 用户名/密码去首尾空格，避免误输空格致校验或注册失败
    state.formData.username = state.formData.username.trim()
    state.formData.password = state.formData.password.trim()

    const valid = await formRef.value.validate()
    if (!valid) return

    state.loading = true
    await enterpriseApi.registerEnterprise({
      name: state.formData.name,
      logo: state.formData.logo || '',
      person: state.formData.person,
      phone: state.formData.phone,
      username: state.formData.username,
      password: state.formData.password,
      code: state.formData.code,
      fingerprint: state.fingerprint
    })
    Message.success('企业注册成功，请登录')
    router.push({ name: 'login' })
  } catch (e) {
    // publicRequest 拦截器已弹出错误提示
  } finally {
    state.loading = false
  }
}

onUnmounted(() => {
  if (codeTimer) {
    clearInterval(codeTimer)
    codeTimer = null
  }
})
</script>

<style lang="less" scoped>
.register-page {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: var(--spacing-xxl) 0;
  overflow: hidden;
  background: var(--bg-color-page);

  // 背景装饰
  .register-bg-decoration {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
    overflow: hidden;

    .bg-circle {
      position: absolute;
      border-radius: 50%;
      opacity: 0.06;
      background: var(--primary-color);

      &--1 {
        top: -150px;
        right: -100px;
        width: 500px;
        height: 500px;
      }

      &--2 {
        bottom: -200px;
        left: -100px;
        width: 600px;
        height: 600px;
      }

      &--3 {
        top: 50%;
        left: 30%;
        width: 300px;
        height: 300px;
        transform: translate(-50%, -50%);
        opacity: 0.04;
      }
    }
  }

  // 注册容器
  .register-container {
    position: relative;
    z-index: 1;
    display: flex;
    width: 1040px;
    min-height: 640px;
    background-color: var(--bg-color);
    border-radius: var(--border-radius-xl);
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
    overflow: hidden;
  }

  // 左侧品牌区
  .register-left {
    width: 360px;
    padding: 48px 36px;
    background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-active-color) 100%);
    display: flex;
    align-items: center;
    justify-content: center;

    .register-banner {
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;

      .banner-content {
        color: #fff;
        text-align: center;

        .banner-logo {
          margin-bottom: 24px;

          .ivu-icon {
            color: #fff;
            opacity: 0.95;
          }
        }

        .banner-title {
          font-size: 24px;
          font-weight: 700;
          margin-bottom: 12px;
          letter-spacing: 1px;
        }

        .banner-desc {
          font-size: var(--font-size-md);
          opacity: 0.85;
          margin-bottom: 28px;
          letter-spacing: 2px;
        }

        .banner-divider {
          width: 40px;
          height: 3px;
          margin: 0 auto 20px;
          background: rgba(255, 255, 255, 0.4);
          border-radius: 2px;
        }

        .banner-subtitle {
          font-size: var(--font-size-sm);
          opacity: 0.8;
          margin-bottom: 20px;
        }

        .banner-features {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 12px;

          .feature-item {
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 8px 10px;
            background: rgba(255, 255, 255, 0.1);
            border-radius: var(--border-radius-base);
            transition: background 0.3s;

            &:hover {
              background: rgba(255, 255, 255, 0.18);
            }

            .ivu-icon {
              font-size: 18px;
              margin-right: 6px;
              color: rgba(255, 255, 255, 0.9);
            }

            span {
              font-size: var(--font-size-xs);
            }
          }
        }
      }
    }
  }

  // 右侧表单区
  .register-right {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 36px 40px;

    .register-form-wrapper {
      width: 100%;
      max-width: 540px;

      .register-form-header {
        margin-bottom: 24px;
        text-align: center;

        .form-title {
          font-size: var(--font-size-xl);
          font-weight: 600;
          color: var(--text-color-title);
          margin-bottom: 8px;
        }

        .form-desc {
          font-size: var(--font-size-sm);
          color: var(--text-color-secondary);
        }
      }

      .register-form {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 0 20px;

        // 跨两列的表单项
        .span-2 {
          grid-column: 1 / -1;
        }

        // 验证码一行：输入框 + 获取按钮
        .code-row {
          display: flex;
          gap: var(--spacing-sm);

          .ivu-input-wrapper {
            flex: 1;
          }
        }

        // 缩小表单项默认下间距，使整体更紧凑
        :deep(.ivu-form-item) {
          margin-bottom: 18px;
        }

        .protocol-link {
          color: var(--primary-color);
          text-decoration: none;

          &:hover {
            opacity: 0.8;
          }
        }
      }

      .register-footer {
        margin-top: 8px;
        text-align: center;
        font-size: var(--font-size-sm);
        color: var(--text-color-secondary);

        .footer-link {
          color: var(--primary-color);
          text-decoration: none;
          transition: opacity 0.3s;

          &:hover {
            opacity: 0.8;
          }
        }

        .footer-divider {
          margin: 0 var(--spacing-sm);
          color: var(--border-color);
        }
      }
    }
  }
}

// 响应式
@media (max-width: 1080px) {
  .register-page {
    .register-container {
      width: 92%;
    }

    .register-form-wrapper {
      .register-form {
        grid-template-columns: 1fr;
      }
    }
  }
}

@media (max-width: 768px) {
  .register-page {
    .register-container {
      flex-direction: column;
      width: 92%;
      min-height: auto;
    }

    .register-left {
      width: 100%;
      padding: 32px;
    }
  }
}
</style>
