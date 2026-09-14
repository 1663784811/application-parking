<template>
  <div class="thing-model-page">
    <div class="filter-bar">
      <div class="filter-row">
        <div class="filter-item">
          <Input v-model="state.searchForm.keyword" placeholder="模型名称" class="filter-input" clearable @on-enter="handleSearch" />
        </div>
        <div class="filter-item"><Button type="primary" @click="handleSearch">查询</Button></div>
        <div class="filter-item"><Button @click="handleReset">重置</Button></div>
        <div class="filter-item"><Button type="primary" @click="handleAdd"><Icon type="ios-add" />添加物模型</Button></div>
      </div>
    </div>
    <div class="stats-row">
      <div class="stat-item total"><span class="stat-label">模型总数</span><span class="stat-value">{{ state.stats.total }}</span></div>
      <div class="stat-item attr"><span class="stat-label">属性总数</span><span class="stat-value">{{ state.stats.attribute }}</span></div>
      <div class="stat-item event"><span class="stat-label">事件总数</span><span class="stat-value">{{ state.stats.event }}</span></div>
      <div class="stat-item command"><span class="stat-label">指令总数</span><span class="stat-value">{{ state.stats.command }}</span></div>
    </div>
    <div class="table-container">
      <TableColumnSetting :columns="columns" v-model:visible="visibleFields" v-model:open="colSettingVisible" @reset="resetColumns" />
      <Table :columns="displayColumns" :data="state.tableData" :loading="state.loading">
        <template #action="{ row }">
          <Button type="text" size="small" @click="handleManage(row)">管理</Button>
          <Button type="text" size="small" @click="handleEdit(row)">编辑</Button>
          <Button type="text" size="small" @click="handleDelete(row)" class="text-danger">删除</Button>
        </template>
      </Table>
      <div class="pagination-wrapper"><Page :total="state.pagination.total" :current="state.pagination.current" :page-size="state.pagination.pageSize" show-total show-elevator @on-change="handlePageChange" /></div>
    </div>

    <!-- 新增/编辑物模型 -->
    <Modal v-model="state.modalVisible" :title="state.modalType === 'add' ? '添加物模型' : '编辑物模型'" width="520">
      <Form :model="state.formData" :rules="state.rules" :label-width="100" class="modal-form-2col">
        <FormItem label="模型名称" prop="name" class="modal-form-full"><Input v-model="state.formData.name" placeholder="如：停车场道闸物模型" /></FormItem>
        <FormItem label="备注" prop="note" class="modal-form-full"><Input v-model="state.formData.note" type="textarea" :rows="2" placeholder="选填" /></FormItem>
      </Form>
      <template #footer><Button @click="state.modalVisible = false">取消</Button><Button type="primary" @click="handleSubmit">确定</Button></template>
    </Modal>

    <!-- 管理子实体 Drawer -->
    <Drawer v-model="state.drawerVisible" :width="900" :title="`物模型详情 - ${state.currentModel.name}`">
      <Tabs :value="state.activeTab" @on-click="handleTabClick">
        <TabPane label="属性" name="attribute">
          <div class="sub-toolbar"><Button type="primary" size="small" @click="handleSubAdd"><Icon type="ios-add" />添加属性</Button></div>
          <Table :columns="attrColumns" :data="state.attributeList" :loading="state.subLoading">
            <template #attrType="{ row }"><span class="tsl-type">{{ getDataTypeText(row.dataType) }}</span></template>
            <template #attrRange="{ row }">{{ getAttrRangeText(row) }}</template>
            <template #subAction="{ row }">
              <Button type="text" size="small" @click="handleSubEdit(row)">编辑</Button>
              <Button type="text" size="small" @click="handleSubDelete(row)" class="text-danger">删除</Button>
            </template>
          </Table>
        </TabPane>
        <TabPane label="事件" name="event">
          <div class="sub-toolbar"><Button type="primary" size="small" @click="handleSubAdd"><Icon type="ios-add" />添加事件</Button></div>
          <Table :columns="subColumns" :data="state.eventList" :loading="state.subLoading">
            <template #subAction="{ row }">
              <Button type="text" size="small" @click="handleSubEdit(row)">编辑</Button>
              <Button type="text" size="small" @click="handleSubDelete(row)" class="text-danger">删除</Button>
            </template>
          </Table>
        </TabPane>
        <TabPane label="指令" name="command">
          <div class="sub-toolbar"><Button type="primary" size="small" @click="handleSubAdd"><Icon type="ios-add" />添加指令</Button></div>
          <Table :columns="subColumns" :data="state.commandList" :loading="state.subLoading">
            <template #subAction="{ row }">
              <Button type="text" size="small" @click="handleSubEdit(row)">编辑</Button>
              <Button type="text" size="small" @click="handleSubDelete(row)" class="text-danger">删除</Button>
            </template>
          </Table>
        </TabPane>
      </Tabs>
    </Drawer>

    <!-- 子实体编辑 Modal -->
    <Modal v-model="state.subModalVisible" :title="subModalTitle" width="600">
      <Form :model="state.subForm" :label-width="100" class="modal-form-2col">
        <template v-if="state.subModalEntity === 'attribute'">
          <FormItem label="名称" prop="name"><Input v-model="state.subForm.name" placeholder="如：温度" /></FormItem>
          <FormItem label="键(key)" prop="propKey"><Input v-model="state.subForm.propKey" placeholder="如：temperature" /></FormItem>
          <FormItem label="单位"><Input v-model="state.subForm.unit" placeholder="如：℃" /></FormItem>
          <FormItem label="数据类型">
            <Select v-model="state.subForm.dataType">
              <Option value="number">数值</Option>
              <Option value="string">字符串</Option>
              <Option value="enumeration">枚举</Option>
            </Select>
          </FormItem>
          <FormItem v-if="state.subForm.dataType === 'number'" label="最小值"><Input v-model="state.subForm.minValue" placeholder="如：0" /></FormItem>
          <FormItem v-if="state.subForm.dataType === 'number'" label="最大值"><Input v-model="state.subForm.maxValue" placeholder="如：100" /></FormItem>
          <FormItem v-if="state.subForm.dataType === 'enumeration' || state.subForm.dataType === 'string'" label="枚举/列表" class="modal-form-full"><Input v-model="state.subForm.data" placeholder="多个用英文逗号分隔，如：A,B,C" /></FormItem>
        </template>
        <template v-else>
          <FormItem label="名称" prop="name"><Input v-model="state.subForm.name" placeholder="请输入名称" /></FormItem>
          <FormItem label="键(key)" prop="propKey"><Input v-model="state.subForm.propKey" placeholder="如：event_xxx" /></FormItem>
        </template>
      </Form>
      <template #footer><Button @click="state.subModalVisible = false">取消</Button><Button type="primary" @click="handleSubSubmit">确定</Button></template>
    </Modal>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted } from 'vue'
