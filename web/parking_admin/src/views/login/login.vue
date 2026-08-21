<template>
  <div class="login-page">
    <!-- 背景装饰元素 -->
    <div class="login-bg-decoration">
      <div class="bg-circle bg-circle--1"></div>
      <div class="bg-circle bg-circle--2"></div>
      <div class="bg-circle bg-circle--3"></div>
    </div>

    <div class="login-container">
      <!-- 左侧品牌展示区 -->
      <div class="login-left">
        <div class="login-banner">
          <div class="banner-content">
            <div class="banner-logo">
              <Icon type="ios-car" size="48" />
            </div>
            <h1 class="banner-title">智慧停车场管理平台</h1>
            <p class="banner-desc">高效 · 智能 · 便捷</p>
            <div class="banner-divider"></div>
            <div class="banner-features">
              <div class="feature-item">
                <Icon type="ios-checkmark-circle" />
                <span>停车场管理</span>
              </div>
              <div class="feature-item">
                <Icon type="ios-checkmark-circle" />
                <span>智能监控</span>
              </div>
              <div class="feature-item">
                <Icon type="ios-checkmark-circle" />
                <span>自动计费</span>
              </div>
              <div class="feature-item">
                <Icon type="ios-checkmark-circle" />
                <span>数据报表</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单区 -->
      <div class="login-right">
        <div class="login-form-wrapper">
          <div class="login-form-header">
            <h2 class="form-title">欢迎登录</h2>
            <p class="form-desc">请输入您的账号信息登录管理系统</p>
          </div>

          <Form
            ref="formRef"
            :model="state.formData"
            :rules="state.rules"
            class="login-form"
          >
            <FormItem prop="username">
              <Input
                v-model="state.formData.username"
                size="large"
                placeholder="请输入用户名"
                @keyup.enter="handleLogin"
              >
                <template #prefix>
                  <Icon type="ios-person-outline" />
                </template>
              </Input>
            </FormItem>

            <FormItem prop="password">
              <Input
                v-model="state.formData.password"
                :type="state.showPassword ? 'text' : 'password'"
                size="large"
                placeholder="请输入密码"
                password
                @keyup.enter="handleLogin"
              >
                <template #prefix>
                  <Icon type="ios-lock-outline" />
                </template>
              </Input>
            </FormItem>

            <FormItem prop="code">
              <div class="captcha-wrapper">
                <Input
                  v-model="state.formData.code"
                  size="large"
                  placeholder="请输入验证码"
                  @keyup.enter="handleLogin"
                >
                  <template #prefix>
                    <Icon type="ios-shield-outline" />
                  </template>
                </Input>
                <div class="captcha-code" title="点击刷新验证码" @click="refreshCaptcha">
                  <img :src="state.captchaUrl" alt="验证码" />
                </div>
              </div>
            </FormItem>

            <FormItem>
              <div class="login-options">
                <Checkbox v-model="state.remember">记住密码</Checkbox>
                <a class="forgot-password" href="javascript:void(0)">忘记密码？</a>
              </div>
            </FormItem>

            <FormItem>
              <Button
                type="primary"
                size="large"
                :loading="state.loading"
                long
                @click="handleLogin"
              >
                {{ state.loading ? '登录中...' : '登 录' }}
              </Button>
            </FormItem>
          </Form>

          <div class="login-footer">
            <router-link :to="{ name: 'welcome' }" class="back-welcome">
              <Icon type="ios-arrow-back" />
              返回首页
            </router-link>
            <span class="footer-sep">|</span>
            <span>还没有账号？</span>
            <router-link :to="{ name: 'register' }" class="register-link">注册企业</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Form, FormItem, Input, Button, Checkbox, Icon, Message } from 'view-ui-plus'
import { useUserStore } from '@/stores/user'
import { enterpriseApi } from '@/api'
import { getFingerprint } from '@/utils/fingerprint'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)

const state = reactive({
  formData: {
    username: '',
    password: '',
    code: ''
  },
  rules: {
    username: [
      { required: true, message: '请输入用户名', trigger: 'blur' }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, message: '密码至少6位', trigger: 'blur' }
    ],
    code: [
      { required: true, message: '请输入验证码', trigger: 'blur' }
    ]
  },
  loading: false,
  remember: false,
  showPassword: false,
  // 设备指纹：获取验证码与登录提交须使用同一指纹，否则验证码归属校验失败
  fingerprint: getFingerprint(),
  // 企业 ID：登录接口必填，由 findAny 返回的 AuEnterprise.id 取得
  // 保持字符串：后端 Long 序列化为字符串即为避免 JS 精度溢出，勿转 Number
  enId: '',
  // 图片验证码键值：getVerifyCode 返回，用于拼接验证码图片地址
  keyCode: '',
  captchaUrl: ''
})

// 获取记住的密码
const loadRememberedAccount = () => {
  const saved = localStorage.getItem('remembered_account')
  if (saved) {
    try {
      const account = JSON.parse(saved)
      state.formData.username = account.username || ''
      state.formData.password = account.password || ''
      state.remember = true
    } catch (e) {
      localStorage.removeItem('remembered_account')
    }
  }
}

// 保存记住的密码
const saveRememberedAccount = () => {
  if (state.remember) {
    localStorage.setItem('remembered_account', JSON.stringify({
      username: state.formData.username,
      password: state.formData.password
    }))
  } else {
    localStorage.removeItem('remembered_account')
  }
}

