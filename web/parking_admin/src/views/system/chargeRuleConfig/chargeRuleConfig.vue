<template>
  <div class="charge-rule-config-page">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <div class="filter-item">
          <Select
            v-model="state.searchForm.parkingId"
            placeholder="选择停车场"
            class="filter-select"
            clearable
            @on-change="handleParkingChange"
          >
            <Option v-for="item in state.parkingList" :key="item.id" :value="item.id">
              {{ item.name }}
            </Option>
          </Select>
        </div>
        <div class="filter-item">
          <Input
            v-model="state.searchForm.keyword"
            placeholder="搜索规则名称"
            class="filter-input"
            clearable
            @on-enter="handleSearch"
          >
            <template #prefix>
              <Icon type="ios-search" />
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
          <Button
            type="primary"
            :disabled="!state.searchForm.parkingId"
            @click="handleAdd"
          >
            <Icon type="ios-add" />
            新增规则
          </Button>
        </div>
      </div>
    </div>

    <!-- 表格 -->
    <div class="table-container">
      <Table :columns="columns" :data="state.tableData" :loading="state.loading">
        <template #carType="{ row }">
          <span>{{ carTypeText(row.carType) }}</span>
        </template>
        <template #type="{ row }">
          <Tag :color="typeColor(row.type)">{{ typeText(row.type) }}</Tag>
        </template>
        <template #timeRange="{ row }">
          <span v-if="row.startTime && row.endTime">{{ row.startTime }} ~ {{ row.endTime }}</span>
          <span v-else-if="row.rule_time != null">{{ row.rule_time }} 分钟</span>
          <span v-else class="text-secondary">—</span>
        </template>
        <template #amount="{ row }">
          <span>{{ row.amount != null ? `${row.amount} 元` : '—' }}</span>
        </template>
        <template #effective="{ row }">
          <span v-if="row.effectiveStartTime || row.effectiveEndTime">
            {{ row.effectiveStartTime || '—' }} ~ {{ row.effectiveEndTime || '—' }}
          </span>
          <span v-else class="text-secondary">长期有效</span>
        </template>
        <template #action="{ row }">
          <Button type="text" size="small" @click="handleEdit(row)">编辑</Button>
          <Button type="text" size="small" @click="handleDelete(row)" class="text-danger">删除</Button>
        </template>
      </Table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <Modal
      v-model="state.modalVisible"
      :title="state.isEdit ? '编辑规则' : '新增规则'"
      width="600"
    >
      <Form :model="state.form" :label-width="100">
        <FormItem label="规则名称" prop="name">
          <Input v-model="state.form.name" placeholder="请输入规则名称" />
        </FormItem>
        <FormItem label="车辆类型" prop="carType">
          <Select v-model="state.form.carType" placeholder="请选择车辆类型">
            <Option value="0">小型汽车</Option>
            <Option value="1">中型汽车</Option>
            <Option value="2">大型汽车</Option>
          </Select>
        </FormItem>
        <FormItem label="收费类型" prop="type">
          <Select v-model="state.form.type" placeholder="请选择收费类型" @on-change="handleTypeChange">
            <Option :value="0">首段收费</Option>
            <Option :value="2">计费时段</Option>
            <Option :value="3">每天封顶金额</Option>
            <Option :value="5">每次封顶金额</Option>
          </Select>
        </FormItem>
        <template v-if="state.form.type === 2">
          <FormItem label="开始时间" prop="startTime">
            <Input v-model="state.form.startTime" placeholder="如 08:00:00" class="time-input" />
          </FormItem>
          <FormItem label="结束时间" prop="endTime">
            <Input v-model="state.form.endTime" placeholder="如 20:00:00" class="time-input" />
          </FormItem>
        </template>
        <FormItem v-if="state.form.type === 0" label="首段时长" prop="rule_time">
          <InputNumber v-model="state.form.rule_time" :min="0" placeholder="分钟" class="form-number" />
          <span class="unit">分钟</span>
        </FormItem>
        <FormItem label="金额" prop="amount">
          <InputNumber v-model="state.form.amount" :min="0" :precision="2" class="form-number" />
          <span class="unit">元</span>
        </FormItem>
        <FormItem label="适用星期" prop="week">
          <Select v-model="state.weekList" multiple class="week-select" placeholder="不选则每天适用">
            <Option value="Monday">周一</Option>
            <Option value="Tuesday">周二</Option>
            <Option value="Wednesday">周三</Option>
            <Option value="Thursday">周四</Option>
            <Option value="Friday">周五</Option>
            <Option value="Saturday">周六</Option>
            <Option value="Sunday">周日</Option>
          </Select>
        </FormItem>
        <FormItem label="生效开始" prop="effectiveStartTime">
          <DatePicker
            type="date"
            format="yyyy-MM-dd"
            :value="state.form.effectiveStartTime"
            @on-change="onEffectiveStartChange"
            placeholder="选择开始日期"
            class="date-input"
          />
        </FormItem>
        <FormItem label="生效结束" prop="effectiveEndTime">
          <DatePicker
            type="date"
            format="yyyy-MM-dd"
            :value="state.form.effectiveEndTime"
            @on-change="onEffectiveEndChange"
            placeholder="选择结束日期"
            class="date-input"
          />
        </FormItem>
      </Form>
      <template #footer>
        <Button @click="state.modalVisible = false">取消</Button>
        <Button type="primary" :loading="state.submitLoading" @click="handleSubmit">确定</Button>
      </template>
    </Modal>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import {
  Button,
  DatePicker,
  Form,
  FormItem,
  Icon,
  Input,
  InputNumber,
  Message,
  Modal,
  Option,
  Select,
  Table,
  Tag
} from 'view-ui-plus'
import { costRulesApi, parkingApi } from '@/api'

