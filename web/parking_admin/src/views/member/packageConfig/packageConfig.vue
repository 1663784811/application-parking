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
          <div class="feature-item"><Icon type="ios-checkmark-circle" />有效期{{ item.validDays}}天</div>
          <div class="feature-item"><Icon type="ios-checkmark-circle" />适用停车场：{{ item.parkingNames }}</div>
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
    <Modal v-model="state.modalVisible" :title="state.modalType === 'add' ? '新增套餐' : '编辑套餐'" width="500">
      <Form :model="state.formData" :rules="state.rules" :label-width="100">
        <FormItem label="套餐名称" prop="name"><Input v-model="state.formData.name" placeholder="如：月卡套餐" /></FormItem>
        <FormItem label="套餐类型" prop="type">
          <Select v-model="state.formData.type">
            <Option :value="1">月卡</Option><Option :value="2">季卡</Option><Option :value="3">年卡</Option>
          </Select>
        </FormItem>
        <FormItem label="有效期" prop="validDays"><InputNumber v-model="state.formData.validDays" :min="1" style="minWidth: 100%" /></FormItem>
        <FormItem label="售价" prop="price"><InputNumber v-model="state.formData.price" :min="0" :precision="2" style="minWidth: 100%" /></FormItem>
        <FormItem label="适用停车场" prop="parkings">
          <CheckboxGroup v-model="state.formData.parkings">
            <Checkbox label="城西停车场"></Checkbox><Checkbox label="城东停车场"></Checkbox><Checkbox label="购物中心"></Checkbox>
          </CheckboxGroup>
        </FormItem>
      </Form>
      <template #footer><Button @click="state.modalVisible = false">取消</Button><Button type="primary" @click="handleSubmit">确定</Button></template>
    </Modal>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { Button, Icon, Modal, Form, FormItem, Input, InputNumber, Select, Option, Checkbox, CheckboxGroup, Message } from 'view-ui-plus'

const state = reactive({
  tableData: [],
  modalVisible: false,
  modalType: 'add',
  formData: { id: null, name: '', type: 1, validDays: 30, price: 300, parkings: [] },
  rules: { name: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }] }
})

const getTypeText = (t) => ({ 1: '月卡', 2: '季卡', 3: '年卡' }[t] || t)
const getUnitText = (t) => ({ 1: '月', 2: '季', 3: '年' }[t] || '')

const initData = () => {
  state.tableData = [
    { id: 1, name: '月卡套餐A', type: 1, validDays: 30, price: 300, parkingNames: '全部停车场', sales: 156 },
    { id: 2, name: '月卡套餐B', type: 1, validDays: 30, price: 280, parkingNames: '城西停车场', sales: 89 },
    { id: 3, name: '季卡套餐', type: 2, validDays: 90, price: 800, parkingNames: '全部停车场', sales: 45 },
    { id: 4, name: '年卡套餐', type: 3, validDays: 365, price: 2800, parkingNames: '全部停车场', sales: 28 }
  ]
}

const handleAdd = () => { state.modalType = 'add'; state.formData = { id: null, name: '', type: 1, validDays: 30, price: 300, parkings: [] }; state.modalVisible = true }
const handleEdit = (r) => { state.modalType = 'edit'; state.formData = { ...r }; state.modalVisible = true }
const handleDelete = (r) => Modal.confirm({ title: '确认删除', content: `确定删除套餐"${r.name}"吗？`, onOk: () => Message.success('删除成功') })
const handleSubmit = () => { Message.success(state.modalType === 'add' ? '新增成功' : '编辑成功'); state.modalVisible = false; initData() }

initData()
</script>

<style lang="less" scoped>
.package-config-page { .filter-bar { padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); margin-bottom: var(--spacing-lg); box-shadow: var(--shadow-base); } }
.package-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: var(--spacing-xl); }
.package-card { background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); padding: var(--spacing-xl); .package-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--spacing-lg); .package-name { font-size: var(--font-size-md); font-weight: 600; color: var(--text-color-title); } .package-type { padding: 2px 8px; border-radius: var(--border-radius-sm); font-size: var(--font-size-xs); &.type-1 { background: rgba(22,93,255,0.1); color: #165DFF; } &.type-2 { background: rgba(15,198,194,0.1); color: #0FC6C2; } &.type-3 { background: rgba(114,46,209,0.1); color: #722ED1; } } } .package-price { font-size: var(--font-size-sm); color: var(--text-color-secondary); margin-bottom: var(--spacing-lg); .price-value { font-size: 32px; font-weight: 600; color: var(--primary-color); } } .package-features { padding: var(--spacing-lg) 0; border-top: 1px solid var(--border-color); .feature-item { display: flex; align-items: center; font-size: var(--font-size-sm); color: var(--text-color); margin-bottom: var(--spacing-sm); .ivu-icon { color: var(--success-color); margin-right: var(--spacing-sm); } } } .package-footer { display: flex; justify-content: space-between; align-items: center; padding-top: var(--spacing-lg); border-top: 1px solid var(--border-color); .package-sales { font-size: var(--font-size-sm); color: var(--text-color-secondary); } .text-danger { color: var(--error-color); } } }
</style>