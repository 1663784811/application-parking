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
          <Button type="text" size="small" @click="handleDevice(row)">设备</Button>
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
      width="1000"
    >
      <Form :model="state.form" :label-width="100" class="modal-form-2col">
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

    <!-- 通道设备绑定 Drawer -->
    <Drawer
      v-model="state.deviceDrawerVisible"
      :width="900"
      :title="`通道设备 · ${state.currentChannel?.name || ''}`"
    >
      <div class="device-drawer-body">
        <div class="sub-toolbar">
          <span class="bound-count">已绑设备（{{ state.boundDevices.length }}）</span>
          <Button type="primary" size="small" @click="openDevicePicker">添加设备</Button>
        </div>
        <Table
          :columns="deviceColumns"
          :data="state.boundDevices"
          :loading="state.deviceLoading"
        >
          <template #onlineStatus="{ row }">
            <Badge
              :status="row.onlineStatus === 1 ? 'success' : 'error'"
              :text="row.onlineStatus === 1 ? '在线' : '离线'"
            />
          </template>
          <template #devAction="{ row }">
            <Button type="text" size="small" class="text-danger" @click="handleUnbindDevice(row)">解绑</Button>
          </template>
        </Table>
        <div v-if="!state.boundDevices.length && !state.deviceLoading" class="empty-tip">
          该通道暂未绑定设备
        </div>
      </div>
    </Drawer>

    <!-- 添加设备：设备列表 Modal（数据源 /admin/device/list，分页+筛选） -->
    <Modal v-model="state.devicePickerVisible" title="添加设备" width="860" :footer-hide="true">
      <div class="picker-filter-bar">
        <Select v-model="state.pickerSearch.type" placeholder="设备类型" class="picker-filter-select" clearable>
          <Option value="camera">摄像头</Option>
          <Option value="gate">道闸</Option>
          <Option value="screen">显示屏</Option>
          <Option value="sensor">地感</Option>
        </Select>
        <Select v-model="state.pickerSearch.onlineStatus" placeholder="在线状态" class="picker-filter-select" clearable>
          <Option :value="1">在线</Option>
          <Option :value="0">离线</Option>
        </Select>
        <Input
          v-model="state.pickerSearch.keyword"
          placeholder="设备名称 / 编号"
          class="picker-filter-input"
          clearable
          @on-enter="handlePickerSearch"
        />
        <Button type="primary" size="small" @click="handlePickerSearch">查询</Button>
        <Button size="small" @click="handlePickerReset">重置</Button>
      </div>
      <Table
        :columns="pickerColumns"
        :data="state.pickerTableData"
        :loading="state.pickerLoading"
      >
        <template #type="{ row }">{{ deviceTypeText(row.type) }}</template>
        <template #onlineStatus="{ row }">
          <Badge
            :status="row.onlineStatus === 1 ? 'success' : 'error'"
            :text="row.onlineStatus === 1 ? '在线' : '离线'"
          />
        </template>
        <template #pickAction="{ row }">
          <span v-if="isChannelBound(row)" class="bound-mark">已绑定</span>
          <Button
            v-else
            type="primary"
            size="small"
            :loading="state.pickingDeviceId === row.id"
            @click="handlePickDevice(row)"
          >绑定</Button>
        </template>
      </Table>
      <div class="picker-pagination">
        <Page
          :total="state.pickerPagination.total"
          :current="state.pickerPagination.current"
          :page-size="state.pickerPagination.pageSize"
          show-total
          @on-change="handlePickerPageChange"
        />
      </div>
    </Modal>

    <!-- 出入口通道绑定摄像头：选入口/出口方向（摄像头与通道一对一，不可能是出入口） -->
    <Modal v-model="state.channelTypeModalVisible" title="选择摄像头方向" width="420" :footer-hide="true">
      <div class="channel-type-tip">
        当前通道为出入口，摄像头“{{ state.pendingDevice?.name }}”需选择方向（入口或出口）：
      </div>
      <Form :label-width="100">
        <FormItem label="方向" prop="channelType">
          <Select v-model="state.channelTypeValue" placeholder="请选择方向">
            <Option value="in">入口</Option>
            <Option value="out">出口</Option>
          </Select>
        </FormItem>
      </Form>
      <div class="channel-type-footer">
        <Button @click="state.channelTypeModalVisible = false">取消</Button>
        <Button
          type="primary"
          :loading="state.pickingDeviceId === state.pendingDevice?.id"
          @click="confirmChannelTypeBind"
        >确定绑定</Button>
      </div>
    </Modal>
  </div>
</template>

