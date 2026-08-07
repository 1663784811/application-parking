<template>
  <div class="real-time-monitor-page">
    <div class="monitor-container">
      <!-- 左侧通道选择 -->
      <div class="channel-sidebar">
        <div class="sidebar-title">通道列表</div>
        <div class="channel-list">
          <div
            v-for="channel in state.channelList"
            :key="channel.id"
            class="channel-item"
            :class="{ active: state.currentChannel?.id === channel.id }"
            @click="handleChannelChange(channel)"
          >
            <Icon :type="channel.type === 'in' ? 'ios-log-in' : 'ios-log-out'" />
            <span class="channel-name">{{ channel.name }}</span>
            <Badge
              v-if="channel.alertCount > 0"
              :count="channel.alertCount"
              :overflow-count="99"
              type="error"
            />
          </div>
        </div>
      </div>

      <!-- 右侧监控区域 -->
      <div class="monitor-main">
        <div class="monitor-header">
          <h3 class="monitor-title">{{ state.currentChannel?.name || '请选择通道' }}</h3>
          <div class="monitor-actions">
            <Button type="primary" @click="handleOpenGate" :loading="state.opening">
              <Icon type="ios-lock-open" />
              远程开闸
            </Button>
            <Button @click="handleBlacklist">
              <Icon type="ios-close-circle-outline" />
              加入黑名单
            </Button>
          </div>
        </div>

        <!-- 视频监控区 -->
        <div class="video-container">
          <div class="video-placeholder">
            <Icon type="ios-videocam" />
            <span>实时视频画面</span>
          </div>
        </div>

        <!-- 抓拍信息 -->
        <div class="capture-info">
          <Row :gutter="16">
            <Col span="6">
              <div class="info-item">
                <span class="info-label">车牌号码</span>
                <span class="info-value">{{ state.captureInfo.plate || '-' }}</span>
              </div>
            </Col>
            <Col span="6">
              <div class="info-item">
                <span class="info-label">车辆类型</span>
                <span class="info-value">{{ state.captureInfo.carType || '-' }}</span>
              </div>
            </Col>
            <Col span="6">
              <div class="info-item">
                <span class="info-label">识别时间</span>
                <span class="info-value">{{ state.captureInfo.time || '-' }}</span>
              </div>
            </Col>
            <Col span="6">
              <div class="info-item">
                <span class="info-label">通行状态</span>
                <span class="info-value" :class="getStatusClass(state.captureInfo.status)">
                  {{ state.captureInfo.statusText || '-' }}
                </span>
              </div>
            </Col>
          </Row>
        </div>

        <!-- 实时通行消息 -->
        <div class="passage-list">
          <div class="passage-header">
            <span class="passage-title">实时通行消息</span>
            <span class="passage-count">今日已记录 {{ state.passageCount }} 条</span>
          </div>
          <div class="passage-table">
            <Table
              :columns="passageColumns"
              :data="state.passageList"
              :height="300"
              size="small"
            >
              <template #type="{ row }">
                <Tag :color="row.type === 'in' ? 'blue' : 'green'">
                  {{ row.type === 'in' ? '进场' : '出场' }}
                </Tag>
              </template>
              <template #status="{ row }">
                <span class="status-badge" :class="'status-' + row.status">
                  {{ getStatusText(row.status) }}
                </span>
              </template>
              <template #action="{ row }">
                <Button type="text" size="small" @click="handleViewPassage(row)">详情</Button>
                <Button type="text" size="small" @click="handleOpenGateByPassage(row)">开闸</Button>
              </template>
            </Table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import {
  Icon,
  Badge,
  Button,
  Row,
  Col,
  Table,
  Tag,
  Modal,
  Message
} from 'view-ui-plus'

const state = reactive({
  channelList: [
    { id: 1, name: '1号入口', type: 'in', alertCount: 2 },
    { id: 2, name: '2号出口', type: 'out', alertCount: 1 },
    { id: 3, name: '3号入口', type: 'in', alertCount: 0 },
    { id: 4, name: '地下入口', type: 'in', alertCount: 0 }
  ],
  currentChannel: null,
  opening: false,
  passageCount: 1654,

  captureInfo: {
    plate: '京A12345',
    carType: '小型车',
    time: '2024-01-15 14:32:15',
    status: 'normal',
    statusText: '正常'
  },

  passageList: []
})

const passageColumns = [
  { title: '车牌号', key: 'plate', minWidth: 120 },
  { title: '通行类型', slot: 'type', minWidth: 80 },
  { title: '通道', key: 'channel', minWidth: 100 },
  { title: '通行时间', key: 'time', minWidth: 160 },
  { title: '状态', slot: 'status', minWidth: 100 },
  { title: '操作', slot: 'action', minWidth: 120 }
]

const getStatusText = (status) => {
  const map = {
    normal: '正常',
    noPlate: '无牌',
    blacklist: '黑名单',
    unpaid: '欠费'
  }
  return map[status] || status
}

const getStatusClass = (status) => {
  const map = {
    normal: '',
    noPlate: 'warning',
    blacklist: 'danger',
    unpaid: 'danger'
  }
  return map[status] || ''
}

const handleChannelChange = (channel) => {
  state.currentChannel = channel
  loadPassageList()
}

