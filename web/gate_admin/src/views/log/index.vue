<template>
  <div class="log-page">
    <!-- 筛选区 -->
    <div class="filter-bar">
      <div class="filter-row">
        <Select v-model="filterForm.operator" placeholder="操作人" style="width: 120px" clearable>
          <Option value="张三">张三</Option>
          <Option value="李四">李四</Option>
          <Option value="王五">王五</Option>
        </Select>
        <Select v-model="filterForm.type" placeholder="操作类型" style="width: 140px" clearable>
          <Option value="openGate">开闸放行</Option>
          <Option value="tempPermission">临时权限开通</Option>
          <Option value="exceptionHandle">异常处理</Option>
          <Option value="deviceOperation">设备操作</Option>
          <Option value="systemSetting">系统设置</Option>
        </Select>
        <Select v-model="filterForm.result" placeholder="操作结果" style="width: 100px" clearable>
          <Option value="success">成功</Option>
          <Option value="fail">失败</Option>
        </Select>
        <DatePicker
          v-model="filterForm.dateRange"
          type="daterange"
          placeholder="时间范围"
          style="width: 260px"
        />
        <Button type="primary" @click="handleSearch">
          <Icon type="ios-search" />
          查询
        </Button>
        <Button @click="handleReset">
          <Icon type="ios-refresh" />
          重置
        </Button>
      </div>
    </div>

    <!-- 操作日志列表 -->
    <div class="table-section">
      <Table :columns="columns" :data="tableData" :loading="loading" :height="450">
        <template #type="{ row }">
          <Tag :color="getTypeTagColor(row.type)">
            {{ getTypeText(row.type) }}
          </Tag>
        </template>
        <template #result="{ row }">
          <Tag :color="row.result === 'success' ? 'success' : 'error'">
            {{ row.result === 'success' ? '成功' : '失败' }}
          </Tag>
        </template>
        <template #action="{ row }">
          <Button type="text" size="small" @click="handleViewDetail(row)">
            详情
          </Button>
        </template>
      </Table>

      <div class="pagination-wrapper">
        <Page :total="pagination.total" :current="pagination.current" :page-size="pagination.pageSize" show-total />
      </div>
    </div>

    <!-- 日志导出与清理 -->
    <div class="footer-actions">
      <div class="export-section">
        <DatePicker
          v-model="exportForm.dateRange"
          type="daterange"
          placeholder="选择导出时间范围"
          style="width: 260px"
        />
        <Button @click="handleExport('excel')">
          <Icon type="ios-download-outline" />
          导出Excel
        </Button>
        <Button @click="handleExport('pdf')">
          <Icon type="ios-document-outline" />
          导出PDF
        </Button>
      </div>

      <div class="cleanup-section" v-if="userStore.isAdmin">
        <span class="cleanup-hint">日志保留期限：30天</span>
        <Button type="error" ghost @click="handleCleanup">
          <Icon type="ios-trash-outline" />
          清理过期日志
        </Button>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <Modal v-model="detailModal.visible" title="日志详情" width="600">
      <div class="detail-content" v-if="detailModal.data">
        <Row :gutter="16">
          <Col span="12">
            <div class="detail-item">
              <label>日志编号</label>
              <span>{{ detailModal.data.id }}</span>
            </div>
          </Col>
          <Col span="12">
            <div class="detail-item">
              <label>操作时间</label>
              <span>{{ detailModal.data.time }}</span>
            </div>
          </Col>
          <Col span="12">
            <div class="detail-item">
              <label>操作人</label>
              <span>{{ detailModal.data.operator }}</span>
            </div>
          </Col>
          <Col span="12">
            <div class="detail-item">
              <label>操作类型</label>
              <Tag :color="getTypeTagColor(detailModal.data.type)" size="small">
                {{ getTypeText(detailModal.data.type) }}
              </Tag>
            </div>
          </Col>
          <Col span="12">
            <div class="detail-item">
              <label>IP地址</label>
              <span>{{ detailModal.data.ip }}</span>
            </div>
          </Col>
          <Col span="12">
            <div class="detail-item">
              <label>操作结果</label>
              <Tag :color="detailModal.data.result === 'success' ? 'success' : 'error'">
                {{ detailModal.data.result === 'success' ? '成功' : '失败' }}
              </Tag>
            </div>
          </Col>
        </Row>

        <div class="detail-content-box">
          <label>操作内容</label>
          <div class="content-text">{{ detailModal.data.content }}</div>
        </div>

        <div class="detail-content-box" v-if="detailModal.data.remark">
          <label>备注</label>
          <div class="content-text">{{ detailModal.data.remark }}</div>
        </div>
      </div>
    </Modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Button, Icon, Select, Option, DatePicker, Table, Tag, Page, Modal, Row, Col, Message } from 'view-ui-plus'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 筛选表单
const filterForm = reactive({
  operator: '',
  type: '',
  result: '',
  dateRange: []
})

// 导出表单
const exportForm = reactive({
  dateRange: []
})

// 状态
const loading = ref(false)
const pagination = reactive({
  total: 0,
  current: 1,
  pageSize: 10
})

// 表格列
const columns = [
  { title: '日志编号', key: 'id', minWidth: 120 },
  { title: '操作人', key: 'operator', minWidth: 100 },
  { title: '操作类型', slot: 'type', minWidth: 120 },
  { title: '操作内容', key: 'content', minWidth: 250, tooltip: true },
  { title: '操作时间', key: 'time', minWidth: 160 },
  { title: 'IP地址', key: 'ip', minWidth: 120 },
  { title: '结果', slot: 'result', minWidth: 80 },
  { title: '操作', slot: 'action', minWidth: 80, fixed: 'right' }
]

