<template>
  <div class="login-page">
    <!-- 背景装饰圆 -->
    <div class="login-decoration">
      <div class="deco-circle deco-circle--1"></div>
      <div class="deco-circle deco-circle--2"></div>
      <div class="deco-circle deco-circle--3"></div>
    </div>

    <div class="login-body">
      <div class="login-card">
        <!-- 卡片头部 -->
        <div class="login-card-header">
          <div class="login-logo">
            <img src="@/assets/icons/parkingLogo.svg" alt="停车场管理系统"/>
          </div>
          <h1 class="login-title">停车场管理系统</h1>
        </div>

        <!-- 登录表单 -->
        <Form
            ref="formRef"
            :model="state.formData"
            :rules="state.rules"
            class="login-form"
        >
          <FormItem prop="username">
            <Input
                v-model="state.formData.username"
                placeholder="请输入账号"
                size="large"
            >
              <template #prefix>
                <Icon type="ios-person-outline"/>
              </template>
            </Input>
          </FormItem>

          <FormItem prop="password">
            <Input
                v-model="state.formData.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                password
                @on-enter="handleLogin"
            >
              <template #prefix>
                <Icon type="ios-lock-outline"/>
              </template>
            </Input>
          </FormItem>

          <FormItem>
            <div class="form-extra">
              <Checkbox v-model="state.formData.remember">
                记住账号
              </Checkbox>
            </div>
          </FormItem>

          <FormItem>
            <Button
                type="success"
                size="large"
                long
                :loading="state.loading"
                @click="handleLogin"
            >
              登 录
            </Button>
          </FormItem>
        </Form>

        <!-- 提示信息 -->
        <div class="login-tips">
          <span>演示账号：admin / 123456</span>
        </div>
      </div>
    </div>

    <div class="login-footer">
      <span>停车场管理系统 v1.0</span>
    </div>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {Button, Checkbox, Form, FormItem, Icon, Input, Message} from 'view-ui-plus'
import {useUserStore} from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)

const state = reactive({
  // 表单数据
  formData: {
    username: 'admin',
    password: '123456',
    remember: false
  },
  // 表单校验规则
  rules: {
    username: [
      {required: true, message: '请输入账号', trigger: 'blur'}
    ],
    password: [
      {required: true, message: '请输入密码', trigger: 'blur'}
    ]
  },
  // 登录按钮加载状态
  loading: false
})

// 组件挂载时，读取本地存储的账号
onMounted(() => {
  const savedUsername = localStorage.getItem('parkAdminUsername')
  if (savedUsername) {
    state.formData.username = savedUsername
    state.formData.remember = true
  }
})

// 处理记住账号
const saveRememberAccount = () => {
  if (state.formData.remember && state.formData.username) {
    localStorage.setItem('parkAdminUsername', state.formData.username)
  } else {
    localStorage.removeItem('parkAdminUsername')
  }
}

// 登录操作
const handleLogin = async () => {
  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    state.loading = true

    // 保存记住账号状态
    saveRememberAccount()

    // 调用后端登录接口
    await userStore.login({
      username: state.formData.username,
      password: state.formData.password
    })

    Message.success('登录成功')
    router.push({name: 'Home'})
  } catch (e) {
    // 表单校验失败或登录失败（错误提示已由 axios 拦截器处理）
  } finally {
    state.loading = false
  }
}
</script>

<style lang="less" scoped>
// ===== 关键帧动画 =====
@keyframes decoFloat1 {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-28px, 22px); }
}
@keyframes decoFloat2 {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(24px, -18px); }
}
@keyframes decoFloat3 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(14px, -16px) scale(1.12); }
}
@keyframes fadeUp {
  from { opacity: 0; transform: translateY(24px); }
  to { opacity: 1; transform: translateY(0); }
}
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
@keyframes logoPop {
  0% { opacity: 0; transform: scale(0.6); }
  60% { opacity: 1; transform: scale(1.08); }
  100% { opacity: 1; transform: scale(1); }
}

.login-page {
  position: relative;
  min-height: 100vh;
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  display: flex;
  flex-direction: column;
  overflow: hidden;

  // 背景装饰圆
  .login-decoration {
    position: absolute;
    inset: 0;
    pointer-events: none;
    overflow: hidden;

    .deco-circle {
      position: absolute;
      border-radius: 50%;
      background: #fff;
      opacity: 0.06;
      will-change: transform;

      &--1 {
        width: 520px;
        height: 520px;
        top: -140px;
        right: -100px;
        animation: decoFloat1 9s ease-in-out infinite;
      }

      &--2 {
        width: 360px;
        height: 360px;
        bottom: -80px;
        left: -80px;
        animation: decoFloat2 11s ease-in-out infinite;
      }

      &--3 {
        width: 180px;
        height: 180px;
        bottom: 25%;
        right: 20%;
        animation: decoFloat3 7s ease-in-out infinite;
      }
    }
  }

  // 主体区域
  .login-body {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    z-index: 1;

    // 登录卡片
    .login-card {
      width: 500px;
      background: var(--color-bg-card);
      border-radius: var(--border-radius-xl);
      padding: 44px 36px 36px;
      box-shadow: var(--shadow-lg);
      animation: fadeUp 0.6s ease both;

      // 卡片头部
      .login-card-header {
        text-align: center;
        margin-bottom: 36px;

        .login-logo {
          width: 90px;
          height: 90px;
          margin: 0 auto 20px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: linear-gradient(135deg, var(--color-bg) 0%, var(--color-text-secondary) 100%);
          border-radius: 18px;
          padding: 16px;
          color: #fff;
          animation: logoPop 0.8s 0.25s ease both;

          img {
            max-width: 100%;
            max-height: 100%;
          }
        }

        .login-title {
          font-size: var(--font-size-title);
          font-weight: var(--font-weight-bold);
          color: var(--color-title);
          margin-bottom: 6px;
          animation: fadeUp 0.6s 0.4s ease both;
        }
      }

      // 表单区域
      .login-form {
        margin-bottom: 8px;
        animation: fadeUp 0.6s 0.55s ease both;

        // 登录按钮交互
        :deep(.ivu-btn) {
          transition: transform 0.18s ease, box-shadow 0.18s ease, filter 0.18s ease;

          &:hover {
            transform: translateY(-2px);
            filter: brightness(1.05);
          }

          &:active {
            transform: translateY(0);
          }
        }
      }

      // 表单额外选项
      .form-extra {
        display: flex;
        align-items: center;
        font-size: var(--font-size-sm);
      }

      // 提示信息
      .login-tips {
        text-align: center;
        font-size: var(--font-size-xs);
        color: var(--color-text-secondary);
        padding-top: 20px;
        border-top: 1px solid var(--color-border-light);
        animation: fadeIn 0.6s 0.75s ease both;
      }
    }
  }

  // 页脚
  .login-footer {
    position: relative;
    z-index: 1;
    padding: 24px;
    text-align: center;
    font-size: var(--font-size-xs);
    color: rgba(255, 255, 255, 0.75);
    letter-spacing: 0.5px;
    animation: fadeIn 0.8s 0.9s ease both;
  }
}

// 尊重用户的减少动态偏好
@media (prefers-reduced-motion: reduce) {
  .login-page,
  .login-page * {
    animation: none !important;
    transition: none !important;
  }
}
</style>