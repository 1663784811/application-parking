<template>
  <div class="real-time-monitor-page">
    <!-- 顶部操作栏 -->
    <div class="monitor-toolbar">
      <div class="toolbar-left">
        <h3 class="page-title">实时监控</h3>
        <span class="page-subtitle">今日通行 {{ state.passageCount }} 次</span>
      </div>
      <div class="toolbar-right">
        <Select v-model="state.channelFilter" placeholder="全部通道" class="channel-select" clearable>
          <Option v-for="item in state.channelList" :key="item.id" :value="item.id">
            {{ item.name }}
          </Option>
        </Select>
        <Button type="primary" @click="handleOpenGate">
          <Icon type="ios-lock-open"/>
          远程开闸
        </Button>
        <Button @click="handleRefresh">
          <Icon type="ios-refresh"/>
          刷新
        </Button>
      </div>
    </div>

    <div class="monitor-body">
      <!-- 视频监控区域 -->
      <div class="video-section">
        <div class="section-header">
          <span class="section-title">视频监控</span>
          <span class="section-badge">4 个通道在线</span>
        </div>
        <div class="video-grid">
          <div v-for="item in state.videoList" :key="item.id" class="video-card">
            <div class="video-placeholder">
              <Icon type="ios-videocam" class="video-icon"/>
            </div>
            <div class="video-footer">
              <span class="video-name">{{ item.name }}</span>
              <span class="video-status" :class="'status-' + item.status">
                {{ item.status === 'online' ? '在线' : '离线' }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 通行记录表格 -->
      <div class="passage-section">
        <div class="section-header">
          <span class="section-title">实时通行记录</span>
          <span class="section-badge">最近 20 条</span>
        </div>
        <Table
          :columns="passageColumns"
          :data="state.passageList"
          :height="320"
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
            <Button type="text" size="small" @click="handleViewDetail(row)">详情</Button>
          </template>
        </Table>
      </div>
    </div>
  </div>
</template>

<script setup>
import {reactive} from 'vue'
import {
  Icon,
  Button,
  Select,
  Option,
  Table,
  Tag,
  Message
} from 'view-ui-plus'

const state = reactive({
  channelFilter: null,
  passageCount: 1654,

  channelList: [
    {id: 1, name: '1号入口'},
    {id: 2, name: '2号出口'},
    {id: 3, name: '3号入口'},
    {id: 4, name: '地下入口'}
  ],

  videoList: [
    {id: 1, name: '1号入口', status: 'online'},
    {id: 2, name: '2号出口', status: 'online'},
    {id: 3, name: '3号入口', status: 'online'},
    {id: 4, name: '地下入口', status: 'offline'}
  ],

  passageList: [
    {id: 1, plate: '京A12345', type: 'in', channel: '1号入口', time: '2024-01-15 14:32:15', status: 'normal'},
    {id: 2, plate: '京B67890', type: 'out', channel: '2号出口', time: '2024-01-15 14:30:00', status: 'normal'},
    {id: 3, plate: '无牌车', type: 'in', channel: '3号入口', time: '2024-01-15 14:28:00', status: 'noPlate'},
    {id: 4, plate: '浙C11111', type: 'in', channel: '1号入口', time: '2024-01-15 14:25:00', status: 'normal'},
    {id: 5, plate: '苏D55555', type: 'out', channel: '2号出口', time: '2024-01-15 14:22:00', status: 'unpaid'},
    {id: 6, plate: '粤E66666', type: 'in', channel: '地下入口', time: '2024-01-15 14:20:00', status: 'normal'},
    {id: 7, plate: '黑名单', type: 'out', channel: '2号出口', time: '2024-01-15 14:18:00', status: 'blacklist'},
    {id: 8, plate: '京F77777', type: 'in', channel: '1号入口', time: '2024-01-15 14:15:00', status: 'normal'}
  ]
})

const passageColumns = [
  {title: '车牌号', key: 'plate', minWidth: 120},
  {title: '通行类型', slot: 'type', minWidth: 80},
  {title: '通道', key: 'channel', minWidth: 100},
  {title: '通行时间', key: 'time', minWidth: 160},
  {title: '状态', slot: 'status', minWidth: 100},
  {title: '操作', slot: 'action', minWidth: 80}
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

const handleOpenGate = () => {
  Message.success('开闸命令已发送')
}

const handleRefresh = () => {
  Message.success('已刷新')
}

const handleViewDetail = (row) => {
  console.log('查看通行详情', row)
}
</script>

<style lang="less" scoped>
.real-time-monitor-page {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: calc(100vh - var(--header-height) - 32px);

  .monitor-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: var(--spacing-lg) var(--spacing-xl);
    background-color: var(--bg-color);
    border-radius: var(--border-radius-base);
    box-shadow: var(--shadow-base);
    margin-bottom: var(--spacing-lg);

    .toolbar-left {
      display: flex;
    align-items: baseline;
    gap: var(--spacing-md);

      .page-title {
        font-size: var(--font-size-lg);
        font-weight: 600;
        color: var(--text-color-title);
      }

      .page-subtitle {
        font-size: var(--font-size-sm);
        color: var(--text-color-secondary);
      }
    }

    .toolbar-right {
      display: flex;
      align-items: center;
      gap: var(--spacing-md);

      .channel-select {
        width: 150px;
      }

      .ivu-btn {
        display: inline-flex;
        align-items: center;

        .ivu-icon {
          margin-right: 4px;
        }
      }
    }
  }

  .monitor-body {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: var(--spacing-lg);

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: var(--spacing-lg);

      .section-title {
        font-size: var(--font-size-md);
        font-weight: 600;
        color: var(--text-color-title);
      }

      .section-badge {
        font-size: var(--font-size-xs);
        color: var(--text-color-secondary);
        background-color: var(--bg-color-page);
        padding: 2px 10px;
        border-radius: var(--border-radius-sm);
      }
    }

    .video-section {
      background-color: var(--bg-color);
      border-radius: var(--border-radius-base);
      box-shadow: var(--shadow-base);
      padding: var(--spacing-xl);

      .video-grid {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: var(--spacing-md);

        .video-card {
          border-radius: var(--border-radius-sm);
          overflow: hidden;
          border: 1px solid var(--border-color);

          .video-placeholder {
            height: 160px;
            background-color: #1a1a1a;
            display: flex;
            align-items: center;
            justify-content: center;

            .video-icon {
              font-size: 36px;
              color: #444;
            }
          }

          .video-footer {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: var(--spacing-sm) var(--spacing-md);
            background-color: var(--bg-color-page);

            .video-name {
              font-size: var(--font-size-sm);
              color: var(--text-color-title);
            }

            .video-status {
              font-size: var(--font-size-xs);
              padding: 1px 6px;
              border-radius: var(--border-radius-sm);

              &.status-online {
                color: var(--success-color);
                background-color: rgba(0, 180, 42, 0.1);
              }

              &.status-offline {
                color: var(--error-color);
                background-color: rgba(245, 63, 63, 0.1);
              }
            }
          }
        }
      }
    }

    .passage-section {
      flex: 1;
      background-color: var(--bg-color);
      border-radius: var(--border-radius-base);
      box-shadow: var(--shadow-base);
      padding: var(--spacing-xl);

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