<template>
  <div class="sms-config-page">
    <div class="filter-bar">
      <div class="filter-actions"><Button type="primary" @click="handleAdd"><Icon type="ios-add" />新增模板</Button></div>
    </div>
    <div class="table-container">
      <Table :columns="columns" :data="state.tableData" :loading="state.loading">
        <template #status="{ row }"><i-switch :value="row.status === 1" @on-change="v => handleToggleStatus(row, v)" /></template>
        <template #action="{ row }"><Button type="text" size="small" @click="handleEdit(row)">编辑</Button><Button type="text" size="small" @click="handleDelete(row)" class="text-danger">删除</Button></template>
      </Table>
    </div>
    <Modal v-model="state.modalVisible" :title="state.modalType === 'add' ? '新增短信模板' : '编辑短信模板'" width="600">
      <Form :model="state.formData" :label-width="100">
        <FormItem label="模板名称"><Input v-model="state.formData.name" placeholder="如：到期提醒" /></FormItem>
        <FormItem label="模板类型">
          <Select v-model="state.formData.type"><Option :value="1">到期提醒</Option><Option :value="2">欠费催缴</Option><Option :value="3">入场通知</Option><Option :value="4">出场通知</Option></Select>
        </FormItem>
        <FormItem label="短信内容"><Input v-model="state.formData.content" type="textarea" :rows="4" placeholder="使用 {plate} {parking} {time} 等变量" /></FormItem>
        <FormItem label="启用状态"><i-switch v-model="state.formData.statusShow" /></FormItem>
      </Form>
      <template #footer><Button @click="state.modalVisible = false">取消</Button><Button type="primary" @click="handleSubmit">确定</Button></template>
    </Modal>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { Button, Icon, Table, Modal, Form, FormItem, Input, Select, Option, Switch, Message } from 'view-ui-plus'

const state = reactive({
  tableData: [],
  loading: false,
  modalVisible: false,
  modalType: 'add',
  formData: { id: null, name: '', type: 1, content: '', statusShow: true }
})

const columns = [
  { title: '模板名称', key: 'name', width: 150 },
  { title: '类型', key: 'typeName', width: 120 },
  { title: '内容预览', key: 'content', minWidth: 200, tooltip: true },
  { title: '状态', slot: 'status', width: 80, align: 'center' },
  { title: '操作', slot: 'action', width: 120 }
]

const typeMap = { 1: '到期提醒', 2: '欠费催缴', 3: '入场通知', 4: '出场通知' }

const initData = () => {
  state.loading = true
  setTimeout(() => {
    state.tableData = [
      { id: 1, name: '月卡到期提醒', type: 1, typeName: '到期提醒', content: '尊敬的{plate}车主，您的月卡将于{time}到期，请及时续费。', status: 1 },
      { id: 2, name: '停车欠费通知', type: 2, typeName: '欠费催缴', content: '您的车辆{plate}在{parking}产生停车费{amount}元，请及时缴纳。', status: 1 },
      { id: 3, name: '入场通知', type: 3, typeName: '入场通知', content: '您的车辆{plate}已于{time}入场{parking}，祝您停车愉快。', status: 1 },
      { id: 4, name: '出场通知', type: 4, typeName: '出场通知', content: '您的车辆{plate}已于{time}出场，停车费{amount}元。', status: 0 }
    ]
    state.loading = false
  }, 500)
}

const handleAdd = () => { state.modalType = 'add'; state.formData = { id: null, name: '', type: 1, content: '', statusShow: true }; state.modalVisible = true }
const handleEdit = r => { state.modalType = 'edit'; state.formData = { ...r, statusShow: r.status === 1 }; state.modalVisible = true }
const handleSubmit = () => { Message.success(state.modalType === 'add' ? '新增成功' : '编辑成功'); state.modalVisible = false; initData() }
const handleToggleStatus = (r, v) => Message.success(`已${v ? '启用' : '停用'}`)
const handleDelete = r => Modal.confirm({ title: '确认删除', content: `删除模板"${r.name}"？`, onOk: () => { Message.success('删除成功'); initData() } })

initData()
</script>

<style lang="less" scoped>
.sms-config-page { .filter-bar { padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); margin-bottom: var(--spacing-lg); box-shadow: var(--shadow-base); } .table-container { background: var(--bg-color); border-radius: var(--border-radius-base); padding: var(--spacing-xl); box-shadow: var(--shadow-base); .text-danger { color: var(--error-color); } } }
</style>