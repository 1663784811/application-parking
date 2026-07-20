<template>
  <Drawer
    :value="visible"
    title="消息通知"
    width="400"
    placement="right"
    @on-close="handleClose"
  >
    <template #extra>
      <Button type="text" size="small" @click="handleMarkAll">全部已读</Button>
    </template>

    <div class="notification-list">
      <div
        v-for="item in systemStore.notifications"
        :key="item.id"
        class="notification-item"
        :class="{ unread: !item.read, [`level-${item.level}`]: true }"
        @click="handleRead(item)"
      >
        <div class="notification-icon">
          <Icon
            :type="getIconType(item.type)"
            :color="getIconColor(item.level)"
          />
        </div>
        <div class="notification-content">
          <div class="notification-title">{{ item.title }}</div>
          <div class="notification-text">{{ item.content }}</div>
          <div class="notification-time">{{ item.time }}</div>
        </div>
        <div class="notification-badge" v-if="!item.read"></div>
      </div>

      <div v-if="systemStore.notifications.length === 0" class="notification-empty">
        <Icon type="ios-notifications-off-outline" size="48" />
        <p>暂无通知</p>
      </div>
    </div>
  </Drawer>
</template>

<script setup>
import { Drawer, Button, Icon } from 'view-ui-plus'
import { useSystemStore } from '@/stores/system'

const props = defineProps({
  visible: Boolean
})

const emit = defineEmits(['update:visible'])

const systemStore = useSystemStore()

const getIconType = (type) => {
  const map = {
    device: 'ios-construct-outline',
    exception: 'ios-alert-outline',
    system: 'ios-information-circle-outline'
  }
  return map[type] || 'ios-notifications-outline'
}

const getIconColor = (level) => {
  const map = {
    danger: '#F53F3F',
    warning: '#FF7D00',
    success: '#00B42A',
    info: '#165DFF'
  }
  return map[level] || '#165DFF'
}

const handleClose = () => {
  emit('update:visible', false)
}

const handleRead = (item) => {
  systemStore.markAsRead(item.id)
}

const handleMarkAll = () => {
  systemStore.markAllAsRead()
}
</script>

<style lang="less" scoped>
.notification-list {
  height: 100%;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  padding: var(--spacing-lg);
  border-bottom: 1px solid var(--color-border-light);
  cursor: pointer;
  transition: background-color var(--transition-fast);
  position: relative;

  &:hover {
    background-color: var(--color-bg);
  }

  &.unread {
    background-color: rgba(22, 93, 255, 0.04);

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 3px;
      background-color: var(--color-primary);
    }
  }

  &.level-danger::before {
    background-color: var(--color-danger);
  }

  &.level-warning::before {
    background-color: var(--color-warning);
  }

  &.level-success::before {
    background-color: var(--color-success);
  }
}

.notification-icon {
  margin-right: var(--spacing-md);
}

.notification-content {
  flex: 1;

  .notification-title {
    font-size: var(--font-size-sm);
    font-weight: var(--font-weight-medium);
    color: var(--color-title);
    margin-bottom: var(--spacing-xs);
  }

  .notification-text {
    font-size: var(--font-size-sm);
    color: var(--color-body);
    margin-bottom: var(--spacing-xs);
    line-height: 1.4;
  }

  .notification-time {
    font-size: var(--font-size-xs);
    color: var(--color-text-secondary);
  }
}

.notification-badge {
  width: 8px;
  height: 8px;
  background-color: var(--color-danger);
  border-radius: 50%;
  position: absolute;
  top: 50%;
  right: var(--spacing-lg);
  transform: translateY(-50%);
}

.notification-empty {
  text-align: center;
  padding: var(--spacing-xxl);
  color: var(--color-text-secondary);

  .ivu-icon {
    margin-bottom: var(--spacing-md);
  }
}
</style>