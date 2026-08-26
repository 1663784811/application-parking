<template>
  <div class="admin-account-page">
    <!-- 搜索筛选栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <div class="filter-item">
          <Input
            v-model="state.searchForm.keyword"
            placeholder="搜索账号/姓名/手机号"
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
            placeholder="状态"
            class="filter-select"
            clearable
          >
            <Option :value="1">启用</Option>
            <Option :value="0">停用</Option>
          </Select>
        </div>

        <div class="filter-item"><Button type="primary" @click="handleSearch">查询</Button></div>
        <div class="filter-item"><Button @click="handleReset">重置</Button></div>
        <div class="filter-actions">
          <Button type="primary" @click="handleAdd"><Icon type="ios-add" />新增管理员</Button>
        </div>
      </div>
    </div>

    <div class="table-container">
      <TableColumnSetting :columns="columns" v-model:visible="visibleFields" v-model:open="colSettingVisible" @reset="resetColumns" />
      <Table :columns="displayColumns" :data="state.tableData" :loading="state.loading">
        <template #role="{ row }"><span class="role-badge" v-if="row.roleName">{{ row.roleName }}</span><span v-else class="text-muted">—</span></template>
        <template #status="{ row }"><i-switch :value="row.status === 1" @on-change="v => handleToggleStatus(row, v)" /></template>
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

    <Modal v-model="state.modalVisible" :title="state.modalType === 'add' ? '新增管理员' : '编辑管理员'" width="500">
      <Form ref="formRef" :model="state.formData" :rules="state.rules" :label-width="100">
        <FormItem label="账号" prop="account"><Input v-model="state.formData.account" placeholder="请输入账号" /></FormItem>
        <FormItem label="真实姓名" prop="realName"><Input v-model="state.formData.realName" placeholder="请输入真实姓名" /></FormItem>
        <FormItem label="手机号" prop="phone"><Input v-model="state.formData.phone" placeholder="请输入手机号" /></FormItem>
        <FormItem label="角色" prop="roleIds">
          <Select v-model="state.formData.roleIds" multiple filterable placeholder="请选择角色（可多选）">
            <Option v-for="item in state.roleList" :key="item.id" :value="item.id">{{ item.name }}</Option>
          </Select>
        </FormItem>
        <FormItem label="状态" prop="status"><RadioGroup v-model="state.formData.status"><Radio :label="1">启用</Radio><Radio :label="0">停用</Radio></RadioGroup></FormItem>
      </Form>
      <template #footer><Button @click="state.modalVisible = false">取消</Button><Button type="primary" :loading="state.submitLoading" @click="handleSubmit">确定</Button></template>
    </Modal>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { Button, Icon, Input, Select, Option, Table, Switch, Page, Modal, Form, FormItem, RadioGroup, Radio, Message } from 'view-ui-plus'
import { systemApi } from '@/api'
import TableColumnSetting from '@/components/TableColumnSetting.vue'
import { useTableColumns } from '@/composables/useTableColumns'

const formRef = ref()

