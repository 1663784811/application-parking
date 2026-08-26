<template>
  <div class="sms-config-page">
    <div class="filter-bar">
      <div class="filter-actions">
        <Button type="primary" @click="handleAdd">
          <Icon type="ios-add"/>
          新增模板
        </Button>
      </div>
    </div>
    <div class="table-container">
      <TableColumnSetting :columns="columns" v-model:visible="visibleFields" v-model:open="colSettingVisible" @reset="resetColumns" />
      <Table :columns="displayColumns" :data="state.tableData" :loading="state.loading">
        <template #status="{ row }">
          <i-switch :value="row.status === 1" @on-change="v => handleToggleStatus(row, v)"/>
        </template>
        <template #action="{ row }">
          <Button type="text" size="small" @click="handleEdit(row)">编辑</Button>
          <Button type="text" size="small" @click="handleDelete(row)" class="text-danger">删除</Button>
        </template>
      </Table>
    </div>
    <Modal v-model="state.modalVisible" :title="state.modalType === 'add' ? '新增短信模板' : '编辑短信模板'"
           width="600">
      <Form :model="state.formData" :label-width="100">
        <FormItem label="模板名称"><Input v-model="state.formData.name" placeholder="如：到期提醒"/></FormItem>
        <FormItem label="模板类型">
          <Select v-model="state.formData.type">
            <Option :value="1">到期提醒</Option>
            <Option :value="2">欠费催缴</Option>
            <Option :value="3">入场通知</Option>
            <Option :value="4">出场通知</Option>
          </Select>
        </FormItem>
        <FormItem label="短信内容"><Input v-model="state.formData.content" type="textarea" :rows="4"
                                          placeholder="使用 {plate} {parking} {time} 等变量"/></FormItem>
        <FormItem label="启用状态">
          <i-switch v-model="state.formData.statusShow"/>
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
import { reactive, onMounted } from 'vue'
import { Button, Form, FormItem, Icon, Input, Message, Modal, Option, Select, Table } from 'view-ui-plus'
import { systemApi } from '@/api'
import TableColumnSetting from '@/components/TableColumnSetting.vue'
import { useTableColumns } from '@/composables/useTableColumns'

const state = reactive({
  tableData: [],
  loading: false,
  modalVisible: false,
  modalType: 'add',
  formData: { id: null, name: '', type: 1, content: '', statusShow: true }
})

const columns = [
  { field: 'name', title: '模板名称', key: 'name', minWidth: 150 },
  { field: 'typeName', title: '类型', key: 'typeName', minWidth: 120 },
  { field: 'content', title: '内容预览', key: 'content', minWidth: 200, tooltip: true },
  { field: 'status', title: '状态', slot: 'status', minWidth: 80, align: 'center' },
  { title: '操作', slot: 'action', minWidth: 120 }
]

const { visibleFields, colSettingVisible, displayColumns, resetColumns } = useTableColumns(columns, 'smsConfig:columnVisible')

const typeMap = { 1: '到期提醒', 2: '欠费催缴', 3: '入场通知', 4: '出场通知' }

// 短信模板列表（全部，按创建时间倒序）
const loadData = async () => {
  state.loading = true
  try {
    const res = await systemApi.getSmsTemplateList()
    state.tableData = (res.data || []).map(r => ({ ...r, typeName: typeMap[r.type] || r.type }))
  } catch (e) {
    console.error('获取短信模板列表失败', e)
  } finally {
    state.loading = false
  }
}

const handleAdd = () => {
  state.modalType = 'add'
  state.formData = { id: null, name: '', type: 1, content: '', statusShow: true }
  state.modalVisible = true
}

const handleEdit = r => {
  state.modalType = 'edit'
  state.formData = { ...r, statusShow: r.status === 1 }
  state.modalVisible = true
}

const handleSubmit = async () => {
  if (!state.formData.name) {
    Message.warning('请输入模板名称')
    return
  }
  try {
    const payload = { ...state.formData, status: state.formData.statusShow ? 1 : 0 }
    await systemApi.saveSmsTemplate(payload)
    Message.success(state.modalType === 'add' ? '新增成功' : '编辑成功')
    state.modalVisible = false
    loadData()
  } catch (e) {
    console.error('保存短信模板失败', e)
  }
}

const handleToggleStatus = async (r, v) => {
  try {
    await systemApi.saveSmsTemplate({ ...r, status: v ? 1 : 0 })
    Message.success(`已${v ? '启用' : '停用'}`)
    loadData()
  } catch (e) {
    console.error('切换短信模板状态失败', e)
  }
}

const handleDelete = r => Modal.confirm({
  title: '确认删除',
  content: `删除模板"${r.name}"？`,
  onOk: async () => {
    try {
      await systemApi.deleteSmsTemplate(r.id)
      Message.success('删除成功')
      loadData()
    } catch (e) {
      console.error('删除短信模板失败', e)
    }
  }
})

onMounted(() => {
  loadData()
})
</script>

<style lang="less" scoped>
.sms-config-page {
  .filter-bar {
    padding: var(--spacing-xl);
    background: var(--bg-color);
    border-radius: var(--border-radius-base);
    margin-bottom: var(--spacing-lg);
    box-shadow: var(--shadow-base);
  }

  .table-container {
    background: var(--bg-color);
    border-radius: var(--border-radius-base);
    padding: var(--spacing-xl);
    box-shadow: var(--shadow-base);

    .text-danger {
      color: var(--error-color);
    }
  }
}
</style>