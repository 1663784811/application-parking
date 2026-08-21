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
      <Table
        :columns="columns"
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
        <FormItem label="通道类型" prop="type">
          <Select v-model="state.form.type" placeholder="请选择通道类型">
            <Option value="in">入口</Option>
            <Option value="out">出口</Option>
            <Option value="inout">出入口</Option>
          </Select>
        </FormItem>
        <FormItem label="所属停车场" prop="parkingId">
          <Select v-model="state.form.parkingId" placeholder="请选择停车场">
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
  </div>
</template>

<script setup>
import {reactive, onMounted} from 'vue'
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

const commonStore = useCommonStore()

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
    type: 'in',
    parkingId: null,
    status: 1
  }
})

const columns = [
  {
    title: '通道编号',
    key: 'code',
    minWidth: 120
  },
  {
    title: '通道名称',
    key: 'name',
    minWidth: 160
  },
  {
    title: '通道类型',
    slot: 'type',
    minWidth: 100
  },
  {
    title: '所属停车场',
    key: 'parkingName',
    minWidth: 160
  },
  {
    title: 'IP地址',
    key: 'ip',
    minWidth: 140
  },
  {
    title: '状态',
    slot: 'status',
    minWidth: 100
  },
  {
    title: '操作',
    slot: 'action',
    minWidth: 140
  }
]

const typeColor = (type) => {
  const map = {in: 'blue', out: 'green', inout: 'orange'}
  return map[type] || 'default'
}

const typeText = (type) => {
  const map = {in: '入口', out: '出口', inout: '出入口'}
  return map[type] || '-'
}

const mockData = () => {
  return [
    {id: 1, code: 'CH001', name: '1号入口', type: 'in', parkingName: '万象城停车场', ip: '192.168.1.10', status: 1},
    {id: 2, code: 'CH002', name: '2号出口', type: 'out', parkingName: '万象城停车场', ip: '192.168.1.11', status: 1},
    {id: 3, code: 'CH003', name: '3号入口', type: 'in', parkingName: '万象城停车场', ip: '192.168.1.12', status: 1},
    {id: 4, code: 'CH004', name: '地下入口', type: 'in', parkingName: 'CBD商务中心停车场', ip: '192.168.2.10', status: 1},
    {id: 5, code: 'CH005', name: '地下出口', type: 'out', parkingName: 'CBD商务中心停车场', ip: '192.168.2.11', status: 0},
    {id: 6, code: 'CH006', name: '东门入口', type: 'in', parkingName: '科技园停车场', ip: '192.168.3.10', status: 1},
    {id: 7, code: 'CH007', name: '西门出口', type: 'out', parkingName: '科技园停车场', ip: '192.168.3.11', status: 1},
    {id: 8, code: 'CH008', name: '主入口', type: 'inout', parkingName: '海岸城购物中心停车场', ip: '192.168.4.10', status: 1}
  ]
}

const loadData = () => {
  state.loading = true
  setTimeout(() => {
    let list = mockData()

    if (state.searchForm.type) {
      list = list.filter(item => item.type === state.searchForm.type)
    }
    if (state.searchForm.status !== null) {
      list = list.filter(item => item.status === state.searchForm.status)
    }
    if (state.searchForm.keyword) {
      const kw = state.searchForm.keyword.trim().toLowerCase()
      list = list.filter(item => item.name.toLowerCase().includes(kw) || item.code.toLowerCase().includes(kw))
    }

    state.tableData = list
    state.pagination.total = list.length
    state.loading = false
  }, 300)
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
    type: 'in',
    parkingId: null,
    status: 1
  }
  state.modalVisible = true
}

const handleEdit = (row) => {
  state.isEdit = true
  state.form = {
    name: row.name,
    code: row.code,
    type: row.type,
    parkingId: null,
    status: row.status
  }
  state.modalVisible = true
}

const handleSubmit = () => {
  if (!state.form.name) {
    Message.warning('请输入通道名称')
    return
  }
  Message.success(state.isEdit ? '编辑成功' : '新增成功')
  state.modalVisible = false
  loadData()
}

const handleDelete = (row) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除通道"${row.name}"吗？`,
    onOk: () => {
      Message.success('删除成功')
      loadData()
    }
  })
}

const handlePageChange = (page) => {
  state.pagination.current = page
  loadData()
}

onMounted(() => {
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
</style>