const state = reactive({
  // 搜索表单
  searchForm: {
    keyword: '',
    status: null
  },
  tableData: [],
  loading: false,
  // 角色下拉数据（来自后端 /system/role/list）
  roleList: [],
  // 弹窗
  modalVisible: false,
  modalType: 'add',
  submitLoading: false,
  formData: { id: null, account: '', realName: '', phone: '', roleIds: [], status: 1 },
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
  { field: 'realName', title: '真实姓名', key: 'realName', minWidth: 120 },
  { field: 'phone', title: '手机号', key: 'phone', minWidth: 130 },
  { field: 'role', title: '角色', slot: 'role', minWidth: 120 },
  { field: 'createTime', title: '创建时间', key: 'createTime', minWidth: 160 },
  { field: 'lastLoginTime', title: '最后登录', key: 'lastLoginTime', minWidth: 160 },
  { field: 'status', title: '状态', slot: 'status', minWidth: 80, align: 'center' },
  { title: '操作', slot: 'action', minWidth: 200 }
]

const { visibleFields, colSettingVisible, displayColumns, resetColumns } = useTableColumns(columns, 'adminAccount:columnVisible')

// 加载角色下拉（后端返回全部角色，含成员数）
const loadRoleList = async () => {
  try {
    const res = await systemApi.getRoleList()
    state.roleList = res.data || []
  } catch (e) {
    console.error('获取角色列表失败', e)
  }
}

const initData = async () => {
  state.loading = true
  try {
    const res = await systemApi.getAdminList({
      page: state.pagination.current,
      size: state.pagination.pageSize,
      keyword: state.searchForm.keyword || undefined,
      status: state.searchForm.status
    })
    state.tableData = res.data || []
    if (res.result) {
      state.pagination.total = res.result.total || 0
    }
  } catch (e) {
    console.error('获取管理员列表失败', e)
  } finally {
    state.loading = false
  }
}

const handleSearch = () => {
  state.pagination.current = 1
  initData()
}

const handleReset = () => {
  state.searchForm = { keyword: '', status: null }
  state.pagination.current = 1
  initData()
}

const handlePageChange = (page) => {
  state.pagination.current = page
  initData()
}

const handleAdd = () => {
  state.modalType = 'add'
  state.formData = { id: null, account: '', realName: '', phone: '', roleIds: [], status: 1 }
  state.modalVisible = true
}

const handleEdit = async (row) => {
  state.modalType = 'edit'
  try {
    const res = await systemApi.getAdminDetail ? systemApi.getAdminDetail(row.id) : null
    const detail = res?.data || row
    state.formData = {
      id: detail.id,
      account: detail.account || '',
      realName: detail.realName || '',
      phone: detail.phone || '',
      roleIds: Array.isArray(detail.roleIds) ? [...detail.roleIds] : [],
      status: detail.status ?? 1
    }
    state.modalVisible = true
  } catch (e) {
    // 回退：直接用行数据
    state.formData = { id: row.id, account: row.account || '', realName: row.realName || '', phone: row.phone || '', roleIds: Array.isArray(row.roleIds) ? [...row.roleIds] : [], status: row.status ?? 1 }
    state.modalVisible = true
  }
}

const handleSubmit = async () => {
  if (!state.formData.account) {
    Message.warning('请输入账号')
    return
  }
  if (!state.formData.realName) {
    Message.warning('请输入真实姓名')
    return
  }
  state.submitLoading = true
  try {
    const payload = {
      id: state.formData.id,
      account: state.formData.account,
      realName: state.formData.realName,
      phone: state.formData.phone,
      roleIds: state.formData.roleIds || [],
      status: state.formData.status
    }
    if (state.modalType === 'add') {
      await systemApi.addAdmin(payload)
      Message.success('新增成功，默认密码为 123456')
    } else {
      await systemApi.editAdmin(payload)
      Message.success('编辑成功')
    }
    state.modalVisible = false
    initData()
  } catch (e) {
    console.error('保存管理员失败', e)
  } finally {
    state.submitLoading = false
  }
}

const handleToggleStatus = async (row, v) => {
  // 直接走编辑接口更新状态
  try {
    await systemApi.editAdmin({
      id: row.id,
      account: row.account,
      realName: row.realName,
      phone: row.phone,
      roleIds: row.roleIds || [],
      status: v ? 1 : 0
    })
    row.status = v ? 1 : 0
    Message.success(`已${v ? '启用' : '停用'}`)
  } catch (e) {
    console.error('切换状态失败', e)
  }
}

const handleResetPwd = (row) => {
  Modal.confirm({
    title: '确认重置',
    content: `确定重置"${row.realName || row.account}"的密码吗？`,
    onOk: async () => {
      try {
        await systemApi.resetPassword(row.id)
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
    content: `删除管理员"${row.realName || row.account}"？`,
    onOk: async () => {
      try {
        await systemApi.deleteAdmin(row.id)
        Message.success('删除成功')
        initData()
      } catch (e) {
        console.error('删除管理员失败', e)
      }
    }
  })
}

onMounted(() => {
  loadRoleList()
  initData()
})
</script>

<style lang="less" scoped>
.admin-account-page {
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

      .filter-select {
        width: 140px;
      }

      .filter-actions {
        margin-left: auto;
      }
    }
  }

  .table-container {
    background: var(--bg-color);
    border-radius: var(--border-radius-base);
    padding: var(--spacing-xl);
    box-shadow: var(--shadow-base);

    .role-badge {
      padding: 2px 8px;
      background: rgba(22, 93, 255, 0.1);
      color: #165DFF;
      border-radius: var(--border-radius-sm);
      font-size: var(--font-size-xs);
    }

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
