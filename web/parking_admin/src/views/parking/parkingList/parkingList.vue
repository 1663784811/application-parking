<template>
  <div class="parking-list-page">
    <!-- 搜索筛选栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <div class="filter-item">
          <Input
            v-model="state.searchForm.name"
            placeholder="搜索停车场名称"
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
          <Select
            v-model="state.searchForm.status"
            placeholder="运营状态"
            class="filter-select"
            clearable
          >
            <Option :value="1">启用</Option>
            <Option :value="0">停用</Option>
          </Select>
        </div>

        <div class="filter-item">
          <DatePicker
            v-model="state.searchForm.dateRange"
            type="daterange"
            placeholder="创建时间"
            class="filter-date"
            format="yyyy-MM-dd"
          />
        </div>

        <div class="filter-item"><Button type="primary" @click="handleSearch">查询</Button></div>
        <div class="filter-item"><Button @click="handleReset">重置</Button></div>
        <div class="filter-item">
          <Button type="primary" @click="handleAdd">
            <Icon type="ios-add" />
            新增停车场
          </Button>
        </div>
        <div class="filter-item">
          <Button @click="handleBatchEnable" :disabled="state.selectedRows.length === 0">
            批量启用
          </Button>
        </div>
        <div class="filter-item">
          <Button @click="handleBatchDisable" :disabled="state.selectedRows.length === 0">
            批量停用
          </Button>
        </div>
        <div class="filter-item">
          <Button @click="handleExport">
            <Icon type="ios-download-outline" />
            导出
          </Button>
        </div>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <!-- 列设置：表格上方右侧 -->
      <TableColumnSetting
        :columns="allColumns"
        v-model:visible="visibleFields"
        v-model:open="colSettingVisible"
        @reset="resetColumns"
      />

      <Table
        :columns="displayColumns"
        :data="state.tableData"
        :loading="state.loading"
        :selection="true"
        @on-selection-change="handleSelectionChange"
      >
        <template #status="{ row }">
          <Badge status="success" text="启用" v-if="row.openingUp === 0" />
          <Badge status="error" text="停用" v-else />
        </template>

        <template #action="{ row }">
          <Button type="text" size="small" @click="handleEdit(row)">编辑</Button>
          <Button type="text" size="small" @click="handleSetRules(row)">设置收费规则</Button>
          <Button type="text" size="small" @click="handleViewInLotVehicles(row)">查看在场车辆</Button>
          <Button type="text" size="small" @click="handleDelete(row)" class="text-danger">
            删除
          </Button>
        </template>
      </Table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <Page
          :total="state.pagination.total"
          :current="state.pagination.current"
          :page-size="state.pagination.pageSize"
          show-total
          show-elevator
          show-sizer
          @on-change="handlePageChange"
          @on-page-size-change="handlePageSizeChange"
        />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <Modal
      v-model="state.modalVisible"
      :title="state.modalType === 'add' ? '新增停车场' : '编辑停车场'"
      width="1000"
      :footer-hide="false"
      @on-cancel="handleModalCancel"
    >
      <Form
        ref="formRef"
        :model="state.formData"
        :rules="state.rules"
        :label-width="120"
        class="modal-form-2col"
      >
        <FormItem label="停车场名称" prop="name">
          <Input v-model="state.formData.name" placeholder="请输入停车场名称" />
        </FormItem>

        <FormItem label="详细地址" prop="address">
          <Input v-model="state.formData.address" placeholder="请输入详细地址" />
        </FormItem>

        <FormItem label="经度" prop="longitude">
          <Input v-model="state.formData.longitude" placeholder="如 116.404153" />
        </FormItem>

        <FormItem label="纬度" prop="latitude">
          <Input v-model="state.formData.latitude" placeholder="如 39.915031" />
        </FormItem>

        <FormItem label="总车位数" prop="totalSpaces">
          <InputNumber
            v-model="state.formData.totalSpaces"
            :min="1"
            placeholder="请输入总车位数"
            style="min-width: 100%"
          />
        </FormItem>

        <FormItem label="运营状态" prop="status">
          <RadioGroup v-model="state.formData.status">
            <Radio :label="1">启用</Radio>
            <Radio :label="0">停用</Radio>
          </RadioGroup>
        </FormItem>

        <!-- 图片用 URL 输入而非 <Upload>：后端上传接口（/common/file/upload）实测不可用，
             挂个上传按钮只会是个假按钮；值与 au_app.logo 一致为单个 URL 字符串 -->
        <FormItem label="停车场图片" class="modal-form-full">
          <Input
            v-model="state.formData.image"
            placeholder="图片 URL，选填（H5 首页停车场卡片展示）"
          >
            <template #prefix>
              <Icon type="ios-image" />
            </template>
          </Input>
          <img
            v-if="state.formData.image"
            :src="state.formData.image"
            class="parking-image-preview"
            alt="停车场图片预览"
          />
        </FormItem>
      </Form>

      <template #footer>
        <Button @click="handleModalCancel">取消</Button>
        <Button type="primary" :loading="state.submitLoading" @click="handleSubmit">
          确定
        </Button>
      </template>
    </Modal>

    <!-- 设置收费规则弹窗：展示停车场已关联规则，表格前有添加按钮 -->
    <Modal
      v-model="state.ruleModalVisible"
      title="设置收费规则"
      width="900"
      @on-cancel="state.ruleModalVisible = false"
    >
      <div class="rule-modal-header">
        <Button type="primary" @click="handleOpenAddRule">
          <Icon type="ios-add" />
          添加
        </Button>
        <span v-if="state.currentParking" class="rule-modal-tip">
          停车场「{{ state.currentParking.name }}」已关联 {{ state.associatedRules.length }} 条规则
        </span>
      </div>
      <Table
        :columns="associatedRuleColumns"
        :data="state.associatedRules"
        :loading="state.ruleLoading"
        row-key="id"
        max-height="420"
        empty-text="暂未关联收费规则，点击上方「添加」选择规则"
      >
        <template #carType="{ row }">
          <span>{{ carTypeText(row.carType) }}</span>
        </template>
        <template #type="{ row }">
          <Tag :color="typeColor(row.type)">{{ typeText(row.type) }}</Tag>
        </template>
        <template #timeRange="{ row }">
          <!-- 计费时段：时段（未设=全天）+ 计费单位 -->
          <template v-if="row.type === 2">
            <span v-if="row.startTime && row.endTime">{{ row.startTime }} ~ {{ row.endTime }}</span>
            <span v-else class="text-secondary">全天</span>
            <span v-if="row.rule_time != null"> · 每 {{ row.rule_time }} 分钟</span>
          </template>
          <!-- 首段收费：阶梯档位时长 -->
          <span v-else-if="row.type === 0 && row.rule_time != null">{{ row.rule_time }} 分钟</span>
          <span v-else class="text-secondary">—</span>
        </template>
        <template #amount="{ row }">
          <span>{{ row.amount != null ? `${row.amount} 元` : '—' }}</span>
        </template>
        <template #action="{ row }">
          <Button type="text" size="small" class="text-danger" @click="handleRemoveRule(row)">
            移除
          </Button>
        </template>
      </Table>
      <template #footer>
        <Button @click="state.ruleModalVisible = false">取消</Button>
        <Button type="primary" :loading="state.ruleSaving" @click="handleSaveRules">
          确定
        </Button>
      </template>
    </Modal>

    <!-- 添加收费规则弹窗：规则列表多选 -->
    <Modal
      v-model="state.ruleAddModalVisible"
      title="添加收费规则"
      width="800"
      @on-cancel="state.ruleAddModalVisible = false"
    >
      <div class="rule-modal-header">
        <span v-if="state.currentParking" class="rule-modal-tip">
          为停车场「{{ state.currentParking.name }}」选择要添加的收费规则
        </span>
        <span class="rule-modal-tip">已选 {{ state.addSelectedIds.length }} 条</span>
      </div>
      <Table
        :columns="ruleColumns"
        :data="state.candidateRules"
        :loading="state.addRuleLoading"
        row-key="id"
        max-height="420"
        empty-text="没有可添加的收费规则"
        @on-selection-change="handleAddSelectionChange"
      >
        <template #carType="{ row }">
          <span>{{ carTypeText(row.carType) }}</span>
        </template>
        <template #type="{ row }">
          <Tag :color="typeColor(row.type)">{{ typeText(row.type) }}</Tag>
        </template>
        <template #timeRange="{ row }">
          <!-- 计费时段：时段（未设=全天）+ 计费单位 -->
          <template v-if="row.type === 2">
            <span v-if="row.startTime && row.endTime">{{ row.startTime }} ~ {{ row.endTime }}</span>
            <span v-else class="text-secondary">全天</span>
            <span v-if="row.rule_time != null"> · 每 {{ row.rule_time }} 分钟</span>
          </template>
          <!-- 首段收费：阶梯档位时长 -->
          <span v-else-if="row.type === 0 && row.rule_time != null">{{ row.rule_time }} 分钟</span>
          <span v-else class="text-secondary">—</span>
        </template>
        <template #amount="{ row }">
          <span>{{ row.amount != null ? `${row.amount} 元` : '—' }}</span>
        </template>
      </Table>
      <template #footer>
        <Button @click="state.ruleAddModalVisible = false">取消</Button>
        <Button type="primary" @click="handleConfirmAdd">确定</Button>
      </template>
    </Modal>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Input,
  Select,
  Option,
  DatePicker,
  Button,
  Table,
  Tag,
  Page,
  Modal,
  Form,
  FormItem,
  InputNumber,
  RadioGroup,
  Radio,
  Icon,
  Badge,
  Message
} from 'view-ui-plus'
import { parkingApi, costRulesApi } from '@/api'
import TableColumnSetting from '@/components/TableColumnSetting.vue'
import { useTableColumns } from '@/composables/useTableColumns'

