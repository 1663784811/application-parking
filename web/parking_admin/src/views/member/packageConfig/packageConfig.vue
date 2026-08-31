<template>
  <div class="package-config-page">
    <div class="filter-bar">
      <div class="filter-actions">
        <Button type="primary" @click="handleAdd"><Icon type="ios-add" />新增套餐</Button>
      </div>
    </div>
    <div class="package-grid">
      <div v-for="item in state.tableData" :key="item.id" class="package-card">
        <div class="package-header">
          <span class="package-name">{{ item.name }}</span>
          <span class="package-type" :class="'type-' + item.type">{{ getTypeText(item.type) }}</span>
        </div>
        <div class="package-price">¥<span class="price-value">{{ item.price }}</span>/{{ getUnitText(item.type) }}</div>
        <div class="package-features">
          <div class="feature-item"><Icon type="ios-checkmark-circle" />有效期{{ item.validDays }}天</div>
          <div class="feature-item"><Icon type="ios-checkmark-circle" />适用停车场：{{ getParkingNames(item.parkingIds) }}</div>
          <div class="feature-item"><Icon type="ios-checkmark-circle" />免费出场</div>
        </div>
        <div class="package-footer">
          <span class="package-sales">已售 {{ item.sales }}</span>
          <div class="package-actions">
            <Button type="text" size="small" @click="handleEdit(item)">编辑</Button>
            <Button type="text" size="small" @click="handleDelete(item)" class="text-danger">删除</Button>
          </div>
        </div>
      </div>
    </div>
    <Modal v-model="state.modalVisible" :title="state.modalType === 'add' ? '新增套餐' : '编辑套餐'" width="1000">
      <Form :model="state.formData" :rules="state.rules" :label-width="100" class="modal-form-2col">
        <FormItem label="套餐名称" prop="name"><Input v-model="state.formData.name" placeholder="如：月卡套餐" /></FormItem>
        <FormItem label="套餐类型" prop="type">
          <Select v-model="state.formData.type">
            <Option :value="1">月卡</Option><Option :value="2">季卡</Option><Option :value="3">年卡</Option>
          </Select>
        </FormItem>
        <FormItem label="有效期" prop="validDays"><InputNumber v-model="state.formData.validDays" :min="1" style="min-width: 100%" /></FormItem>
        <FormItem label="售价" prop="price"><InputNumber v-model="state.formData.price" :min="0" :precision="2" style="min-width: 100%" /></FormItem>
        <FormItem label="适用停车场" prop="parkingIds">
          <Select v-model="state.parkingIdList" multiple filterable placeholder="不选则适用全部停车场" class="parking-select">
            <Option v-for="item in state.parkingList" :key="item.id" :value="item.id">{{ item.name }}</Option>
          </Select>
        </FormItem>
      </Form>
      <template #footer><Button @click="state.modalVisible = false">取消</Button><Button type="primary" @click="handleSubmit">确定</Button></template>
    </Modal>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { Button, Icon, Modal, Form, FormItem, Input, InputNumber, Select, Option, Message } from 'view-ui-plus'
import { memberApi, parkingApi } from '@/api'

const state = reactive({
  tableData: [],
  // 停车场列表（供表单多选）
  parkingList: [],
  // 表单中选中的停车场ID数组，提交时与 parkingIds 字符串互转
  parkingIdList: [],
  modalVisible: false,
  modalType: 'add',
  formData: { id: null, name: '', type: 1, validDays: 30, price: 300, parkingIds: null },
  rules: { name: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }] }
})

const getTypeText = (t) => ({ 1: '月卡', 2: '季卡', 3: '年卡' }[t] || t)
const getUnitText = (t) => ({ 1: '月', 2: '季', 3: '年' }[t] || '')

// 加载停车场列表（供表单多选使用）
const loadParkingList = async () => {
  try {
    // size 取较大值以一次性加载全部停车场供选择器使用
    const res = await parkingApi.getParkingList({ size: 1000 })
    state.parkingList = res.data || []
  } catch (e) {
    console.error('获取停车场列表失败', e)
  }
}