import { Button, Icon, Table, Page, Select, Option, Input, Modal, Form, FormItem, Message, Drawer, Tabs, TabPane } from 'view-ui-plus'
import { thingModelApi } from '@/api'
import TableColumnSetting from '@/components/TableColumnSetting.vue'
import { useTableColumns } from '@/composables/useTableColumns'

const state = reactive({
  searchForm: { keyword: '' },
  stats: { total: 0, attribute: 0, event: 0, command: 0 },
  tableData: [],
  loading: false,
  pagination: { total: 0, current: 1, pageSize: 10 },
  modalVisible: false,
  modalType: 'add',
  formData: { id: null, name: '', note: '' },
  rules: {
    name: [{ required: true, message: '请输入模型名称', trigger: 'blur' }]
  },
  drawerVisible: false,
  currentModel: { id: null, name: '' },
  activeTab: 'attribute',
  attributeList: [],
  eventList: [],
  commandList: [],
  subLoading: false,
  subModalVisible: false,
  subModalType: 'add',
  subModalEntity: 'attribute',
  subForm: {}
})

// 子实体类型 → API / state key 映射
const SUB_API = {
  attribute: { list: 'getAttributeList', save: 'saveAttribute', del: 'deleteAttribute', key: 'attributeList', label: '属性' },
  event: { list: 'getEventList', save: 'saveEvent', del: 'deleteEvent', key: 'eventList', label: '事件' },
  command: { list: 'getCommandList', save: 'saveCommand', del: 'deleteCommand', key: 'commandList', label: '指令' }
}

