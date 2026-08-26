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

      <!-- 通行记录表格 -->
      <div class="passage-section">
        <div class="section-header">
          <span class="section-title">实时通行记录</span>
          <span class="section-badge">最近 20 条</span>
        </div>
        <TableColumnSetting :columns="passageColumns" v-model:visible="visibleFields" v-model:open="colSettingVisible" @reset="resetColumns" />
        <Table
          :columns="displayColumns"
          :data="state.passageList"
          :loading="state.loading"
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

    <!-- 通行详情 -->
    <Modal v-model="state.detailModal" title="通行详情" width="480">
      <div class="detail-list" v-if="state.detailData.id">
        <div class="detail-row"><span class="detail-label">车牌号</span><span>{{ state.detailData.carNumber || '无牌车' }}</span></div>
        <div class="detail-row"><span class="detail-label">车辆类型</span><span>{{ state.detailData.carType || '—' }}</span></div>
        <div class="detail-row"><span class="detail-label">入场时间</span><span>{{ state.detailData.entryTime || '—' }}</span></div>
        <div class="detail-row"><span class="detail-label">出场时间</span><span>{{ state.detailData.outTime || '—' }}</span></div>
        <div class="detail-row"><span class="detail-label">状态</span><span>{{ state.detailData.status === 1 ? '已出场' : '场内' }}</span></div>
        <div class="detail-row"><span class="detail-label">停车场ID</span><span>{{ state.detailData.parkingId || '—' }}</span></div>
      </div>
      <template #footer><Button @click="state.detailModal = false">关闭</Button></template>
    </Modal>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted } from 'vue'
import {
  Icon,
  Button,
  Select,
  Option,
  Table,
  Tag,
  Modal,
  Message
} from 'view-ui-plus'
import { passageApi, channelApi } from '@/api'
import TableColumnSetting from '@/components/TableColumnSetting.vue'
import { useTableColumns } from '@/composables/useTableColumns'

const state = reactive({
  channelFilter: null,
  passageCount: 0,
  loading: false,
  channelList: [],
  passageList: [],
  detailModal: false,
  detailData: {}
})

// 视频卡片由通道数据生成（视频流需对接摄像头设备，状态暂以在线占位）
const videoList = computed(() => {
  const list = state.channelFilter
    ? state.channelList.filter(c => c.id === state.channelFilter)
    : state.channelList
  return list.map(c => ({ id: c.id, name: c.name, status: 'online' }))
})

const passageColumns = [
  { field: 'plate', title: '车牌号', key: 'plate', minWidth: 120 },
  { field: 'type', title: '通行类型', slot: 'type', minWidth: 80 },
  { field: 'channel', title: '通道', key: 'channel', minWidth: 100 },
  { field: 'time', title: '通行时间', key: 'time', minWidth: 160 },
  { field: 'status', title: '状态', slot: 'status', minWidth: 100 },
  { title: '操作', slot: 'action', minWidth: 80 }
]

const { visibleFields, colSettingVisible, displayColumns, resetColumns } = useTableColumns(passageColumns, 'realTimeMonitor:passage:columnVisible')

const getStatusText = (status) => {
  const map = {
    normal: '正常',
    noPlate: '无牌',
    blacklist: '黑名单',
    unpaid: '欠费'
  }
  return map[status] || status
}

// PkCarLog → 通行记录行：outTime 有值视为出场，否则进场；无车牌视为无牌
const mapCarLog = (log) => ({
  id: log.id,
  plate: log.carNumber || '无牌车',
  type: log.outTime ? 'out' : 'in',
  channel: '—',
  time: log.outTime || log.entryTime || '',
  status: log.carNumber ? 'normal' : 'noPlate'
})

const loadData = async () => {
  state.loading = true
  try {
    const [countRes, channelRes, passageRes] = await Promise.all([
      passageApi.getTodayCount(),
      channelApi.getChannelList({ size: 1000 }),
      passageApi.getRecordList({ size: 20 })
    ])
    state.passageCount = countRes.data || 0
    state.channelList = channelRes.data || []
    state.passageList = (passageRes.data || []).map(mapCarLog)
  } catch (e) {
    console.error('加载实时监控数据失败', e)
  } finally {
    state.loading = false
  }
}

// 远程开闸需对接道闸设备控制接口
const handleOpenGate = () => {
  Message.info('远程开闸需对接道闸设备控制接口')
}

const handleRefresh = () => {
  loadData()
}

const handleViewDetail = async (row) => {
  try {
    const res = await passageApi.getRecordDetail(row.id)
    state.detailData = res.data || {}
    state.detailModal = true
  } catch (e) {
    console.error('查询通行详情失败', e)
  }
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

  .detail-list {
    .detail-row {
      display: flex;
      justify-content: space-between;
      padding: var(--spacing-sm) 0;
      border-bottom: 1px solid var(--border-color);
      font-size: var(--font-size-sm);

      .detail-label {
        color: var(--text-color-secondary);
      }
    }
  }
}
</style>