<script setup>
import {reactive, ref, nextTick, watch, onMounted} from 'vue'
import QRCode from 'qrcode'
import {
  Badge,
  Button,
  Drawer,
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
import {parkingApi, channelApi, parkingDeviceApi, deviceApi} from '@/api'
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
  qrUrl: '',

  // 通道设备绑定 Drawer
  deviceDrawerVisible: false,
  currentChannel: null,
  boundDevices: [],
  deviceLoading: false,

  // 添加设备 Modal（数据源：/admin/device/list，分页+筛选）
  devicePickerVisible: false,
  pickerSearch: { type: null, onlineStatus: null, keyword: '' },
  pickerPagination: { total: 0, current: 1, pageSize: 10 },
  pickerTableData: [],
  pickerLoading: false,
  pickingDeviceId: null,

  // 摄像头绑定：通道类型选择弹窗（道闸由服务端取通道类型，不走此弹窗）
  channelTypeModalVisible: false,
  pendingDevice: null,
  channelTypeValue: null
})

const columns = [
  {
    field: 'parkingName',
    title: '所属停车场',
    key: 'parkingName',
    minWidth: 160
  },
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
    field: 'deviceCount',
    title: '设备数',
    key: 'deviceCount',
    minWidth: 90,
    align: 'center'
  },
  {
    title: '操作',
    slot: 'action',
    minWidth: 230
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
// 指向重新设计的扫码出场页 scanExit；旧链接 parkingExit 仍可访问，已印出去的二维码不受影响
const buildChannelQrUrl = (channel) => {
  const appId = resolveAppId(channel)
  const code = channel.code ? encodeURIComponent(channel.code) : ''
  console.log(channel)
  return `${H5_BASE_URL}/#/app/${appId}/scanExit?channelId=${channel.id}&parkingId=${channel.parkingId}`
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
    const rows = (res.data || []).map(mapRow)
    state.pagination.total = (res.result && res.result.total) || 0
    // 批量取每通道已绑设备数（避免逐行请求）
    if (rows.length) {
      try {
        const countsRes = await parkingDeviceApi.countByChannel(rows.map(r => r.id))
        const cm = countsRes.data || {}
        rows.forEach(r => { r.deviceCount = cm[String(r.id)] || 0 })
      } catch (e) {
        console.error('获取通道设备数失败', e)
        rows.forEach(r => { r.deviceCount = 0 })
      }
    }
    state.tableData = rows
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

// ===== 通道设备绑定 =====
// Drawer 内已绑设备表格列
const deviceColumns = [
  { title: '设备名称', key: 'name', minWidth: 150 },
  { title: '设备编码', key: 'code', minWidth: 130 },
  { title: '在线状态', slot: 'onlineStatus', minWidth: 100, align: 'center' },
  { title: '操作', slot: 'devAction', minWidth: 90, align: 'center' }
]

// 打开设备绑定 Drawer
const handleDevice = async (row) => {
  state.currentChannel = row
  state.deviceDrawerVisible = true
  state.boundDevices = []
  state.deviceLoading = true
  try {
    // 仅加载本通道已绑设备；添加设备的候选列表在弹窗中按 /admin/device/list 取
    const bound = await parkingDeviceApi.getBoundDevices(row.id)
    state.boundDevices = bound.data || []
  } catch (e) {
    console.error('加载通道设备失败', e)
  } finally {
    state.deviceLoading = false
  }
}

// 把当前通道行的设备数同步为已绑设备数（绑定/解绑后局部刷新，免整表重载）
const syncRowDeviceCount = () => {
  const ch = state.currentChannel
  if (!ch) return
  const row = state.tableData.find(r => String(r.id) === String(ch.id))
  if (row) row.deviceCount = state.boundDevices.length
}

// 添加设备 Modal 的设备列表列
const pickerColumns = [
  { title: '设备名称', key: 'name', minWidth: 150 },
  { title: '设备编码', key: 'code', minWidth: 120 },
  { title: '类型', slot: 'type', minWidth: 90, align: 'center' },
  { title: 'IP地址', key: 'ip', minWidth: 130 },
  { title: '在线状态', slot: 'onlineStatus', minWidth: 100, align: 'center' },
  { title: '操作', slot: 'pickAction', minWidth: 90, align: 'center' }
]

const deviceTypeText = (type) => {
  const map = { camera: '摄像头', gate: '道闸', screen: '显示屏', sensor: '地感' }
  return map[type] || '-'
}

// 该设备是否已绑定到当前通道（已绑则禁用绑定按钮，显示"已绑定"）
const isChannelBound = (device) => {
  return state.boundDevices.some(d => String(d.id) === String(device.id))
}

// 加载设备列表（数据源 /admin/device/list，分页+筛选；
// parkingId 锁定为当前通道所属停车场，只列同停车场设备）
const loadPickerDevices = async () => {
  state.pickerLoading = true
  try {
    const { type, onlineStatus, keyword } = state.pickerSearch
    const res = await deviceApi.getDeviceList({
      page: state.pickerPagination.current,
      size: state.pickerPagination.pageSize,
      parkingId: state.currentChannel?.parkingId || null,
      type: type || null,
      onlineStatus: onlineStatus !== null && onlineStatus !== '' ? onlineStatus : null,
      keyword: keyword ? keyword.trim() : null
    })
    state.pickerTableData = res.data || []
    state.pickerPagination.total = (res.result && res.result.total) || 0
  } catch (e) {
    console.error('获取设备列表失败', e)
  } finally {
    state.pickerLoading = false
  }
}

const handlePickerSearch = () => {
  state.pickerPagination.current = 1
  loadPickerDevices()
}

const handlePickerReset = () => {
  state.pickerSearch = { type: null, onlineStatus: null, keyword: '' }
  handlePickerSearch()
}

const handlePickerPageChange = (p) => {
  state.pickerPagination.current = p
  loadPickerDevices()
}

// 打开添加设备 Modal
const openDevicePicker = () => {
  state.pickerSearch = { type: null, onlineStatus: null, keyword: '' }
  state.pickerPagination.current = 1
  state.pickerPagination.total = 0
  state.pickerTableData = []
  state.pickingDeviceId = null
  state.devicePickerVisible = true
  loadPickerDevices()
}

// 在设备列表中点"绑定"：
//   摄像头 + 出入口(inout)通道 → 选入口/出口方向（摄像头与通道一对一，不可能是出入口）；
//   摄像头 + 入口/出口通道     → 直接绑定（服务端按通道类型确定）；
//   其余类型(道闸等)           → 直接绑定（服务端取通道类型）。
const handlePickDevice = (device) => {
  if (device.type === 'camera' && state.currentChannel?.type === 'inout') {
    state.pendingDevice = device
    state.channelTypeValue = null
    state.channelTypeModalVisible = true
    return
  }
  doBind(device, null)
}

// 摄像头：确认方向后绑定
const confirmChannelTypeBind = async () => {
  if (!state.channelTypeValue) {
    Message.warning('请选择方向（入口或出口）')
    return
  }
  const ok = await doBind(state.pendingDevice, state.channelTypeValue)
  if (ok) {
    state.channelTypeModalVisible = false
  }
}

// 执行绑定：camera 传 channelType（用户选择）；gate/其余由服务端取通道类型
const doBind = async (device, channelType) => {
  const channelId = state.currentChannel?.id
  state.pickingDeviceId = device.id
  try {
    await parkingDeviceApi.bind(device.id, channelId, channelType)
    Message.success(`设备"${device.name}"绑定成功`)
    // 刷新本通道已绑设备：该行随之变为"已绑定"（picker 表格不重载，便于连续绑定多台）
    const bound = await parkingDeviceApi.getBoundDevices(channelId)
    state.boundDevices = bound.data || []
    syncRowDeviceCount()
    return true
  } catch (e) {
    console.error('绑定设备失败', e)
    return false
  } finally {
    state.pickingDeviceId = null
  }
}

// 解绑设备
const handleUnbindDevice = (device) => {
  const channelId = state.currentChannel?.id
  Modal.confirm({
    title: '确认解绑',
    content: `确定将设备"${device.name}"从该通道解绑吗？`,
    onOk: async () => {
      try {
        await parkingDeviceApi.unbind(device.id, channelId)
        Message.success('解绑成功')
        const bound = await parkingDeviceApi.getBoundDevices(channelId)
        state.boundDevices = bound.data || []
        syncRowDeviceCount()
      } catch (e) {
        console.error('解绑设备失败', e)
      }
    }
  })
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

.device-drawer-body {
  .sub-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--spacing-lg);

    .bound-count {
      font-size: var(--font-size-base);
      font-weight: 500;
      color: var(--text-color-title);
    }
  }
}

.picker-filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
  align-items: center;
  margin-bottom: var(--spacing-md);
  .picker-filter-select { width: 140px; }
  .picker-filter-input { width: 180px; }
}

.picker-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--spacing-md);
}

.bound-mark {
  color: var(--text-color-secondary);
  font-size: var(--font-size-sm);
}

.channel-type-tip {
  margin-bottom: var(--spacing-md);
  color: var(--text-color-secondary);
  font-size: var(--font-size-sm);
  line-height: 1.6;
}

.channel-type-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-md);
}

.empty-tip {
  text-align: center;
  color: var(--text-color-secondary);
  padding: var(--spacing-xl) 0;
  font-size: var(--font-size-sm);
}

.text-danger {
  color: var(--error-color);
}
</style>
