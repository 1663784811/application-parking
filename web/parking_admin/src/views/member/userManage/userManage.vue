<template>
  <div class="user-manage-page">
    <!-- 搜索筛选栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <div class="filter-item">
          <Input
            v-model="state.searchForm.keyword"
            placeholder="搜索账号/昵称/姓名/手机号"
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
          <Input
            v-model="state.searchForm.phone"
            placeholder="手机号"
            class="filter-input-sm"
            clearable
            @on-enter="handleSearch"
          />
        </div>

        <div class="filter-item">
          <Select
            v-model="state.searchForm.status"
            placeholder="状态"
            class="filter-select"
            clearable
          >
            <Option :value="1">启用</Option>
            <Option :value="0">禁用</Option>
          </Select>
        </div>

        <div class="filter-item"><Button type="primary" @click="handleSearch">查询</Button></div>
        <div class="filter-item"><Button @click="handleReset">重置</Button></div>
      </div>
    </div>

    <!-- 数量统计 -->
    <div class="stats-row">
      <div class="stat-item"><span class="stat-label">用户总数</span><span class="stat-value">{{ state.stats.total }}</span></div>
      <div class="stat-item"><span class="stat-label">启用</span><span class="stat-value">{{ state.stats.enabled }}</span></div>
      <div class="stat-item"><span class="stat-label">禁用</span><span class="stat-value">{{ state.stats.disabled }}</span></div>
      <div class="stat-item"><span class="stat-label">本月新增</span><span class="stat-value">{{ state.stats.newThisMonth }}</span></div>
    </div>

    <div class="table-container">
      <TableColumnSetting :columns="columns" v-model:visible="visibleFields" v-model:open="colSettingVisible" @reset="resetColumns" />
      <Table :columns="displayColumns" :data="state.tableData" :loading="state.loading">
        <template #nickName="{ row }">
          <span v-if="row.nickName">{{ row.nickName }}</span>
          <span v-else class="text-muted">—</span>
        </template>
        <template #gender="{ row }">
          <span>{{ getGenderText(row.gender) }}</span>
        </template>
        <template #status="{ row }">
          <Badge :status="row.status === 1 ? 'success' : 'default'" :text="row.status === 1 ? '启用' : '禁用'" />
        </template>
        <template #action="{ row }">
          <Button type="text" size="small" @click="handleEdit(row)">编辑</Button>
          <Button type="text" size="small" @click="handleResetPwd(row)">重置密码</Button>
          <Button type="text" size="small" @click="handleDelete(row)" class="text-danger">删除</Button>
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

    <!-- 编辑 -->
    <Modal v-model="state.modalVisible" title="编辑用户" width="1000">
      <Form :model="state.formData" :rules="state.rules" :label-width="100" class="modal-form-2col">
        <FormItem label="账号" prop="account">
          <Input v-model="state.formData.account" placeholder="请输入账号" />
        </FormItem>
        <FormItem label="密码" prop="password">
          <Input
            v-model="state.formData.password"
            type="password"
            placeholder="留空则不修改密码"
            @on-enter="handleSubmit"
          />
        </FormItem>
        <FormItem label="真实姓名" prop="realName">
          <Input v-model="state.formData.realName" placeholder="请输入真实姓名" />
        </FormItem>
        <FormItem label="昵称" prop="nickName">
          <Input v-model="state.formData.nickName" placeholder="请输入昵称" />
        </FormItem>
        <FormItem label="手机号" prop="phone">
          <Input v-model="state.formData.phone" placeholder="请输入手机号" />
        </FormItem>
        <FormItem label="邮箱" prop="email">
          <Input v-model="state.formData.email" placeholder="请输入邮箱" />
        </FormItem>
        <FormItem label="性别" prop="gender">
          <Select v-model="state.formData.gender">
            <Option :value="0">未知</Option>
            <Option :value="1">男</Option>
            <Option :value="2">女</Option>
          </Select>
        </FormItem>
        <FormItem label="状态" prop="status">
          <RadioGroup v-model="state.formData.status">
            <Radio :label="1">启用</Radio>
            <Radio :label="0">禁用</Radio>
          </RadioGroup>
        </FormItem>
        <FormItem label="生日" prop="birthday">
          <DatePicker
            v-model="state.formData.birthday"
            type="date"
            format="yyyy-MM-dd"
            placeholder="选择生日"
            style="width: 100%"
          />
        </FormItem>
        <FormItem label="个人简介" prop="introduction" class="modal-form-full">
          <Input v-model="state.formData.introduction" placeholder="请输入个人简介" />
        </FormItem>
        <FormItem label="备注" prop="note" class="modal-form-full">
          <Input type="textarea" :rows="2" v-model="state.formData.note" placeholder="请输入备注" />
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
  Icon,
  Input,
  Select,
  Option,
  Table,
  Badge,
  Page,
  Modal,
  Form,
  FormItem,
  RadioGroup,
  Radio,
  DatePicker,
  Message
} from 'view-ui-plus'
import { userApi } from '@/api'
import TableColumnSetting from '@/components/TableColumnSetting.vue'
import { useTableColumns } from '@/composables/useTableColumns'

