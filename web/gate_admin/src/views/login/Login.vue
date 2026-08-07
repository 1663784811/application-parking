<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <div class="login-logo">
            <i class="fas fa-car"></i>
          </div>
          <h1 class="login-title">停车场管理系统</h1>
          <p class="login-subtitle">请输入账号密码登录</p>
        </div>

        <Form
          ref="formRef"
          :model="formData"
          :rules="rules"
          class="login-form"
        >
          <FormItem prop="username">
            <Input
              v-model="formData.username"
              placeholder="请输入保安账号"
              size="large"
              >
              <template #prefix>
                <i class="fas fa-user"></i>
              </template>
            </Input>
          </FormItem>

          <FormItem prop="password">
            <Input
              v-model="formData.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              password
              @on-enter="handleLogin"
            >
              <template #prefix>
                <i class="fas fa-lock"></i>
              </template>
            </Input>
          </FormItem>

          <FormItem>
            <label class="remember-label">
              <Checkbox v-model="formData.rememberMe">记住账号</Checkbox>
            </label>
          </FormItem>

          <FormItem>
            <Button type="primary" size="large" long @click="handleLogin">
              登录
            </Button>
          </FormItem>
        </Form>

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
import { reactive, ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Form, FormItem, Input, Button, Checkbox, Message } from 'view-ui-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)

const formData = reactive({
  username: '',
  password: '',
  rememberMe: false
})

const rules = {
  username: [{ required: true, message: '请输入保安账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 监听记住账号选项，保存到本地存储
watch(() => formData.rememberMe, (value) => {
  if (value && formData.username) {
    localStorage.setItem('parkAdminUsername', formData.username)
  } else if (!value) {
    localStorage.removeItem('parkAdminUsername')
  }
})

// 组件加载时检查本地存储的账号
onMounted(() => {
  const savedUsername = localStorage.getItem('parkAdminUsername')
  if (savedUsername) {
    formData.username = savedUsername
    formData.rememberMe = true
  }
})

const handleLogin = async () => {
  try {
    await formRef.value.validate()

    // 简化验证逻辑
    if (formData.username === 'admin' && formData.password === '123456') {
      userStore.login({ name: '管理员李明', role: 'admin' })
      Message.success('登录成功')
      router.push('/home')
    } else {
      // 通用登录
      userStore.login({
        name: formData.username,
        role: formData.username === 'admin' ? 'admin' : 'guard'
      })
      Message.success('登录成功')
      router.push('/home')
    }
  } catch (e) {
    console.error('验证失败', e)
  }
}
</script>

<style lang="less" scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 50%, #165DFF 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.login-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  width: 400px;
  background: #fff;
  border-radius: 12px;
  padding: 40px 32px 32px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.12);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;

  .login-logo {
    width: 64px;
    height: 64px;
    background: linear-gradient(135deg, #165DFF, #4080FF);
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 16px;
    color: #fff;
    font-size: 28px;
  }

  .login-title {
    font-size: 24px;
    font-weight: 700;
    color: #1a1a1a;
    margin-bottom: 8px;
  }

  .login-subtitle {
    font-size: 14px;
    color: #86909c;
  }
}

.login-form {
  margin-bottom: 16px;

  :deep(.ivu-input-wrapper-large) {
    .ivu-input {
      font-size: 14px;
      height: 44px;
    }
  }

  :deep(.ivu-input-prefix) {
    i {
      font-size: 16px;
      color: #86909c;
    }
  }

  :deep(.ivu-input:focus) {
    border-color: #165DFF;
    box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.1);
  }
}

.remember-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  cursor: pointer;
}

.login-tips {
  text-align: center;
  font-size: 12px;
  color: #c9cdd4;
  padding-top: 16px;
  border-top: 1px solid #f2f3f5;
}

.login-footer {
  padding: 24px;
  text-align: center;
  color: rgba(255, 255, 255, 0.8);
  font-size: 12px;
  letter-spacing: 0.5px;
}
</style>