<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <div class="login-logo">
            <Icon type="ios-car" size="40" color="#fff" />
          </div>
          <h1 class="login-title">停车场门口管理端</h1>
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
              placeholder="请输入账号"
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
            <Button type="primary" size="large" long @click="handleLogin">
              登 录
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
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Form, FormItem, Input, Button, Icon, Message } from 'view-ui-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)

const formData = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

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
  background-color: var(--color-bg-card);
  border-radius: var(--border-radius-xl);
  padding: var(--spacing-xxl);
  box-shadow: var(--shadow-lg);
}

.login-header {
  text-align: center;
  margin-bottom: var(--spacing-xxl);

  .login-logo {
    width: 64px;
    height: 64px;
    background: linear-gradient(135deg, #165DFF, #4080FF);
    border-radius: var(--border-radius-lg);
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto var(--spacing-lg);
  }

  .login-title {
    font-size: var(--font-size-xl);
    font-weight: var(--font-weight-bold);
    color: var(--color-title);
    margin-bottom: var(--spacing-sm);
  }

  .login-subtitle {
    font-size: var(--font-size-sm);
    color: var(--color-text-secondary);
  }
}

.login-form {
  margin-bottom: var(--spacing-lg);

  :deep(.ivu-btn) {
    font-size: var(--font-size-md);
    font-weight: var(--font-weight-medium);
  }
}

.login-tips {
  text-align: center;
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
}

.login-footer {
  padding: var(--spacing-lg);
  text-align: center;
  color: rgba(255, 255, 255, 0.8);
  font-size: var(--font-size-xs);
}
</style>