// 表单初始值
const emptyForm = () => ({
  id: null,
  account: '',
  password: '',
  realName: '',
  nickName: '',
  phone: '',
  email: '',
  gender: 0,
  status: 1,
  birthday: '',
  introduction: '',
  note: ''
})

const state = reactive({
  // 搜索表单
  searchForm: {
    keyword: '',
    phone: '',
    status: null
  },
  // 数量统计
  stats: { total: 0, enabled: 0, disabled: 0, newThisMonth: 0 },
  tableData: [],
  loading: false,
  // 编辑弹窗
  modalVisible: false,
  submitLoading: false,
  formData: emptyForm(),
  // 仅对新增/编辑都必填的字段设规则；密码为条件性必填（仅新增时），
  // 由 handleSubmit 的手动校验兜底，避免编辑时"留空不修改"被 rules 拦死
  rules: {
    account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
    realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
  },
  pagination: {
    total: 0,
    current: 1,
    pageSize: 10
  }
})

const columns = [
  { field: 'id', title: 'ID', key: 'id', minWidth: 80 },
  { field: 'account', title: '账号', key: 'account', minWidth: 120 },
  { field: 'realName', title: '真实姓名', key: 'realName', minWidth: 110 },
  { field: 'nickName', title: '昵称', slot: 'nickName', minWidth: 110 },
  { field: 'phone', title: '手机号', key: 'phone', minWidth: 130 },
  { field: 'gender', title: '性别', slot: 'gender', minWidth: 70, align: 'center' },
  { field: 'status', title: '状态', slot: 'status', minWidth: 90, align: 'center' },
  { field: 'createTime', title: '创建时间', key: 'createTime', minWidth: 160 },
  { field: 'lastLoginTime', title: '最后登录', key: 'lastLoginTime', minWidth: 160 },
  { title: '操作', slot: 'action', minWidth: 190 }
]

const { visibleFields, colSettingVisible, displayColumns, resetColumns } = useTableColumns(columns, 'userManage:columnVisible')

const getGenderText = (g) => ({ 0: '未知', 1: '男', 2: '女' }[g] || '未知')

// 用户数量统计
const loadStats = async () => {
  try {
    const res = await userApi.getUserStats()
    state.stats = { total: 0, enabled: 0, disabled: 0, newThisMonth: 0, ...(res.data || {}) }
  } catch (e) {
    console.error('获取用户统计失败', e)
  }
}

// 用户列表（分页）
const initData = async () => {
  state.loading = true
  try {
    const res = await userApi.getUserList({
      page: state.pagination.current,
      size: state.pagination.pageSize,
      keyword: state.searchForm.keyword || undefined,
      phone: state.searchForm.phone || undefined,
      status: state.searchForm.status
    })
    state.tableData = res.data || []
    if (res.result) {
      state.pagination.total = res.result.total || 0
    }
  } catch (e) {
    console.error('获取用户列表失败', e)
  } finally {
    state.loading = false
  }
}

