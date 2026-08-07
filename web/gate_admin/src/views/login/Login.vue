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
            <img src="@/assets/icons/parking-logo.svg" alt="停车场管理系统" />
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
              placeholder="请输入保安账号"
              size="large"
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
              placeholder="请输入密码"
              size="large"
              password
              @on-enter="handleLogin"
            >
              <template #prefix>
                <Icon type="ios-lock-outline" />
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
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Form, FormItem, Input, Button, Checkbox, Icon, Message } from 'view-ui-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)

const state = reactive({
  // 表单数据
  formData: {
    username: '',
    password: '',
    remember: false
  },
  // 表单校验规则
  rules: {
    username: [
      { required: true, message: '请输入保安账号', trigger: 'blur' }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' }
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

    // 模拟登录验证
    if (state.formData.username === 'admin' && state.formData.password === '123456') {
      userStore.login({ name: '管理员李明', role: 'admin' })
    } else {
      userStore.login({
        name: state.formData.username,
        role: 'guard'
      })
    }

    Message.success('登录成功')
    router.push({ name: 'Home' })
  } catch (e) {
    console.error('表单验证失败', e)
  } finally {
    state.loading = false
  }
}
</script>

<style lang="less" scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

// 背景装饰圆
.login-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  background: #fff;
  opacity: 0.06;
}

.deco-circle--1 {
  width: 520px;
  height: 520px;
  top: -140px;
  right: -100px;
}

.deco-circle--2 {
  width: 360px;
  height: 360px;
  bottom: -80px;
  left: -80px;
}

.deco-circle--3 {
  width: 180px;
  height: 180px;
  bottom: 25%;
  right: 20%;
}

// 主体区域
.login-body {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;
}

// 登录卡片
.login-card {
  width: 420px;
  background: var(--color-bg-card);
  border-radius: var(--border-radius-xl);
  padding: 44px 36px 36px;
  box-shadow: var(--shadow-lg);
}

// 卡片头部
.login-card-header {
  text-align: center;
  margin-bottom: 36px;
}

.login-logo {
  width: 72px;
  height: 72px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--color-success) 0%, var(--color-success-light) 100%);
  border-radius: 18px;
  padding: 16px;
  color: #fff;
}

.login-title {
  font-size: var(--font-size-title);
  font-weight: var(--font-weight-bold);
  color: var(--color-title);
  margin-bottom: 6px;
}

// 表单区域
.login-form {
  margin-bottom: 8px;
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
}
</style>