const router = useRouter()
const formRef = ref(null)

// 经纬度校验：留空合法；非空须为数字且在合法范围内
const coordValidator = (min, max, label) => (rule, value, callback) => {
  if (value === '' || value == null) return callback()
  const n = Number(value)
  if (isNaN(n)) return callback(new Error(`请输入有效的${label}`))
  if (n < min || n > max) return callback(new Error(`${label}范围 ${min} ~ ${max}`))
  callback()
}

// 表格列定义：selection/操作固定显示，带 field 的列可由「列设置」控制显隐
const allColumns = [
  {
    type: 'selection',
    width: 80,
    align: 'center'
  },
  {
    field: 'id',
    title: '停车场ID',
    key: 'id',
    minWidth: 100
  },
  {
    field: 'name',
    title: '停车场名称',
    key: 'name',
    minWidth: 180
  },
  {
    field: 'address',
    title: '地址',
    key: 'address',
    minWidth: 200,
    tooltip: true
  },
  {
    field: 'longLat',
    title: '经纬度',
    key: 'longLat',
    minWidth: 160,
    tooltip: true
  },
  {
    field: 'capacity',
    title: '总车位',
    key: 'capacity',
    minWidth: 80,
    align: 'center'
  },
  {
    field: 'remaining',
    title: '剩余车位',
    key: 'capacity',
    minWidth: 80,
    align: 'center'
  },
  {
    field: 'createTime',
    title: '创建时间',
    key: 'createTime',
    minWidth: 160
  },
  {
    field: 'status',
    title: '状态',
    slot: 'status',
    minWidth: 100,
    align: 'center'
  },
  {
    title: '操作',
    slot: 'action',
    minWidth: 260,
    fixed: 'right',
    align: 'center'
  }
]