const handleSearch = () => {
  state.pagination.current = 1
  initData()
}

const handleReset = () => {
  state.searchForm = { keyword: '', phone: '', status: null }
  state.pagination.current = 1
  initData()
}

const handlePageChange = (page) => {
  state.pagination.current = page
  initData()
}

const handleEdit = (row) => {
  state.formData = {
    id: row.id,
    account: row.account || '',
    // 密码不参与回显（后端 @JsonIgnore 不返回），留空即不修改
    password: '',
    realName: row.realName || '',
    nickName: row.nickName || '',
    phone: row.phone || '',
    email: row.email || '',
    gender: row.gender ?? 0,
    status: row.status ?? 1,
    birthday: row.birthday || '',
    introduction: row.introduction || '',
    note: row.note || ''
  }
  state.modalVisible = true
}

const handleSubmit = async () => {
  const form = state.formData
  if (!form.account) {
    Message.warning('请输入账号')
    return
  }
  if (!form.realName) {
    Message.warning('请输入真实姓名')
    return
  }
  if (state.modalType === 'add' && !form.password) {
    Message.warning('请输入密码')
    return
  }
  if (form.password && (form.password.length < 6 || form.password.length > 20)) {
    Message.warning('密码长度需为 6-20 位')
    return
  }

  state.submitLoading = true
  try {
    // 密码留空不传，避免覆盖已有密码
    const payload = { ...form }
    if (!payload.password) {
      delete payload.password
    }
    await userApi.editUser(payload)
    Message.success('编辑成功')
    state.modalVisible = false
    loadStats()
    initData()
  } catch (e) {
    console.error('保存用户失败', e)
  } finally {
    state.submitLoading = false
  }
}

const handleResetPwd = (row) => {
  Modal.confirm({
    title: '确认重置',
    content: `确定重置"${row.realName || row.account}"的密码吗？重置后将无法登录原密码。`,
    onOk: async () => {
      try {
        await userApi.resetPassword(row.id)
        Message.success('密码已重置为 123456')
      } catch (e) {
        console.error('重置密码失败', e)
      }
    }
  })
}

const handleDelete = (row) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定删除用户"${row.realName || row.account}"吗？`,
    onOk: async () => {
      try {
        await userApi.deleteUser(row.id)
        Message.success('删除成功')
        loadStats()
        initData()
      } catch (e) {
        console.error('删除用户失败', e)
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
.user-manage-page {
  .filter-bar {
    padding: var(--spacing-xl);
    background: var(--bg-color);
    border-radius: var(--border-radius-base);
    margin-bottom: var(--spacing-lg);
    box-shadow: var(--shadow-base);

    .filter-row {
      display: flex;
      flex-wrap: wrap;
      gap: var(--spacing-md);
      align-items: center;

      .filter-item {
        flex-shrink: 0;
      }

      .filter-input {
        width: 240px;
      }

      .filter-input-sm {
        width: 150px;
      }

      .filter-select {
        width: 120px;
      }
    }
  }

  .stats-row {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: var(--spacing-lg);
    margin-bottom: var(--spacing-lg);

    .stat-item {
      padding: var(--spacing-xl);
      background: var(--bg-color);
      border-radius: var(--border-radius-base);
      box-shadow: var(--shadow-base);
      display: flex;
      flex-direction: column;

      .stat-label {
        font-size: var(--font-size-sm);
        color: var(--text-color-secondary);
        margin-bottom: var(--spacing-xs);
      }

      .stat-value {
        font-size: 24px;
        font-weight: 600;
        color: var(--text-color-title);
      }
    }
  }

  .table-container {
    background: var(--bg-color);
    border-radius: var(--border-radius-base);
    padding: var(--spacing-xl);
    box-shadow: var(--shadow-base);

    .text-muted {
      color: var(--text-color-secondary);
    }

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
