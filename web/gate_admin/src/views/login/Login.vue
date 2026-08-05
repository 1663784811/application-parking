<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <div class="login-logo">
            <Icon type="ios-car" size="40" color="#fff" />
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
              prefix="ios-person-outline"
            >
              <template #prefix>
                <Icon type="ios-person-outline" />
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
                <Icon type="ios-lock-outline" />
              </template>
            </Input>
          </FormItem>

          <FormItem>
            <Label value=" remember" class="remember-label">
              <Checkbox v-model="formData.rememberMe">记住账号</Checkbox>
            </Label>
          </FormItem>

          <FormItem>
            <Button type="primary" size="large" block @click="handleLogin">
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
import { Form, FormItem, Input, Button, Icon, Message, Label, Checkbox } from 'view-ui-plus'
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
  padding: 32px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 24px;

  .login-logo {
    width: 64px;
    height: 64px;
    background: linear-gradient(135deg, #165DFF, #4080FF);
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 16px;
  }

  .login-title {
    font-size: 28px;
    font-weight: 700;
    color: #1a1a1a;
    margin-bottom: 8px;
  }

  .login-subtitle {
    font-size: 14px;
    color: #666;
  }
}

.login-form {
  margin-bottom: 16px;
}

.remember-checkbox {
  display: flex;
  align-items: center;
  gap: 8px;
}

.login-tips {
  text-align: center;
  font-size: 12px;
  color: #999;
}

.login-footer {
  padding: 24px;
  text-align: center;
  color: rgba(255, 255, 255, 0.8);
  font-size: 12px;
}
</style>