const columns = [
  { field: 'name', title: '模型名称', key: 'name', minWidth: 200 },
  { field: 'attributeCount', title: '属性数', key: 'attributeCount', minWidth: 100, align: 'center' },
  { field: 'eventCount', title: '事件数', key: 'eventCount', minWidth: 100, align: 'center' },
  { field: 'commandCount', title: '指令数', key: 'commandCount', minWidth: 100, align: 'center' },
  { field: 'createTime', title: '创建时间', key: 'createTime', minWidth: 160 },
  { title: '操作', slot: 'action', minWidth: 200, fixed: 'right' }
]
const { visibleFields, colSettingVisible, displayColumns, resetColumns } = useTableColumns(columns, 'thingModel:columnVisible')

const attrColumns = [
  { title: '名称', key: 'name', minWidth: 120 },
  { title: '键', key: 'propKey', minWidth: 120 },
  { title: '单位', key: 'unit', minWidth: 70 },
  { title: '类型', slot: 'attrType', minWidth: 80 },
  { title: '取值/枚举', slot: 'attrRange', minWidth: 160 },
  { title: '操作', slot: 'subAction', minWidth: 110 }
]
const subColumns = [
  { title: '名称', key: 'name', minWidth: 150 },
  { title: '键', key: 'propKey', minWidth: 150 },
  { title: '操作', slot: 'subAction', minWidth: 110 }
]

const subModalTitle = computed(() => `${state.subModalType === 'add' ? '添加' : '编辑'}${SUB_API[state.subModalEntity].label}`)

const getDataTypeText = (t) => ({ number: '数值', string: '字符串', enumeration: '枚举' }[t] || '-')
const getAttrRangeText = (r) => {
  if (r.dataType === 'number') {
    const min = r.minValue != null && r.minValue !== '' ? r.minValue : '-∞'
    const max = r.maxValue != null && r.maxValue !== '' ? r.maxValue : '+∞'
    return `${min} ~ ${max}`
  }
  if (r.dataType === 'enumeration' || r.dataType === 'string') return r.data || '-'
  return '-'
}

// 物模型统计
const loadStats = async () => {
  try {
    const res = await thingModelApi.getModelStats()
    state.stats = { total: 0, attribute: 0, event: 0, command: 0, ...(res.data || {}) }
  } catch (e) {
    console.error('获取物模型统计失败', e)
  }
}

// 物模型列表 + 批量取每行子实体计数
const initData = async () => {
  state.loading = true
  try {
    const res = await thingModelApi.getModelList({
      page: state.pagination.current,
      size: state.pagination.pageSize,
      keyword: state.searchForm.keyword
    })
    const rows = res.data || []
    state.pagination.total = (res.result && res.result.total) || 0
    if (rows.length) {
      try {
        const countsRes = await thingModelApi.getSubCounts(rows.map(r => r.id))
        const cm = countsRes.data || {}
        rows.forEach(r => {
          const c = cm[String(r.id)] || {}
          r.attributeCount = c.attribute || 0
          r.eventCount = c.event || 0
          r.commandCount = c.command || 0
        })
      } catch (e) {
        console.error('获取子实体计数失败', e)
        rows.forEach(r => { r.attributeCount = 0; r.eventCount = 0; r.commandCount = 0 })
      }
    }
    state.tableData = rows
  } catch (e) {
    console.error('获取物模型列表失败', e)
  } finally {
    state.loading = false
  }
}

const handleSearch = () => { state.pagination.current = 1; initData() }
const handleReset = () => { state.searchForm = { keyword: '' }; handleSearch() }
const handlePageChange = (p) => { state.pagination.current = p; initData() }

// 物模型 CRUD
const handleAdd = () => {
  state.modalType = 'add'
  state.formData = { id: null, name: '', note: '' }
  state.modalVisible = true
}
const handleEdit = (r) => {
  state.modalType = 'edit'
  state.formData = { id: r.id, name: r.name, note: r.note || '' }
  state.modalVisible = true
}
const handleSubmit = async () => {
  if (!state.formData.name) { Message.warning('请输入模型名称'); return }
  try {
    await thingModelApi.saveModel({ ...state.formData })
    Message.success(state.modalType === 'add' ? '添加成功' : '编辑成功')
    state.modalVisible = false
    loadStats()
    initData()
  } catch (e) {
    console.error('保存物模型失败', e)
  }
}
const handleDelete = (r) => {
  Modal.confirm({
    title: '确认删除',
    content: `删除物模型"${r.name}"？其下属性/事件/指令将一并删除。`,
    onOk: async () => {
      try {
        await thingModelApi.deleteModel(r.id)
        Message.success('删除成功')
        loadStats()
        initData()
      } catch (e) {
        console.error('删除物模型失败', e)
      }
    }
  })
}

