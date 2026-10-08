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
          <span class="section-badge">{{ videoList.length }} 个通道</span>
        </div>
        <p class="section-tip">视频流需对接摄像头设备，下方为通道占位卡片</p>
        <div class="video-grid">
          <div v-for="item in videoList" :key="item.id" class="video-card">
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
          <div v-if="!videoList.length" class="video-empty">暂无通道数据</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted } from 'vue'
import {
  Icon,
  Button,
  Select,
  Option,
  Message
} from 'view-ui-plus'
import { passageApi, channelApi } from '@/api'

const state = reactive({
  channelFilter: null,
  passageCount: 0,
  channelList: []
})

// 视频卡片由通道数据生成（视频流需对接摄像头设备，状态暂以在线占位）
const videoList = computed(() => {
  const list = state.channelFilter
    ? state.channelList.filter(c => c.id === state.channelFilter)
    : state.channelList
  return list.map(c => ({ id: c.id, name: c.name, status: 'online' }))
})

const loadData = async () => {
  try {
    const [countRes, channelRes] = await Promise.all([
      passageApi.getTodayCount(),
      channelApi.getChannelList({ size: 1000 })
    ])
    state.passageCount = countRes.data || 0
    state.channelList = channelRes.data || []
  } catch (e) {
    console.error('加载实时监控数据失败', e)
  }
}

// 远程开闸需对接道闸设备控制接口
const handleOpenGate = () => {
  Message.info('远程开闸需对接道闸设备控制接口')
}

const handleRefresh = () => {
  loadData()
}

onMounted(() => {
  loadData()
})
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
      flex: 1;
      background-color: var(--bg-color);
      border-radius: var(--border-radius-base);
      box-shadow: var(--shadow-base);
      padding: var(--spacing-xl);

      .section-tip {
        font-size: var(--font-size-xs);
        color: var(--text-color-secondary);
        margin-bottom: var(--spacing-lg);
      }

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

        .video-empty {
          grid-column: 1 / -1;
          text-align: center;
          padding: var(--spacing-xl);
          color: var(--text-color-secondary);
          font-size: var(--font-size-sm);
        }
      }
    }
  }
}
</style>
