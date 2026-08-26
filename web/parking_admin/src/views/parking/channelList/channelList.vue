<template>
  <div class="channel-list-page">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <div class="filter-item">
          <Select v-model="state.searchForm.type" placeholder="通道类型" class="filter-select" clearable>
            <Option value="in">入口</Option>
            <Option value="out">出口</Option>
            <Option value="inout">出入口</Option>
          </Select>
        </div>
        <div class="filter-item">
          <Select v-model="state.searchForm.status" placeholder="状态" class="filter-select" clearable>
            <Option :value="1">正常</Option>
            <Option :value="0">故障</Option>
          </Select>
        </div>
        <div class="filter-item">
          <Input
            v-model="state.searchForm.keyword"
            placeholder="搜索通道名称"
            class="filter-input"
            clearable
            @on-enter="handleSearch"
          >
            <template #prefix>
              <Icon type="ios-search"/>
            </template>
          </Input>
        </div>
        <div class="filter-item">
          <Button type="primary" @click="handleSearch">查询</Button>
        </div>
        <div class="filter-item">
          <Button @click="handleReset">重置</Button>
        </div>
        <div class="filter-item">
          <Button type="primary" icon="ios-add" @click="handleAdd">新增通道</Button>
        </div>
      </div>
    </div>

    <!-- 表格 -->
    <div class="table-container">
      <TableColumnSetting :columns="columns" v-model:visible="visibleFields" v-model:open="colSettingVisible" @reset="resetColumns" />
      <Table
        :columns="displayColumns"
        :data="state.tableData"
        :loading="state.loading"
      >
        <template #type="{ row }">
          <Tag :color="typeColor(row.type)">{{ typeText(row.type) }}</Tag>
        </template>
        <template #status="{ row }">
          <span class="status-tag" :class="'status-' + (row.status === 1 ? 'normal' : 'fault')">
            {{ row.status === 1 ? '正常' : '故障' }}
          </span>
        </template>
        <template #action="{ row }">
          <Button type="text" size="small" @click="handleShowQr(row)">二维码</Button>
          <Button type="text" size="small" @click="handleEdit(row)">编辑</Button>
          <Button type="text" size="small" @click="handleDelete(row)">删除</Button>
        </template>
      </Table>

      <div class="pagination-wrapper">
        <Page
          :total="state.pagination.total"
          :current="state.pagination.current"
          :page-size="state.pagination.pageSize"
          show-total
          show-elevator
          @on-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <Modal
      v-model="state.modalVisible"
      :title="state.isEdit ? '编辑通道' : '新增通道'"
      width="500"
    >
      <Form :model="state.form" :label-width="100">
        <FormItem label="通道名称" prop="name">
          <Input v-model="state.form.name" placeholder="请输入通道名称"/>
        </FormItem>
        <FormItem label="通道编号" prop="code">
          <Input v-model="state.form.code" placeholder="请输入通道编号"/>
        </FormItem>
        <FormItem label="IP地址" prop="ip">
          <Input v-model="state.form.ip" placeholder="请输入设备IP地址"/>
        </FormItem>
        <FormItem label="通道类型" prop="type">
          <Select v-model="state.form.type" placeholder="请选择通道类型">
            <Option value="in">入口</Option>
            <Option value="out">出口</Option>
            <Option value="inout">出入口</Option>
          </Select>
        </FormItem>
        <FormItem label="所属停车场" prop="parkingId">
          <Select v-model="state.form.parkingId" placeholder="请选择停车场" filterable>
            <Option v-for="item in state.parkingList" :key="item.id" :value="item.id">
              {{ item.name }}
            </Option>
          </Select>
        </FormItem>
        <FormItem label="状态" prop="status">
          <i-switch v-model="state.form.status" :true-value="1" :false-value="0">
            <span slot="open">正常</span>
            <span slot="close">故障</span>
          </i-switch>
        </FormItem>
      </Form>
      <template #footer>
        <Button @click="state.modalVisible = false">取消</Button>
        <Button type="primary" @click="handleSubmit">确定</Button>
      </template>
    </Modal>

    <!-- 通道二维码弹窗 -->
    <Modal v-model="state.qrModalVisible" title="通道二维码" width="420">
      <div class="qr-modal-body">
        <div class="qr-info">
          <div class="qr-info-row"><span class="qr-label">通道名称</span><span class="qr-value">{{ state.qrChannel?.name }}</span></div>
          <div class="qr-info-row"><span class="qr-label">通道编号</span><span class="qr-value">{{ state.qrChannel?.code }}</span></div>
        </div>
        <div class="qr-canvas-wrap"><canvas ref="qrCanvasRef"></canvas></div>
        <div class="qr-url-wrap">
          <Input v-model="state.qrUrl" readonly class="qr-url-input"/>
          <Button type="primary" size="small" @click="handleCopyUrl">复制链接</Button>
        </div>
      </div>
      <template #footer>
        <Button @click="state.qrModalVisible = false">关闭</Button>
        <Button type="primary" @click="handleDownloadQr">下载二维码</Button>
      </template>
    </Modal>
  </div>