// 套餐列表（无分页）
const initData = async () => {
  try {
    const res = await memberApi.getPackageList()
    state.tableData = res.data || []
  } catch (e) {
    console.error('获取套餐列表失败', e)
  }
}

// 将逗号分隔的停车场ID字符串解析为停车场名称（空值表示适用全部停车场）
const getParkingNames = (idsStr) => {
  if (!idsStr) return '全部停车场'
  const ids = String(idsStr).split(',').map(s => s.trim()).filter(Boolean)
  const names = ids
    .map(id => state.parkingList.find(p => String(p.id) === String(id)))
    .filter(Boolean)
    .map(p => p.name)
  return names.length ? names.join('、') : '全部停车场'
}

const handleAdd = () => {
  state.modalType = 'add'
  state.formData = { id: null, name: '', type: 1, validDays: 30, price: 300, parkingIds: null }
  state.parkingIdList = []
  state.modalVisible = true
}

const handleEdit = (r) => {
  state.modalType = 'edit'
  state.formData = { ...r }
  // parkingIds 为逗号分隔字符串，拆分为数组供多选
  state.parkingIdList = r.parkingIds ? String(r.parkingIds).split(',').map(s => s.trim()).filter(Boolean) : []
  state.modalVisible = true
}

const handleDelete = (r) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定删除套餐"${r.name}"吗？`,
    onOk: async () => {
      try {
        await memberApi.deletePackage(r.id)
        Message.success('删除成功')
        initData()
      } catch (e) {
        console.error('删除套餐失败', e)
      }
    }
  })
}

const handleSubmit = async () => {
  if (!state.formData.name) {
    Message.warning('请输入套餐名称')
    return
  }
  try {
    const payload = {
      ...state.formData,
      // 适用停车场：多选数组转逗号分隔字符串；未选则为 null
      parkingIds: state.parkingIdList.length ? state.parkingIdList.join(',') : null
    }
    await (state.modalType === 'add' ? memberApi.addPackage : memberApi.editPackage)(payload)
    Message.success(state.modalType === 'add' ? '新增成功' : '编辑成功')
    state.modalVisible = false
    initData()
  } catch (e) {
    console.error('保存套餐失败', e)
  }
}

onMounted(() => {
  loadParkingList()
  initData()
})
</script>

<style lang="less" scoped>
.package-config-page { .filter-bar { padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); margin-bottom: var(--spacing-lg); box-shadow: var(--shadow-base); } }
.package-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: var(--spacing-xl); }
.package-card { background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); padding: var(--spacing-xl); .package-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--spacing-lg); .package-name { font-size: var(--font-size-md); font-weight: 600; color: var(--text-color-title); } .package-type { padding: 2px 8px; border-radius: var(--border-radius-sm); font-size: var(--font-size-xs); &.type-1 { background: rgba(22,93,255,0.1); color: #165DFF; } &.type-2 { background: rgba(15,198,194,0.1); color: #0FC6C2; } &.type-3 { background: rgba(114,46,209,0.1); color: #722ED1; } } } .package-price { font-size: var(--font-size-sm); color: var(--text-color-secondary); margin-bottom: var(--spacing-lg); .price-value { font-size: 32px; font-weight: 600; color: var(--primary-color); } } .package-features { padding: var(--spacing-lg) 0; border-top: 1px solid var(--border-color); .feature-item { display: flex; align-items: center; font-size: var(--font-size-sm); color: var(--text-color); margin-bottom: var(--spacing-sm); .ivu-icon { color: var(--success-color); margin-right: var(--spacing-sm); } } } .package-footer { display: flex; justify-content: space-between; align-items: center; padding-top: var(--spacing-lg); border-top: 1px solid var(--border-color); .package-sales { font-size: var(--font-size-sm); color: var(--text-color-secondary); } .text-danger { color: var(--error-color); } } }
.parking-select { width: 100%; }
</style>