// 表格列设置（显隐 + 持久化到 localStorage）
const {
  visibleFields,
  colSettingVisible,
  displayColumns,
  resetColumns
} = useTableColumns(allColumns, 'parkingList:columnVisible')

// 收费规则显示映射（与 chargeRuleConfig 一致）
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

// 设置收费规则弹窗：已关联规则表格列（含移除操作）
const associatedRuleColumns = [
  { title: '规则名称', key: 'name', minWidth: 150 },
  { title: '车辆类型', slot: 'carType', minWidth: 100, align: 'center' },
  { title: '收费类型', slot: 'type', minWidth: 110, align: 'center' },
  { title: '计费时段/时长', slot: 'timeRange', minWidth: 170 },
  { title: '金额', slot: 'amount', minWidth: 100, align: 'right' },
  { title: '操作', slot: 'action', width: 80, align: 'center', fixed: 'right' }
]

// 添加收费规则弹窗：规则列表表格列（selection 列驱动多选）
const ruleColumns = [
  { type: 'selection', width: 55, align: 'center' },
  { title: '规则名称', key: 'name', minWidth: 150 },
  { title: '车辆类型', slot: 'carType', minWidth: 100, align: 'center' },
  { title: '收费类型', slot: 'type', minWidth: 110, align: 'center' },
  { title: '计费时段/时长', slot: 'timeRange', minWidth: 170 },
  { title: '金额', slot: 'amount', minWidth: 100, align: 'right' }
]