// 查询企业是否存在并取企业 ID
//   存在 → 记录 enId 供登录使用；不存在 → 跳转注册企业页
const loadEnterprise = async () => {
  try {
    const res = await enterpriseApi.checkEnterpriseExists()
    if (res.data) {
      // enId 取企业主键 id（AuEnterprise.id，后端 Long 序列化为字符串，直接保持字符串避免 JS 精度溢出）
      state.enId = res.data.id
    } else {
      Message.info('系统尚未注册企业，请先完成企业注册')
      router.push({ name: 'register' })
    }
  } catch (e) {
    // 查询异常时不阻断页面，登录提交由后端兜底校验
  }
}

// 刷新图片验证码：先取 keyCode，再拼图片地址
const refreshCaptcha = async () => {
  try {
    const res = await enterpriseApi.getVerifyCode({ fingerprint: state.fingerprint })
    // res.data 为验证码键值 keyCode
    state.keyCode = res.data
    state.captchaUrl = enterpriseApi.getVerifyImgUrl(state.keyCode)
  } catch (e) {
    // publicRequest 拦截器已弹出错误提示
  }
}

// 登录
const handleLogin = async () => {
  try {
    // 用户名/密码去首尾空格，避免误输空格致校验或登录失败
    state.formData.username = state.formData.username.trim()
    state.formData.password = state.formData.password.trim()

    const valid = await formRef.value.validate()
    if (!valid) return

    // 未取到企业 ID 时先补查一次，仍无则提示并引导注册
    if (!state.enId) {
      await loadEnterprise()
      if (!state.enId) {
        Message.error('未检测到企业信息，请先注册企业')
        return
      }
    }

    state.loading = true
    saveRememberedAccount()

    const res = await enterpriseApi.adminLogin({
      enId: state.enId,
      username: state.formData.username,
      password: state.formData.password,
      code: state.formData.code,
      fingerprint: state.fingerprint
    })
    // res.data = { jwtToken, refreshToken }
    userStore.setToken(res.data.jwtToken)
    localStorage.setItem('refreshToken', res.data.refreshToken)

    Message.success('登录成功')
    router.push({ name: 'home' })
  } catch (e) {
    // 登录失败：刷新验证码并清空验证码输入，publicRequest 拦截器已提示错误原因
    state.formData.code = ''
    refreshCaptcha()
  } finally {
    state.loading = false
  }
}

onMounted(() => {
  loadRememberedAccount()
  loadEnterprise()
  refreshCaptcha()
})
</script>

<style lang="less" scoped>
.login-page {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  overflow: hidden;
  background: var(--bg-color-page);

  // 背景装饰
  .login-bg-decoration {
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

  // 登录容器
  .login-container {
    position: relative;
    z-index: 1;
    display: flex;
    width: 960px;
    height: 560px;
    background-color: var(--bg-color);
    border-radius: var(--border-radius-xl);
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
    overflow: hidden;
  }

  // 左侧品牌区
  .login-left {
    width: 420px;
    padding: 48px 40px;
    background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-active-color) 100%);
    display: flex;
    align-items: center;
    justify-content: center;

    .login-banner {
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
          font-size: 26px;
          font-weight: 700;
          margin-bottom: 12px;
          letter-spacing: 1px;
        }

        .banner-desc {
          font-size: var(--font-size-md);
          opacity: 0.85;
          margin-bottom: 32px;
          letter-spacing: 2px;
        }

        .banner-divider {
          width: 40px;
          height: 3px;
          margin: 0 auto 32px;
          background: rgba(255, 255, 255, 0.4);
          border-radius: 2px;
        }

        .banner-features {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 16px;

          .feature-item {
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 8px 12px;
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
              font-size: var(--font-size-sm);
            }
          }
        }
      }
    }
  }

  // 右侧表单区
  .login-right {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 48px;

    .login-form-wrapper {
      width: 100%;
      max-width: 360px;

      .login-form-header {
        margin-bottom: 36px;
        text-align: center;

        .form-title {
          font-size: var(--font-size-xl);
          font-weight: 600;
          color: var(--text-color-title);
          margin-bottom: 10px;
        }

        .form-desc {
          font-size: var(--font-size-sm);
          color: var(--text-color-secondary);
        }
      }

      .login-form {
        .captcha-wrapper {
          display: flex;
          gap: 12px;

          .ivu-input-wrapper {
            flex: 1;
          }

          .captcha-code {
            width: 110px;
            height: 40px;
            border: 1px solid var(--border-color);
            border-radius: var(--border-radius-sm);
            overflow: hidden;
            cursor: pointer;
            flex-shrink: 0;
            transition: border-color 0.3s;

            &:hover {
              border-color: var(--primary-color);
            }

            img {
              width: 100%;
              height: 100%;
              object-fit: cover;
              display: block;
            }
          }
        }

        .login-options {
          display: flex;
          align-items: center;
          justify-content: space-between;
          width: 100%;

          .forgot-password {
            font-size: var(--font-size-sm);
            color: var(--primary-color);
            text-decoration: none;
            transition: opacity 0.3s;

            &:hover {
              opacity: 0.8;
            }
          }
        }
      }

      .login-footer {
        display: flex;
        align-items: center;
        justify-content: center;
        flex-wrap: wrap;
        margin-top: var(--spacing-xl);
        font-size: var(--font-size-sm);
        color: var(--text-color-secondary);

        .back-welcome {
          display: inline-flex;
          align-items: center;
          color: var(--text-color-secondary);
          text-decoration: none;
          transition: color 0.3s;

          .ivu-icon {
            margin-right: 4px;
          }

          &:hover {
            color: var(--primary-color);
          }
        }

        .footer-sep {
          margin: 0 var(--spacing-sm);
          color: var(--border-color);
        }

        .register-link {
          color: var(--primary-color);
          text-decoration: none;
          transition: opacity 0.3s;

          &:hover {
            opacity: 0.8;
          }
        }
      }
    }
  }
}
</style>