const state = reactive({
  searchForm: {
    parkingId: null,
    keyword: ''
  },

  parkingList: [],

  loading: false,
  tableData: [],

  modalVisible: false,
  isEdit: false,
  submitLoading: false,

  form: {
    id: null,
    parkingId: null,
    name: '',
    carType: '0',
    type: 2,
    startTime: '',
    endTime: '',
    rule_time: null,
    amount: 0,
    week: null,
    effectiveStartTime: null,
    effectiveEndTime: null
  },

  // 适用星期多选（与 form.week 字符串互转，避免直接提交数组）
  weekList: []
})

const columns = [
  { title: '规则名称', key: 'name', minWidth: 160 },
  { title: '车辆类型', slot: 'carType', minWidth: 110, align: 'center' },
  { title: '收费类型', slot: 'type', minWidth: 120, align: 'center' },
  { title: '计费时段/时长', slot: 'timeRange', minWidth: 180 },
  { title: '金额', slot: 'amount', minWidth: 110, align: 'right' },
  { title: '生效日期', slot: 'effective', minWidth: 200 },
  { title: '操作', slot: 'action', minWidth: 140, fixed: 'right' }
]

const carTypeText = (carType) => {
  const map = { '0': '小型汽车', '1': '中型汽车', '2': '大型汽车' }
  return map[carType] ?? carType ?? '—'
}

const typeText = (type) => {
  const map = { 0: '首段收费', 2: '计费时段', 3: '每天封顶', 5: '每次封顶' }
  return map[type] ?? '—'
}

const typeColor = (type) => {
  const map = { 0: 'blue', 2: 'green', 3: 'orange', 5: 'purple' }
  return map[type] || 'default'
}

// 加载停车场列表（供选择器使用）
const loadParkingList = async () => {
  try {
    // size 取较大值以一次性加载全部停车场供选择器使用
    const res = await parkingApi.getParkingList({ size: 1000 })
    state.parkingList = res.data || []
  } catch (e) {
    console.error('获取停车场列表失败', e)
  }
}

// 加载计费规则（按停车场ID；列表接口无分页、无服务端筛选）
const loadData = async () => {
  if (!state.searchForm.parkingId) {
    state.tableData = []
    return
  }
  state.loading = true
  try {
    const res = await costRulesApi.getCostRulesList(state.searchForm.parkingId)
    let list = res.data || []
    // 客户端按名称关键词筛选
    if (state.searchForm.keyword) {
      const kw = state.searchForm.keyword.trim().toLowerCase()
      list = list.filter(item => (item.name || '').toLowerCase().includes(kw))
    }
    state.tableData = list
  } catch (e) {
    console.error('获取计费规则失败', e)
  } finally {
    state.loading = false
  }
}

