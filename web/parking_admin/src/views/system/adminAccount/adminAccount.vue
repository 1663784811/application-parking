<template>
  <div class="admin-account-page">
    <div class="filter-bar">
      <div class="filter-actions">
        <Button type="primary" @click="handleAdd"><Icon type="ios-add" />新增管理员</Button>
      </div>
    </div>
    <div class="table-container">
      <Table :columns="columns" :data="state.tableData" :loading="state.loading">
        <template #role="{ row }"><span class="role-badge">{{ row.roleName }}</span></template>
        <template #status="{ row }"><i-switch :value="row.status === 1" @on-change="v => handleToggleStatus(row, v)" /></template>
        <template #action="{ row }">
          <Button type="text" size="small" @click="handleEdit(row)">编辑</Button>
          <Button type="text" size="small" @click="handleResetPwd(row)">重置密码</Button>
          <Button type="text" size="small" @click="handleDelete(row)" class="text-danger" v-if="row.id !== 1">删除</Button>
        </template>
      </Table>
    </div>
    <Modal v-model="state.modalVisible" :title="state.modalType === 'add' ? '新增管理员' : '编辑管理员'" width="500">
      <Form :model="state.formData" :rules="state.rules" :label-width="100">
        <FormItem label="用户名" prop="username"><Input v-model="state.formData.username" placeholder="请输入用户名" /></FormItem>
        <FormItem label="真实姓名" prop="name"><Input v-model="state.formData.name" placeholder="请输入真实姓名" /></FormItem>
        <FormItem label="手机号" prop="phone"><Input v-model="state.formData.phone" placeholder="请输入手机号" /></FormItem>
        <FormItem label="角色" prop="roleId">
          <Select v-model="state.formData.roleId"><Option value="1">超级管理员</Option><Option value="2">财务</Option><Option value="3">车场值守</Option><Option value="4">巡检员</Option></Select>
        </FormItem>
        <FormItem label="所属车场" prop="parkings">
          <CheckboxGroup v-model="state.formData.parkings"><Checkbox label="城西停车场"></Checkbox><Checkbox label="城东停车场"></Checkbox><Checkbox label="购物中心"></Checkbox></CheckboxGroup>
        </FormItem>
        <FormItem label="状态" prop="status"><RadioGroup v-model="state.formData.status"><Radio :label="1">启用</Radio><Radio :label="0">停用</Radio></RadioGroup></FormItem>
      </Form>
      <template #footer><Button @click="state.modalVisible = false">取消</Button><Button type="primary" @click="handleSubmit">确定</Button></template>
    </Modal>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { Button, Icon, Table, Switch, Modal, Form, FormItem, Input, Select, Option, Checkbox, CheckboxGroup, RadioGroup, Radio, Message } from 'view-ui-plus'

const state = reactive({
  tableData: [],
  loading: false,
  modalVisible: false,
  modalType: 'add',
  formData: { id: null, username: '', name: '', phone: '', roleId: '', parkings: [], status: 1 },
  rules: { username: [{ required: true, message: '请输入用户名', trigger: 'blur' }], name: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }] }
})

const columns = [
  { title: 'ID', key: 'id', minWidth: 80 },
  { title: '用户名', key: 'username', minWidth: 120 },
  { title: '真实姓名', key: 'name', minWidth: 120 },
  { title: '手机号', key: 'phone', minWidth: 130 },
  { title: '角色', slot: 'role', minWidth: 120 },
  { title: '创建时间', key: 'createTime', minWidth: 160 },
  { title: '最后登录', key: 'lastLogin', minWidth: 160 },
  { title: '状态', slot: 'status', minWidth: 80, align: 'center' },
  { title: '操作', slot: 'action', minWidth: 180 }
]

const initData = () => {
  state.loading = true
  setTimeout(() => {
    state.tableData = [
      { id: 1, username: 'admin', name: '系统管理员', phone: '13800138001', roleId: 1, roleName: '超级管理员', createTime: '2024-01-01', lastLogin: '2024-01-15 14:00', status: 1 },
      { id: 2, username: 'finance', name: '财务经理', phone: '13800138002', roleId: 2, roleName: '财务', createTime: '2024-01-05', lastLogin: '2024-01-15 10:00', status: 1 },
      { id: 3, username: 'guard1', name: '张保安', phone: '13800138003', roleId: 3, roleName: '车场值守', createTime: '2024-01-10', lastLogin: '2024-01-14 22:00', status: 1 },
      { id: 4, username: 'inspector', name: '巡检员', phone: '13800138004', roleId: 4, roleName: '巡检员', createTime: '2024-01-12', lastLogin: '-', status: 0 }
    ]
    state.loading = false
  }, 500)
}

const handleAdd = () => { state.modalType = 'add'; state.formData = { id: null, username: '', name: '', phone: '', roleId: '', parkings: [], status: 1 }; state.modalVisible = true }
const handleEdit = r => { state.modalType = 'edit'; state.formData = { ...r, parkings: [] }; state.modalVisible = true }
const handleSubmit = () => { Message.success(state.modalType === 'add' ? '新增成功' : '编辑成功'); state.modalVisible = false; initData() }
const handleToggleStatus = (r, v) => Message.success(`已${v ? '启用' : '停用'}`)
const handleResetPwd = r => Modal.confirm({ title: '确认重置', content: `确定重置"${r.name}"的密码吗？`, onOk: () => Message.success('密码已重置为123456') })
const handleDelete = r => Modal.confirm({ title: '确认删除', content: `删除管理员"${r.name}"？`, onOk: () => { Message.success('删除成功'); initData() } })

initData()
</script>

<style lang="less" scoped>
.admin-account-page { .filter-bar { padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); margin-bottom: var(--spacing-lg); box-shadow: var(--shadow-base); } .table-container { background: var(--bg-color); border-radius: var(--border-radius-base); padding: var(--spacing-xl); box-shadow: var(--shadow-base); .role-badge { padding: 2px 8px; background: rgba(22,93,255,0.1); color: #165DFF; border-radius: var(--border-radius-sm); font-size: var(--font-size-xs); } .text-danger { color: var(--error-color); } } }
</style>