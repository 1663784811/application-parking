<template>
  <div class="parking-list-page">
    <!-- 搜索筛选栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <div class="filter-item">
          <Input
            v-model="state.searchForm.name"
            placeholder="搜索车场名称"
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
      </div>

      <div class="filter-actions">
        <Button type="primary" @click="handleAdd">
          <Icon type="ios-add" />
          新增车场
        </Button>
        <Button @click="handleBatchEnable" :disabled="state.selectedRows.length === 0">
          批量启用
        </Button>
        <Button @click="handleBatchDisable" :disabled="state.selectedRows.length === 0">
          批量停用
        </Button>
        <Button @click="handleExport">
          <Icon type="ios-download-outline" />
          导出
        </Button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <Table
        :columns="columns"
        :data="state.tableData"
        :loading="state.loading"
        :selection="true"
        @on-selection-change="handleSelectionChange"
      >
        <template #status="{ row }">
          <Badge status="success" text="启用" v-if="row.status === 1" />
          <Badge status="error" text="停用" v-else />
        </template>

        <template #action="{ row }">
          <Button type="text" size="small" @click="handleEdit(row)">编辑</Button>
          <Button type="text" size="small" @click="handleViewSpaces(row)">查看车位</Button>
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
      :title="state.modalType === 'add' ? '新增车场' : '编辑车场'"
      width="600"
      :footer-hide="false"
      @on-cancel="handleModalCancel"
    >
      <Form
        ref="formRef"
        :model="state.formData"
        :rules="state.rules"
        :label-width="120"
      >
        <FormItem label="车场名称" prop="name">
          <Input v-model="state.formData.name" placeholder="请输入车场名称" />
        </FormItem>

        <FormItem label="所在区域" prop="region">
          <Input v-model="state.formData.region" placeholder="请输入所在区域" />
        </FormItem>

        <FormItem label="详细地址" prop="address">
          <Input v-model="state.formData.address" placeholder="请输入详细地址" />
        </FormItem>

        <FormItem label="总车位数" prop="totalSpaces">
          <InputNumber
            v-model="state.formData.totalSpaces"
            :min="1"
            placeholder="请输入总车位数"
            style="minWidth: 100%"
          />
        </FormItem>

        <FormItem label="联系人" prop="contact">
          <Input v-model="state.formData.contact" placeholder="请输入联系人" />
        </FormItem>

        <FormItem label="联系电话" prop="phone">
          <Input v-model="state.formData.phone" placeholder="请输入联系电话" />
        </FormItem>

        <FormItem label="收费模板" prop="chargeTemplate">
          <Select v-model="state.formData.chargeTemplate" placeholder="请选择收费模板">
            <Option :value="1">标准收费</Option>
            <Option :value="2">商场收费</Option>
            <Option :value="3">大厦收费</Option>
          </Select>
        </FormItem>

        <FormItem label="运营状态" prop="status">
          <RadioGroup v-model="state.formData.status">
            <Radio :label="1">启用</Radio>
            <Radio :label="0">停用</Radio>
          </RadioGroup>
        </FormItem>
      </Form>

      <template #footer>
        <Button @click="handleModalCancel">取消</Button>
        <Button type="primary" :loading="state.submitLoading" @click="handleSubmit">
          确定
        </Button>
      </template>
    </Modal>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  Input,
  Select,
  Option,
  DatePicker,
  Button,
  Table,
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

const router = useRouter()
const formRef = ref(null)

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
    region: '',
    address: '',
    totalSpaces: 100,
    contact: '',
    phone: '',
    chargeTemplate: null,
    status: 1
  },

  // 表单验证规则
  rules: {
    name: [
      { required: true, message: '请输入车场名称', trigger: 'blur' }
    ],
    region: [
      { required: true, message: '请输入所在区域', trigger: 'blur' }
    ],
    totalSpaces: [
      { required: true, type: 'number', message: '请输入总车位数', trigger: 'blur' }
    ],
    phone: [
      { required: true, message: '请输入联系电话', trigger: 'blur' }
    ]
  }
})