const handleParkingChange = () => {
  loadData()
}

const handleSearch = () => {
  loadData()
}

const handleReset = () => {
  state.searchForm.parkingId = null
  state.searchForm.keyword = ''
  loadData()
}

// 切换收费类型时清理无关字段，避免提交脏数据
const handleTypeChange = (val) => {
  if (val !== 2) {
    state.form.startTime = ''
    state.form.endTime = ''
  }
  if (val !== 0) {
    state.form.rule_time = null
  }
}

// 将 DatePicker 变更值归一化为 yyyy-MM-dd 字符串（兼容 Date 对象与字符串，避免提交 ISO 时间导致后端解析失败）
const formatDateValue = (val) => {
  if (!val) return null
  if (typeof val === 'string') return val
  if (val instanceof Date) {
    const y = val.getFullYear()
    const m = String(val.getMonth() + 1).padStart(2, '0')
    const d = String(val.getDate()).padStart(2, '0')
    return `${y}-${m}-${d}`
  }
  return val
}

const onEffectiveStartChange = (val) => {
  state.form.effectiveStartTime = formatDateValue(val)
}

const onEffectiveEndChange = (val) => {
  state.form.effectiveEndTime = formatDateValue(val)
}

const handleAdd = () => {
  if (!state.searchForm.parkingId) {
    Message.warning('请先选择停车场')
    return
  }
  state.isEdit = false
  state.form = {
    id: null,
    parkingId: state.searchForm.parkingId,
    name: '',
    carType: '0',
    type: 2,
    startTime: '',
    endTime: '',
    rule_time: null,
    amount: 0,
    week: null,
    effectiveStartTime: null,
    effectiveEndTime: null
  }
  state.weekList = []
  state.modalVisible = true
}

const handleEdit = (row) => {
  state.isEdit = true
  // 复制整行，保留表单未展示的字段，避免编辑时丢失
  state.form = { ...row }
  // week 为逗号分隔的英文星期字符串，拆分为数组供多选；非逗号分隔的旧值会原样保留（见 handleSubmit）
  state.weekList = row.week ? row.week.split(',').map(s => s.trim()).filter(Boolean) : []
  state.modalVisible = true
}

const handleSubmit = async () => {
  if (!state.form.name) {
    Message.warning('请输入规则名称')
    return
  }
  if (state.form.amount == null) {
    Message.warning('请输入金额')
    return
  }
  if (state.form.type === 2 && (!state.form.startTime || !state.form.endTime)) {
    Message.warning('请填写计费时段的开始与结束时间')
    return
  }
  if (state.form.type === 0 && state.form.rule_time == null) {
    Message.warning('请填写首段时长')
    return
  }
  state.submitLoading = true
  try {
    const payload = {
      ...state.form,
      // 适用星期：多选数组转逗号分隔字符串；未选则为 null
      week: state.weekList.length ? state.weekList.join(',') : null
    }
    await costRulesApi.saveCostRules(payload)
    Message.success(state.isEdit ? '编辑成功' : '新增成功')
    state.modalVisible = false
    loadData()
  } catch (e) {
    console.error('保存失败', e)
  } finally {
    state.submitLoading = false
  }
}

const handleDelete = (row) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除规则"${row.name}"吗？`,
    onOk: async () => {
      try {
        await costRulesApi.deleteCostRules(row.id)
        Message.success('删除成功')
        loadData()
      } catch (e) {
        console.error('删除失败', e)
      }
    }
  })
}

onMounted(() => {
  loadParkingList()
})
</script>

<style lang="less" scoped>
.charge-rule-config-page {
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
        width: 180px;
      }

      .filter-input {
        width: 200px;
      }
    }
  }

  .table-container {
    background-color: var(--bg-color);
    border-radius: var(--border-radius-base);
    padding: var(--spacing-xl);
    box-shadow: var(--shadow-base);

    .text-secondary {
      color: var(--text-color-secondary);
    }

    .text-danger {
      color: var(--error-color);
    }
  }

  .form-number {
    width: 200px;
  }

  .time-input {
    width: 200px;
  }

  .week-select {
    width: 100%;
  }

  .date-input {
    width: 200px;
  }

  .unit {
    margin-left: var(--spacing-sm);
    color: var(--text-color-secondary);
  }
}
</style>