</template>

<script setup>
import {reactive, ref, nextTick, watch, onMounted} from 'vue'
import QRCode from 'qrcode'
import {
  Button,
  Form,
  FormItem,
  Icon,
  Input,
  Message,
  Modal,
  Option,
  Page,
  Select,
  Switch,
  Table,
  Tag
} from 'view-ui-plus'
import {useCommonStore} from '@/stores/common.js'
import {parkingApi, channelApi} from '@/api'
import TableColumnSetting from '@/components/TableColumnSetting.vue'
import { useTableColumns } from '@/composables/useTableColumns'

const commonStore = useCommonStore()
const qrCanvasRef = ref(null)

const state = reactive({
  searchForm: {
    type: null,
    status: null,
    keyword: ''
  },

  loading: false,
  tableData: [],

  parkingList: commonStore.state.parkingList.length > 0
    ? commonStore.state.parkingList
    : [],

  pagination: {
    total: 0,
    current: 1,
    pageSize: 10
  },

  modalVisible: false,
  isEdit: false,
  form: {
    name: '',
    code: '',
    ip: '',
    type: 'in',
    parkingId: null,
    status: 1
  },

  // 通道二维码弹窗
  qrModalVisible: false,
  qrChannel: null,
  qrUrl: ''
})

const columns = [
  {
    field: 'code',
    title: '通道编号',
    key: 'code',
    minWidth: 120
  },
  {
    field: 'name',
    title: '通道名称',
    key: 'name',
    minWidth: 160
  },
  {
    field: 'type',
    title: '通道类型',
    slot: 'type',
    minWidth: 100
  },
  {
    field: 'parkingName',
    title: '所属停车场',
    key: 'parkingName',
    minWidth: 160
  },
  {
    field: 'ip',
    title: 'IP地址',
    key: 'ip',
    minWidth: 140
  },
  {
    field: 'status',
    title: '状态',
    slot: 'status',
    minWidth: 100
  },
  {
    title: '操作',
    slot: 'action',
    minWidth: 200
  }
]

const { visibleFields, colSettingVisible, displayColumns, resetColumns } = useTableColumns(columns, 'channelList:columnVisible')

const typeColor = (type) => {
  const map = {in: 'blue', out: 'green', inout: 'orange'}
  return map[type] || 'default'
}

const typeText = (type) => {
  const map = {in: '入口', out: '出口', inout: '出入口'}
  return map[type] || '-'
}

// 停车场ID → 名称（后端实体只存 parkingId，名称由前端按停车场列表映射）
const parkingName = (parkingId) => {
  if (!parkingId) return '-'
  const pk = state.parkingList.find(item => String(item.id) === String(parkingId))
  return pk ? pk.name : '-'
}

