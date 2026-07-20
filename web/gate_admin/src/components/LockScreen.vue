<template>
  <div class="lock-screen-overlay">
    <div class="lock-screen-content">
      <div class="lock-icon">
        <Icon type="ios-lock-outline" size="48" />
      </div>
      <h2 class="lock-title">屏幕已锁定</h2>
      <p class="lock-desc">请输入密码解锁</p>

      <div class="lock-form">
        <Input
          v-model="password"
          type="password"
          placeholder="请输入解锁密码"
          size="large"
          password
          @on-enter="handleUnlock"
        />
        <div class="lock-hint">提示：默认密码为 123456</div>
        <Button type="primary" size="large" long @click="handleUnlock">
          <Icon type="ios-unlock-outline" />
          解锁
        </Button>
      </div>

      <div class="lock-user">
        <span>{{ userStore.userInfo.name }}</span>
        <span class="separator">|</span>
        <span>{{ systemStore.stats.parkingName }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Input, Button, Icon, Message } from 'view-ui-plus'
import { useUserStore } from '@/stores/user'
import { useSystemStore } from '@/stores/system'

const userStore = useUserStore()
const systemStore = useSystemStore()

const password = ref('')

const handleUnlock = () => {
  if (systemStore.unlockScreen(password.value)) {
    Message.success('已解锁')
    password.value = ''
  } else {
    Message.error('密码错误')
  }
}
</script>

<style lang="less" scoped>
.lock-screen-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.lock-screen-content {
  background-color: var(--color-bg-card);
  border-radius: var(--border-radius-xl);
  padding: var(--spacing-xxl) 48px;
  text-align: center;
  box-shadow: var(--shadow-lg);

  .lock-icon {
    width: 80px;
    height: 80px;
    background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    margin: 0 auto var(--spacing-lg);
  }

  .lock-title {
    font-size: var(--font-size-xl);
    font-weight: var(--font-weight-bold);
    color: var(--color-title);
    margin-bottom: var(--spacing-sm);
  }

  .lock-desc {
    font-size: var(--font-size-sm);
    color: var(--color-text-secondary);
    margin-bottom: var(--spacing-xl);
  }
}

.lock-form {
  width: 300px;

  .lock-hint {
    font-size: var(--font-size-xs);
    color: var(--color-text-secondary);
    text-align: left;
    margin: var(--spacing-sm) 0 var(--spacing-lg);
  }

  .ivu-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: var(--spacing-sm);
  }
}

.lock-user {
  margin-top: var(--spacing-xxl);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);

  .separator {
    margin: 0 var(--spacing-sm);
  }
}
</style>