// 表格列定义
const columns = [
  {
    type: 'selection',
    minWidth: 60,
    align: 'center'
  },
  {
    title: '车场ID',
    key: 'id',
    minWidth: 100
  },
  {
    title: '车场名称',
    key: 'name',
    minWidth: 180
  },
  {
    title: '地址',
    key: 'address',
    minWidth: 200,
    tooltip: true
  },
  {
    title: '总车位',
    key: 'totalSpaces',
    minWidth: 100,
    align: 'center'
  },
  {
    title: '空闲车位',
    key: 'freeSpaces',
    minWidth: 100,
    align: 'center'
  },
  {
    title: '管理员',
    key: 'contact',
    minWidth: 120
  },
  {
    title: '联系电话',
    key: 'phone',
    minWidth: 130
  },
  {
    title: '创建时间',
    key: 'createTime',
    minWidth: 160
  },
  {
    title: '状态',
    slot: 'status',
    minWidth: 100,
    align: 'center'
  },
  {
    title: '操作',
    slot: 'action',
    minWidth: 180,
    fixed: 'right',
    align: 'center'
  }
]

// 初始化数据
const initData = () => {
  state.loading = true
  setTimeout(() => {
    state.tableData = [
      {
        id: 1001,
        name: '城西停车场',
        address: '北京市朝阳区城西路88号',
        region: '朝阳区',
        totalSpaces: 300,
        freeSpaces: 156,
        contact: '张经理',
        phone: '13800138001',
        chargeTemplate: 1,
        status: 1,
        createTime: '2024-01-01 10:00:00'
      },
      {
        id: 1002,
        name: '购物中心停车场',
        address: '北京市海淀区中关村大街100号',
        region: '海淀区',
        totalSpaces: 500,
        freeSpaces: 234,
        contact: '李经理',
        phone: '13800138002',
        chargeTemplate: 2,
        status: 1,
        createTime: '2024-01-05 14:30:00'
      },
      {
        id: 1003,
        name: '城东停车场',
        address: '北京市东城区东四大街66号',
        region: '东城区',
        totalSpaces: 200,
        freeSpaces: 89,
        contact: '王经理',
        phone: '13800138003',
        chargeTemplate: 1,
        status: 1,
        createTime: '2024-01-10 09:00:00'
      },
      {
        id: 1004,
        name: '写字楼停车场',
        address: '北京市西城区金融街8号',
        region: '西城区',
        totalSpaces: 150,
        freeSpaces: 0,
        contact: '刘经理',
        phone: '13800138004',
        chargeTemplate: 3,
        status: 0,
        createTime: '2024-01-15 16:00:00'
      }
    ]
    state.pagination.total = 4
    state.loading = false
  }, 500)
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
    region: '',
    address: '',
    totalSpaces: 100,
    contact: '',
    phone: '',
    chargeTemplate: null,
    status: 1
  }
  state.modalVisible = true
}

// 编辑
const handleEdit = (row) => {
  state.modalType = 'edit'
  state.formData = { ...row }
  state.modalVisible = true
}

// 删除
const handleDelete = (row) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除车场"${row.name}"吗？删除后无法恢复。`,
    onOk: () => {
      Message.success('删除成功')
      initData()
    }
  })
}

// 查看车位
const handleViewSpaces = (row) => {
  router.push({ name: 'spaceManagement', query: { parkingId: row.id } })
}

// 批量选择
const handleSelectionChange = (selection) => {
  state.selectedRows = selection
}

// 批量启用
const handleBatchEnable = () => {
  Message.success(`已启用 ${state.selectedRows.length} 个车场`)
  initData()
}

// 批量停用
const handleBatchDisable = () => {
  Message.success(`已停用 ${state.selectedRows.length} 个车场`)
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

    setTimeout(() => {
      Message.success(state.modalType === 'add' ? '新增成功' : '编辑成功')
      state.modalVisible = false
      state.submitLoading = false
      initData()
    }, 500)
  } catch (e) {
    console.error('表单验证失败', e)
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
initData()
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

    .filter-actions {
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
}
</style>