const state = reactive({
  // 搜索表单
  searchForm: {
    name: '',
    status: null,
    dateRange: []
  },

  // 表格数据
  tableData: [],
  loading: false,
  selectedRows: [],

  // 分页
  pagination: {
    total: 0,
    current: 1,
    pageSize: 10
  },

  // 弹窗
  modalVisible: false,
  modalType: 'add',
  submitLoading: false,

  // 表单数据
  formData: {
    id: null,
    name: '',
    address: '',
    image: '',
    longitude: '',
    latitude: '',
    totalSpaces: 100,
    status: 1
  },

  // 表单验证规则
  rules: {
    name: [
      { required: true, message: '请输入停车场名称', trigger: 'blur' }
    ],
    longitude: [
      { validator: coordValidator(-180, 180, '经度'), trigger: 'blur' }
    ],
    latitude: [
      { validator: coordValidator(-90, 90, '纬度'), trigger: 'blur' }
    ],
    totalSpaces: [
      { required: true, type: 'number', message: '请输入总车位数', trigger: 'blur' }
    ]
  },

  // 设置收费规则弹窗（已关联规则表格 + 添加按钮）
  ruleModalVisible: false,
  ruleLoading: false,
  ruleSaving: false,
  currentParking: null,      // 当前设置规则的停车场行
  allCostRules: [],          // 全部收费规则（缓存，供表格渲染）
  associatedRules: [],       // 已关联规则（完整对象，供表格展示）
  selectedRuleIds: [],       // 已关联规则ID（字符串，防雪花ID精度丢失，保存基线）

  // 添加收费规则弹窗（规则列表多选）
  ruleAddModalVisible: false,
  addRuleLoading: false,
  candidateRules: [],        // 可添加的规则（排除已关联）
  addSelectedIds: []         // 添加弹窗勾选的规则ID（字符串，防雪花ID精度丢失）
})

// 初始化数据
const initData = async () => {
  state.loading = true
  try {
    const res = await parkingApi.getParkingList({
      page: state.pagination.current,
      size: state.pagination.pageSize,
      name: state.searchForm.name || undefined,
      status: state.searchForm.status
    })
    state.tableData = res.data || []
    if (res.result) {
      state.pagination.total = res.result.total || 0
    }
  } catch (e) {
    console.error('获取停车场列表失败', e)
  } finally {
    state.loading = false
  }
}

// 搜索
const handleSearch = () => {
  state.pagination.current = 1
  initData()
}

// 重置
const handleReset = () => {
  state.searchForm = {
    name: '',
    status: null,
    dateRange: []
  }
  handleSearch()
}

// 新增
const handleAdd = () => {
  state.modalType = 'add'
  state.formData = {
    id: null,
    name: '',
    address: '',
    image: '',
    longitude: '',
    latitude: '',
    totalSpaces: 100,
    status: 1
  }
  state.modalVisible = true
}

// 编辑
const handleEdit = (row) => {
  // longLat 存储格式 "经度,纬度"（long,lat），回填到两个输入框
  const [lng, lat] = (row.longLat || '').split(',')
  state.modalType = 'edit'
  state.formData = {
    id: row.id,
    name: row.name,
    address: row.address,
    image: row.image || '',
    longitude: lng ? lng.trim() : '',
    latitude: lat ? lat.trim() : '',
    totalSpaces: row.capacity,
    status: row.openingUp === 0 ? 1 : 0
  }
  state.modalVisible = true
}