// PkChannel 字段 → 页面字段映射（补 parkingName 用于表格展示）
const mapRow = (r) => ({
  ...r,
  parkingName: parkingName(r.parkingId)
})

// H5 出场扫码页地址：优先读环境变量 VITE_H5_BASE_URL，留空回退到当前站点 origin
const H5_BASE_URL = import.meta.env.VITE_H5_BASE_URL || window.location.origin

// 解析通道所属应用ID：优先通道自身 appId，其次取所属停车场的 appId
const resolveAppId = (channel) => {
  if (channel.appId) return channel.appId
  const pk = state.parkingList.find(item => String(item.id) === String(channel.parkingId))
  return pk ? pk.appId : null
}

// 构造通道出场扫码 H5 链接（扫码后进入对应通道的出场流程）
const buildChannelQrUrl = (channel) => {
  const appId = resolveAppId(channel)
  const code = channel.code ? encodeURIComponent(channel.code) : ''
  return `${H5_BASE_URL}/#/app/${appId}/scanExit?channelId=${channel.id}&code=${code}`
}

// 打开二维码弹窗（appId 缺失时阻断，避免生成无效链接）
const handleShowQr = (row) => {
  if (!resolveAppId(row)) {
    Message.warning('该通道未绑定应用ID(appId)，无法生成完整 H5 出场链接，请先在所属停车场中配置 appId 后重试')
    return
  }
  state.qrChannel = row
  state.qrUrl = buildChannelQrUrl(row)
  state.qrModalVisible = true
  nextTick(() => renderQr())
}

// 渲染二维码到 canvas
const renderQr = () => {
  const canvas = qrCanvasRef.value
  if (!canvas || !state.qrUrl) return
  QRCode.toCanvas(canvas, state.qrUrl, {width: 240, margin: 2}, (err) => {
    if (err) console.error('二维码渲染失败', err)
  })
}

// 下载二维码 PNG
const handleDownloadQr = async () => {
  if (!state.qrUrl) return
  try {
    const dataUrl = await QRCode.toDataURL(state.qrUrl, {width: 480, margin: 2})
    const a = document.createElement('a')
    a.href = dataUrl
    a.download = `通道二维码_${state.qrChannel?.code || state.qrChannel?.id || ''}.png`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
  } catch (e) {
    console.error('二维码下载失败', e)
    Message.error('二维码下载失败')
  }
}

// 复制链接
const handleCopyUrl = async () => {
  if (!state.qrUrl) return
  try {
    await navigator.clipboard.writeText(state.qrUrl)
    Message.success('链接已复制')
  } catch (e) {
    Message.info('复制失败，请手动选中链接复制')
  }
}

// 弹窗显隐时重渲二维码（兜底，确保 canvas 已挂载）
watch(() => state.qrModalVisible, (v) => {
  if (v) nextTick(renderQr)
})

// 加载停车场列表（供弹窗选择 + 名称映射）
const loadParkingList = async () => {
  try {
    const res = await parkingApi.getParkingList({size: 1000})
    state.parkingList = res.data || []
  } catch (e) {
    console.error('获取停车场列表失败', e)
  }
}

// 通道列表（分页，type + status + keyword）
const loadData = async () => {
  state.loading = true
  try {
    const {type, status, keyword} = state.searchForm
    const params = {
      page: state.pagination.current,
      size: state.pagination.pageSize,
      type: type || null,
      keyword: keyword ? keyword.trim() : null
    }
    // status 为 0（故障）时也需发送，仅 null/未选时不发
    if (status !== null && status !== '' && status !== undefined) {
      params.status = status
    }
    const res = await channelApi.getChannelList(params)
    state.tableData = (res.data || []).map(mapRow)
    state.pagination.total = (res.result && res.result.total) || 0
  } catch (e) {
    console.error('获取通道列表失败', e)
  } finally {
    state.loading = false
  }
}