const loadPassageList = () => {
  setTimeout(() => {
    state.passageList = [
      { id: 1, plate: '京A12345', type: 'in', channel: '1号入口', time: '2024-01-15 14:32:15', status: 'normal' },
      { id: 2, plate: '京B67890', type: 'out', channel: '2号出口', time: '2024-01-15 14:30:00', status: 'normal' },
      { id: 3, plate: '无牌车', type: 'in', channel: '3号入口', time: '2024-01-15 14:28:00', status: 'noPlate' },
      { id: 4, plate: '浙C11111', type: 'in', channel: '1号入口', time: '2024-01-15 14:25:00', status: 'normal' },
      { id: 5, plate: '黑名单', type: 'out', channel: '2号出口', time: '2024-01-15 14:20:00', status: 'blacklist' }
    ]
  }, 300)
}

const handleOpenGate = () => {
  if (!state.currentChannel) {
    Message.warning('请先选择通道')
    return
  }
  state.opening = true
  setTimeout(() => {
    Message.success('开闸命令已发送')
    state.opening = false
  }, 1000)
}

const handleOpenGateByPassage = (row) => {
  Message.success(`正在为 ${row.plate} 开闸...`)
}

const handleBlacklist = () => {
  if (!state.captureInfo.plate) {
    Message.warning('当前无抓拍车辆')
    return
  }
  Modal.confirm({
    title: '确认加入黑名单',
    content: `确定要将 "${state.captureInfo.plate}" 加入黑名单吗？`,
    onOk: () => {
      Message.success('已加入黑名单')
    }
  })
}

const handleViewPassage = (row) => {
  console.log('查看通行详情', row)
}

loadPassageList()
</script>

<style lang="less" scoped>
.real-time-monitor-page {
  height: calc(100vh - var(--header-height) - 32px);

  .monitor-container {
    display: flex;
    height: 100%;
    gap: var(--spacing-lg);
  }

  .channel-sidebar {
    min-width: 200px;
    flex-shrink: 0;
    background-color: var(--bg-color);
    border-radius: var(--border-radius-base);
    box-shadow: var(--shadow-base);
    padding: var(--spacing-lg);

    .sidebar-title {
      font-size: var(--font-size-md);
      font-weight: 600;
      color: var(--text-color-title);
      margin-bottom: var(--spacing-lg);
    }

    .channel-list {
      .channel-item {
        display: flex;
        align-items: center;
        padding: var(--spacing-md);
        margin-bottom: var(--spacing-sm);
        border-radius: var(--border-radius-sm);
        cursor: pointer;
        transition: background-color 0.2s;

        &:hover {
          background-color: var(--bg-color-hover);
        }

        &.active {
          background-color: rgba(22, 93, 255, 0.1);
          color: var(--primary-color);
        }

        .ivu-icon {
          margin-right: var(--spacing-sm);
        }

        .channel-name {
          flex: 1;
          font-size: var(--font-size-sm);
        }
      }
    }
  }

  .monitor-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: var(--spacing-lg);

    .monitor-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      background-color: var(--bg-color);
      padding: var(--spacing-lg);
      border-radius: var(--border-radius-base);
      box-shadow: var(--shadow-base);

      .monitor-title {
        font-size: var(--font-size-lg);
        font-weight: 600;
        color: var(--text-color-title);
      }

      .monitor-actions {
        display: flex;
        gap: var(--spacing-md);

        .ivu-btn {
          display: inline-flex;
          align-items: center;

          .ivu-icon {
            margin-right: 4px;
          }
        }
      }
    }

    .video-container {
      height: 300px;
      background-color: #1a1a1a;
      border-radius: var(--border-radius-base);
      display: flex;
      align-items: center;
      justify-content: center;

      .video-placeholder {
        color: #666;
        display: flex;
        flex-direction: column;
        align-items: center;

        .ivu-icon {
          font-size: 48px;
          margin-bottom: var(--spacing-sm);
        }
      }
    }

    .capture-info {
      background-color: var(--bg-color);
      padding: var(--spacing-xl);
      border-radius: var(--border-radius-base);
      box-shadow: var(--shadow-base);

      .info-item {
        display: flex;
        flex-direction: column;

        .info-label {
          font-size: var(--font-size-xs);
          color: var(--text-color-secondary);
          margin-bottom: var(--spacing-xs);
        }

        .info-value {
          font-size: var(--font-size-md);
          font-weight: 600;
          color: var(--text-color-title);

          &.warning {
            color: var(--warning-color);
          }

          &.danger {
            color: var(--error-color);
          }
        }
      }
    }

    .passage-list {
      flex: 1;
      background-color: var(--bg-color);
      border-radius: var(--border-radius-base);
      box-shadow: var(--shadow-base);
      padding: var(--spacing-xl);
      overflow: hidden;

      .passage-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: var(--spacing-lg);

        .passage-title {
          font-size: var(--font-size-md);
          font-weight: 600;
          color: var(--text-color-title);
        }

        .passage-count {
          font-size: var(--font-size-sm);
          color: var(--text-color-secondary);
        }
      }

      .status-badge {
        padding: 2px 8px;
        border-radius: var(--border-radius-sm);
        font-size: var(--font-size-xs);

        &.status-normal {
          background-color: rgba(0, 180, 42, 0.1);
          color: var(--success-color);
        }

        &.status-noPlate {
          background-color: rgba(255, 125, 0, 0.1);
          color: var(--warning-color);
        }

        &.status-blacklist,
        &.status-unpaid {
          background-color: rgba(245, 63, 63, 0.1);
          color: var(--error-color);
        }
      }
    }
  }
}
</style>