// 删除
const handleDelete = async (row) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除停车场"${row.name}"吗？删除后无法恢复。`,
    onOk: async () => {
      try {
        await parkingApi.deleteParking(row.id)
        Message.success('删除成功')
        initData()
      } catch (e) {
        console.error('删除失败', e)
      }
    }
  })
}

// 查看该停车场的在场车辆
const handleViewInLotVehicles = (row) => {
  router.push({ name: 'spaceManagement', query: { parkingId: row.id } })
}

// 设置收费规则：打开弹窗，加载全部规则（缓存）+ 该停车场已关联规则，组装关联表格数据
const handleSetRules = async (row) => {
  state.currentParking = row
  state.ruleModalVisible = true
  state.ruleLoading = true
  state.associatedRules = []
  state.selectedRuleIds = []
  try {
    // 规则总量不大，size 取较大值一次性加载供表格展示；已加载则复用缓存
    if (state.allCostRules.length === 0) {
      const rulesRes = await costRulesApi.getCostRulesList({ size: 1000 })
      state.allCostRules = rulesRes.data || []
    }
    const res = await costRulesApi.getCostRulesByParking(row.id)
    // 已关联规则ID集合 → 字符串集合（防雪花ID精度丢失），过滤出完整规则对象供表格展示
    const associated = new Set((Array.isArray(res.data) ? res.data : []).map(id => String(id)))
    state.selectedRuleIds = [...associated]
    state.associatedRules = state.allCostRules.filter(r => associated.has(String(r.id)))
  } catch (e) {
    console.error('获取收费规则关联失败', e)
  } finally {
    state.ruleLoading = false
  }
}

// 打开添加收费规则弹窗：候选 = 全部规则 - 已关联规则，重新置空勾选
const handleOpenAddRule = () => {
  state.candidateRules = state.allCostRules.filter(r => !state.selectedRuleIds.includes(String(r.id)))
  state.addSelectedIds = []
  state.ruleAddModalVisible = true
}

// 添加弹窗选择变化：同步勾选规则ID（字符串，防雪花ID精度丢失）
const handleAddSelectionChange = (selection) => {
  state.addSelectedIds = selection.map(r => String(r.id))
}

// 确认添加：合并所选规则到已关联列表，刷新保存基线
const handleConfirmAdd = () => {
  if (state.addSelectedIds.length === 0) {
    Message.warning('请先选择要添加的收费规则')
    return
  }
  const addIds = new Set(state.addSelectedIds)
  const added = state.candidateRules.filter(r => addIds.has(String(r.id)))
  state.associatedRules = [...state.associatedRules, ...added]
  state.selectedRuleIds = state.associatedRules.map(r => String(r.id))
  state.ruleAddModalVisible = false
}

// 移除已关联规则：同步更新关联列表与保存基线
const handleRemoveRule = (row) => {
  state.associatedRules = state.associatedRules.filter(r => String(r.id) !== String(row.id))
  state.selectedRuleIds = state.associatedRules.map(r => String(r.id))
}

// 保存收费规则关联（按停车场同步多对多）
const handleSaveRules = async () => {
  if (!state.currentParking) return
  state.ruleSaving = true
  try {
    await costRulesApi.saveCostRulesByParking(state.currentParking.id, state.selectedRuleIds)
    Message.success('收费规则设置成功')
    state.ruleModalVisible = false
  } catch (e) {
    console.error('保存收费规则失败', e)
  } finally {
    state.ruleSaving = false
  }
}

// 批量选择
const handleSelectionChange = (selection) => {
  state.selectedRows = selection
}

// 批量启用
const handleBatchEnable = () => {
  Message.success(`已启用 ${state.selectedRows.length} 个停车场`)
  initData()
}

// 批量停用
const handleBatchDisable = () => {
  Message.success(`已停用 ${state.selectedRows.length} 个停车场`)
  initData()
}

// 导出
const handleExport = () => {
  Message.info('正在导出...')
}

// 弹窗取消
const handleModalCancel = () => {
  state.modalVisible = false
  formRef.value?.resetFields()
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    state.submitLoading = true

    const params = {
      name: state.formData.name,
      address: state.formData.address,
      image: state.formData.image || '',
      longLat: state.formData.longitude && state.formData.latitude
        ? `${state.formData.longitude},${state.formData.latitude}`
        : '',
      capacity: state.formData.totalSpaces,
      openingUp: state.formData.status === 1 ? 0 : 1
    }
    if (state.formData.id) {
      params.id = state.formData.id
    }

    await parkingApi.editParking(params)
    Message.success(state.modalType === 'add' ? '新增成功' : '编辑成功')
    state.modalVisible = false
    state.submitLoading = false
    initData()
  } catch (e) {
    console.error('操作失败', e)
  } finally {
    state.submitLoading = false
  }
}

// 分页
const handlePageChange = (page) => {
  state.pagination.current = page
  initData()
}

// 每页条数
const handlePageSizeChange = (size) => {
  state.pagination.pageSize = size
  initData()
}

// 初始化
onMounted(() => {
  initData()
})
</script>

<style lang="less" scoped>
.parking-list-page {
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

      .filter-input {
        width: 240px;
      }

      .filter-select {
        width: 150px;
      }

      .filter-date {
        width: 260px;
      }
    }
  }

  .table-container {
    background-color: var(--bg-color);
    border-radius: var(--border-radius-base);
    padding: var(--spacing-xl);
    box-shadow: var(--shadow-base);

    .text-danger {
      color: var(--error-color);
    }

    .pagination-wrapper {
      display: flex;
      justify-content: flex-end;
      margin-top: var(--spacing-xl);
    }
  }

  .rule-modal-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: var(--spacing-md);
  }

  .rule-modal-tip {
    color: var(--text-color-secondary);
  }
}

// 停车场图片预览。Modal 内容 teleport 到 body，样式不能挂在 .parking-list-page 下
.parking-image-preview {
  display: block;
  width: 160px;
  height: 100px;
  margin-top: var(--spacing-sm);
  object-fit: cover;
  border: 1px solid var(--border-color-base);
  border-radius: var(--border-radius-base);
  background-color: var(--bg-color-secondary);
}
</style>