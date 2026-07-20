<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-left">
        <div class="login-banner">
          <div class="banner-content">
            <h1 class="banner-title">智慧停车场管理平台</h1>
            <p class="banner-desc">高效、智能、便捷的停车场管理系统</p>
            <div class="banner-features">
              <div class="feature-item">
                <Icon type="ios-car" />
                <span>停车场管理</span>
              </div>
              <div class="feature-item">
                <Icon type="ios-camera" />
                <span>智能监控</span>
              </div>
              <div class="feature-item">
                <Icon type="ios-card" />
                <span>自动计费</span>
              </div>
              <div class="feature-item">
                <Icon type="ios-stats-chart" />
                <span>数据报表</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="login-right">
        <div class="login-form-wrapper">
          <div class="login-form-header">
            <h2 class="form-title">欢迎登录</h2>
            <p class="form-desc">请输入您的账号信息登录</p>
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
              >
                <template #prefix>
                  <Icon type="ios-person-outline" />
                </template>
              </Input>
            </FormItem>

            <FormItem prop="password">
              <Input
                v-model="state.formData.password"
                type="password"
                size="large"
                placeholder="请输入密码"
                password
              >
                <template #prefix>
                  <Icon type="ios-lock-outline" />
                </template>
              </Input>
            </FormItem>

            <FormItem prop="captcha">
              <div class="captcha-wrapper">
                <Input
                  v-model="state.formData.captcha"
                  size="large"
                  placeholder="请输入验证码"
                  @keyup.enter="handleLogin"
                >
                  <template #prefix>
                    <Icon type="ios-shield-outline" />
                  </template>
                </Input>
                <div class="captcha-code" @click="refreshCaptcha">
                  <img :src="state.captchaUrl" alt="验证码" />
                </div>
              </div>
            </FormItem>

            <FormItem>
              <Checkbox v-model="state.remember">记住密码</Checkbox>
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

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)

const state = reactive({
  formData: {
    username: 'admin',
    password: '123456',
    captcha: '',
    captchaId: ''
  },
  rules: {
    username: [
      { required: true, message: '请输入用户名', trigger: 'blur' }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' }
    ],
    captcha: [
      { required: true, message: '请输入验证码', trigger: 'blur' }
    ]
  },
  loading: false,
  remember: false,
  captchaUrl: ''
})

// 刷新验证码
const refreshCaptcha = () => {
  state.captchaUrl = `/api/captcha?t=${Date.now()}`
}

// 登录
const handleLogin = async () => {
  try {
    await formRef.value.validate()
    state.loading = true

    // 模拟登录请求
    setTimeout(() => {
      // 模拟登录成功
      userStore.setToken('mock_token_' + Date.now())
      userStore.setUserInfo({
        id: 1,
        name: state.formData.username,
        role: 'admin'
      })

      Message.success('登录成功')
      router.push({ name: 'home' })
      state.loading = false
    }, 1000)

  } catch (e) {
    console.error('表单验证失败:', e)
  } finally {
    state.loading = false
  }
}

onMounted(() => {
  refreshCaptcha()
})
</script>

<style lang="less" scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-container {
  display: flex;
  minWidth: 900px;
  height: 580px;
  background-color: var(--bg-color);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-heavy);
  overflow: hidden;
}

.login-left {
  minWidth: 400px;
  background: linear-gradient(135deg, #165DFF 0%, #0D47A1 100%);
  padding: 60px 40px;

  .login-banner {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;

    .banner-content {
      color: #fff;

      .banner-title {
        font-size: 28px;
        font-weight: 600;
        margin-bottom: 12px;
      }

      .banner-desc {
        font-size: var(--font-size-md);
        opacity: 0.9;
        margin-bottom: 40px;
      }

      .banner-features {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 20px;

        .feature-item {
          display: flex;
          align-items: center;

          .ivu-icon {
            font-size: 20px;
            margin-right: 8px;
          }

          span {
            font-size: var(--font-size-sm);
          }
        }
      }
    }
  }
}

.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;

  .login-form-wrapper {
    minWidth: 100%;
    max-minWidth: 320px;

    .login-form-header {
      margin-bottom: 32px;
      text-align: center;

      .form-title {
        font-size: 24px;
        font-weight: 600;
        color: var(--text-color-title);
        margin-bottom: 8px;
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
          minWidth: 100px;
          height: 40px;
          border: 1px solid var(--border-color);
          border-radius: var(--border-radius-sm);
          overflow: hidden;
          cursor: pointer;

          img {
            minWidth: 100%;
            height: 100%;
            object-fit: cover;
          }
        }
      }
    }
  }
}
</style>