// 表格数据
const tableData = ref([])

// 详情弹窗
const detailModal = reactive({
  visible: false,
  data: null
})

// 方法
const getTypeTagColor = (type) => {
  const map = {
    openGate: 'blue',
    tempPermission: 'purple',
    exceptionHandle: 'orange',
    deviceOperation: 'cyan',
    systemSetting: 'default'
  }
  return map[type] || 'default'
}

const getTypeText = (type) => {
  const map = {
    openGate: '开闸放行',
    tempPermission: '临时权限',
    exceptionHandle: '异常处理',
    deviceOperation: '设备操作',
    systemSetting: '系统设置'
  }
  return map[type] || type
}

const handleSearch = () => {
  pagination.current = 1
  initData()
}

const handleReset = () => {
  filterForm.operator = ''
  filterForm.type = ''
  filterForm.result = ''
  filterForm.dateRange = []
  handleSearch()
}

const handleViewDetail = (row) => {
  detailModal.data = row
  detailModal.visible = true
}

const handleExport = (format) => {
  Message.success(`${format.toUpperCase()} 导出成功`)
}

const handleCleanup = () => {
  Modal.confirm({
    title: '确认清理',
    content: '确定要清理所有超过30天的日志吗？此操作不可恢复。',
    okText: '确认清理',
    okType: 'error',
    onOk: () => {
      Message.success('过期日志已清理')
    }
  })
}

// 初始化数据
const initData = () => {
  loading.value = true

  setTimeout(() => {
    tableData.value = [
      { id: 'LOG20240115001', operator: '张三', type: 'openGate', content: '远程开闸，通道：入口1', time: '2024-01-15 14:32:25', ip: '192.168.1.100', result: 'success' },
      { id: 'LOG20240115002', operator: '张三', type: 'tempPermission', content: '开通临时权限，编号：TMP12345678', time: '2024-01-15 14:25:00', ip: '192.168.1.100', result: 'success' },
      { id: 'LOG20240115003', operator: '李四', type: 'exceptionHandle', content: '欠费车辆处理，收取费用：¥20', time: '2024-01-15 14:20:30', ip: '192.168.1.101', result: 'success' },
      { id: 'LOG20240115004', operator: '李四', type: 'deviceOperation', content: '设备重启，道闸：出口2', time: '2024-01-15 14:15:00', ip: '192.168.1.101', result: 'success' },
      { id: 'LOG20240115005', operator: '张三', type: 'openGate', content: '远程开闸，通道：出口1', time: '2024-01-15 14:10:00', ip: '192.168.1.100', result: 'success' },
      { id: 'LOG20240115006', operator: '张三', type: 'exceptionHandle', content: '黑名单车辆临时放行', time: '2024-01-15 14:00:00', ip: '192.168.1.100', result: 'fail', remark: '操作失败：黑名单车辆不允许临时放行' },
      { id: 'LOG20240115007', operator: '王五', type: 'openGate', content: '远程开闸，通道：入口2', time: '2024-01-15 13:45:00', ip: '192.168.1.102', result: 'success' },
      { id: 'LOG20240115008', operator: '王五', type: 'systemSetting', content: '修改系统设置：刷新频率调整为5秒', time: '2024-01-15 13:30:00', ip: '192.168.1.102', result: 'success' }
    ]
    pagination.total = tableData.value.length
    loading.value = false
  }, 300)
}

// 生命周期
onMounted(() => {
  initData()
})
</script>

<style lang="less" scoped>
.log-page {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.filter-bar {
  background-color: var(--color-bg-card);
  border-radius: var(--border-radius-base);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-base);

  .filter-row {
    display: flex;
    gap: var(--spacing-md);
    flex-wrap: wrap;
  }
}

.table-section {
  background-color: var(--color-bg-card);
  border-radius: var(--border-radius-base);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-base);
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--spacing-md);
}

.footer-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--color-bg-card);
  border-radius: var(--border-radius-base);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-base);
}

.export-section {
  display: flex;
  gap: var(--spacing-md);

  .ivu-btn {
    display: inline-flex;
    align-items: center;
    gap: var(--spacing-xs);
  }
}

.cleanup-section {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);

  .cleanup-hint {
    font-size: var(--font-size-xs);
    color: var(--color-text-secondary);
  }
}

/* 详情弹窗 */
.detail-content {
  :deep(.ivu-col) {
    margin-bottom: var(--spacing-md);
  }

  .detail-item {
    label {
      display: block;
      font-size: var(--font-size-xs);
      color: var(--color-text-secondary);
      margin-bottom: var(--spacing-xs);
    }

    span {
      font-size: var(--font-size-sm);
      color: var(--color-title);
    }
  }

  .detail-content-box {
    margin-top: var(--spacing-md);

    label {
      display: block;
      font-size: var(--font-size-xs);
      color: var(--color-text-secondary);
      margin-bottom: var(--spacing-sm);
    }

    .content-text {
      font-size: var(--font-size-sm);
      color: var(--color-body);
      background-color: var(--color-bg);
      padding: var(--spacing-md);
      border-radius: var(--border-radius-base);
      line-height: 1.6;
    }
  }
}
</style>