// Drawer 子实体管理
const handleManage = (r) => {
  state.currentModel = { id: r.id, name: r.name }
  state.activeTab = 'attribute'
  state.drawerVisible = true
  loadSubList()
}
const handleTabClick = (name) => {
  state.activeTab = name
  loadSubList()
}
const loadSubList = async () => {
  const cfg = SUB_API[state.activeTab]
  if (!state.currentModel.id) return
  state.subLoading = true
  try {
    const res = await thingModelApi[cfg.list](state.currentModel.id)
    state[cfg.key] = res.data || []
  } catch (e) {
    console.error('获取子实体列表失败', e)
  } finally {
    state.subLoading = false
  }
}

const emptySubForm = (entity, modelId) => {
  if (entity === 'attribute') {
    return { id: null, thingModelId: modelId, name: '', propKey: '', unit: '', dataType: 'number', minValue: null, maxValue: null, data: '' }
  }
  return { id: null, thingModelId: modelId, name: '', propKey: '' }
}
const handleSubAdd = () => {
  state.subModalType = 'add'
  state.subModalEntity = state.activeTab
  state.subForm = emptySubForm(state.activeTab, state.currentModel.id)
  state.subModalVisible = true
}
const handleSubEdit = (r) => {
  state.subModalType = 'edit'
  state.subModalEntity = state.activeTab
  state.subForm = { ...r }
  state.subModalVisible = true
}
const handleSubSubmit = async () => {
  const cfg = SUB_API[state.subModalEntity]
  if (!state.subForm.name) { Message.warning('请输入名称'); return }
  const data = { ...state.subForm }
  if (state.subModalEntity === 'attribute') {
    data.minValue = (data.minValue === '' || data.minValue === null) ? null : Number(data.minValue)
    data.maxValue = (data.maxValue === '' || data.maxValue === null) ? null : Number(data.maxValue)
  }
  try {
    await thingModelApi[cfg.save](data)
    Message.success(state.subModalType === 'add' ? '添加成功' : '编辑成功')
    state.subModalVisible = false
    loadSubList()
    loadStats()
    initData()
  } catch (e) {
    console.error('保存子实体失败', e)
  }
}
const handleSubDelete = (r) => {
  const cfg = SUB_API[state.subModalEntity]
  Modal.confirm({
    title: '确认删除',
    content: `删除${cfg.label}"${r.name}"？`,
    onOk: async () => {
      try {
        await thingModelApi[cfg.del](r.id)
        Message.success('删除成功')
        loadSubList()
        loadStats()
        initData()
      } catch (e) {
        console.error('删除子实体失败', e)
      }
    }
  })
}

onMounted(() => {
  loadStats()
  initData()
})
</script>

<style lang="less" scoped>
.thing-model-page {
  .filter-bar { padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); margin-bottom: var(--spacing-lg); box-shadow: var(--shadow-base); }
  .filter-row { display: flex; flex-wrap: wrap; gap: var(--spacing-md); .filter-item { flex-shrink: 0; } .filter-input { width: 220px; } }
  .stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--spacing-lg); margin-bottom: var(--spacing-lg); .stat-item { padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); display: flex; flex-direction: column; .stat-label { font-size: var(--font-size-sm); color: var(--text-color-secondary); margin-bottom: var(--spacing-xs); } .stat-value { font-size: 24px; font-weight: 600; color: var(--text-color-title); } &.attr .stat-value { color: var(--primary-color); } &.event .stat-value { color: var(--warning-color); } &.command .stat-value { color: #722ED1; } } }
  .table-container { background: var(--bg-color); border-radius: var(--border-radius-base); padding: var(--spacing-xl); box-shadow: var(--shadow-base); .text-danger { color: var(--error-color); } .pagination-wrapper { display: flex; justify-content: flex-end; margin-top: var(--spacing-xl); } }
  .sub-toolbar { margin-bottom: var(--spacing-md); }
  .tsl-type { padding: 2px 8px; background: rgba(22,93,255,0.1); color: #165DFF; border-radius: var(--border-radius-sm); font-size: var(--font-size-xs); }
}
</style>