const handleSearch = () => {
  state.pagination.current = 1
  loadData()
}

const handleReset = () => {
  state.searchForm = {
    type: null,
    status: null,
    keyword: ''
  }
  handleSearch()
}

const handleAdd = () => {
  state.isEdit = false
  state.form = {
    name: '',
    code: '',
    ip: '',
    type: 'in',
    parkingId: null,
    status: 1
  }
  state.modalVisible = true
}

const handleEdit = (row) => {
  state.isEdit = true
  state.form = {
    id: row.id,
    name: row.name,
    code: row.code,
    ip: row.ip,
    type: row.type,
    parkingId: row.parkingId,
    status: row.status
  }
  state.modalVisible = true
}

const handleSubmit = async () => {
  if (!state.form.name) {
    Message.warning('请输入通道名称')
    return
  }
  try {
    if (state.isEdit) {
      await channelApi.editChannel({...state.form})
    } else {
      await channelApi.addChannel({...state.form})
    }
    Message.success(state.isEdit ? '编辑成功' : '新增成功')
    state.modalVisible = false
    loadData()
  } catch (e) {
    console.error('保存通道失败', e)
  }
}

const handleDelete = (row) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除通道"${row.name}"吗？`,
    onOk: async () => {
      try {
        await channelApi.deleteChannel(row.id)
        Message.success('删除成功')
        loadData()
      } catch (e) {
        console.error('删除通道失败', e)
      }
    }
  })
}

const handlePageChange = (page) => {
  state.pagination.current = page
  loadData()
}

onMounted(() => {
  loadParkingList()
  loadData()
})
</script>

<style lang="less" scoped>
.channel-list-page {
  flex: 1;
  display: flex;
  flex-direction: column;

  .filter-bar {
    padding: var(--spacing-xl);
    background-color: var(--bg-color);
    border-radius: var(--border-radius-base);
    margin-bottom: var(--spacing-lg);
    box-shadow: var(--shadow-base);

    .filter-row {
      display: flex;
      flex-wrap: wrap;
      gap: var(--spacing-md);
      margin-bottom: var(--spacing-lg);

      .filter-item {
        flex-shrink: 0;
      }

      .filter-select {
        width: 150px;
      }

      .filter-input {
        width: 200px;
      }
    }
  }

  .table-container {
    flex: 1;
    background-color: var(--bg-color);
    border-radius: var(--border-radius-base);
    padding: var(--spacing-xl);
    box-shadow: var(--shadow-base);

    .status-tag {
      padding: 2px 8px;
      border-radius: var(--border-radius-sm);
      font-size: var(--font-size-xs);
    }

    .status-normal {
      background-color: rgba(0, 180, 42, 0.1);
      color: var(--success-color);
    }

    .status-fault {
      background-color: rgba(245, 63, 63, 0.1);
      color: var(--error-color);
    }

    .pagination-wrapper {
      display: flex;
      justify-content: flex-end;
      margin-top: var(--spacing-xl);
    }
  }
}

.qr-modal-body {
  .qr-info {
    margin-bottom: var(--spacing-lg);
    .qr-info-row {
      display: flex;
      margin-bottom: var(--spacing-sm);
      .qr-label {
        width: 70px;
        color: var(--text-color-secondary);
        font-size: var(--font-size-sm);
      }
      .qr-value {
        flex: 1;
        color: var(--text-color-title);
        font-weight: 500;
      }
    }
  }
  .qr-canvas-wrap {
    display: flex;
    justify-content: center;
    padding: var(--spacing-md) 0 var(--spacing-lg);
    canvas {
      width: 240px;
      height: 240px;
    }
  }
  .qr-url-wrap {
    display: flex;
    gap: var(--spacing-sm);
    align-items: center;
    .qr-url-input {
      flex: 1;
    }
  }
}
</style>
