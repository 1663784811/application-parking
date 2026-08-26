<template>
  <div class="coupon-config-page">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-actions">
        <Button type="primary" @click="handleAdd">
          <Icon type="ios-add" />
          新增优惠券
        </Button>
      </div>
    </div>

    <!-- 优惠券列表 -->
    <div class="coupon-table">
      <TableColumnSetting :columns="columns" v-model:visible="visibleFields" v-model:open="colSettingVisible" @reset="resetColumns" />
      <Table :columns="displayColumns" :data="state.tableData" :loading="state.loading">
        <template #type="{ row }">
          <span class="coupon-type" :class="'type-' + row.type">{{ getTypeText(row.type) }}</span>
        </template>
        <template #status="{ row }">
          <i-switch :value="row.status === 1" @on-change="handleToggleStatus(row)" />
        </template>
        <template #action="{ row }">
          <Button type="text" size="small" @click="handleEdit(row)">编辑</Button>
          <Button type="text" size="small" @click="handleDelete(row)" class="text-danger">删除</Button>
        </template>
      </Table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <Modal v-model="state.modalVisible" :title="state.modalType === 'add' ? '新增优惠券' : '编辑优惠券'" width="500">
      <Form :model="state.formData" :rules="state.rules" :label-width="100">
        <FormItem label="优惠券名称" prop="name">
          <Input v-model="state.formData.name" placeholder="请输入优惠券名称" />
        </FormItem>
        <FormItem label="优惠券类型" prop="type">
          <Select v-model="state.formData.type" placeholder="请选择类型">
            <Option :value="1">满减券</Option>
            <Option :value="2">折扣券</Option>
            <Option :value="3">免费时长券</Option>
          </Select>
        </FormItem>
        <FormItem label="使用门槛" prop="threshold">
          <InputNumber v-model="state.formData.threshold" :min="0" style="min-width: 100%" />
          <span class="form-tip">消费满此金额即可使用，设为0则无门槛</span>
        </FormItem>
        <FormItem label="优惠金额" prop="discount">
          <InputNumber v-model="state.formData.discount" :min="0" style="min-width: 100%" />
          <span class="form-tip">满减金额、折扣率或免费时长（分钟）</span>
        </FormItem>
        <FormItem label="发放数量" prop="totalCount">
          <InputNumber v-model="state.formData.totalCount" :min="1" style="min-width: 100%" />
        </FormItem>
        <FormItem label="有效期" prop="validDays">
          <InputNumber v-model="state.formData.validDays" :min="1" style="min-width: 100%" />
          <span class="form-tip">领取后多少天内有效</span>
        </FormItem>
        <FormItem label="状态" prop="status">
          <RadioGroup v-model="state.formData.status">
            <Radio :label="1">启用</Radio>
            <Radio :label="0">停用</Radio>
          </RadioGroup>
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
import { Button, Icon, Table, Modal, Form, FormItem, Input, InputNumber, Select, Option, RadioGroup, Radio, Switch, Message } from 'view-ui-plus'
import { chargeApi } from '@/api'
import TableColumnSetting from '@/components/TableColumnSetting.vue'
import { useTableColumns } from '@/composables/useTableColumns'

const state = reactive({
  tableData: [],
  loading: false,
  modalVisible: false,
  modalType: 'add',
  formData: {
    id: null,
    name: '',
    type: 1,
    threshold: 0,
    discount: 10,
    totalCount: 100,
    validDays: 30,
    status: 1
  },
  rules: {
    name: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }]
  }
})

const columns = [
  { field: 'name', title: '优惠券名称', key: 'name', minWidth: 200 },
  { field: 'type', title: '类型', slot: 'type', minWidth: 120 },
  { field: 'threshold', title: '使用门槛', key: 'threshold', minWidth: 120, render: (h, p) => h('span', p.row.threshold > 0 ? `满${p.row.threshold}元` : '无门槛') },
  { field: 'discount', title: '优惠内容', key: 'discount', minWidth: 150, render: (h, p) => h('span', getDiscountText(p.row)) },
  { field: 'totalCount', title: '发放数量', key: 'totalCount', minWidth: 100, align: 'center' },
  { field: 'usedCount', title: '已使用', key: 'usedCount', minWidth: 100, align: 'center' },
  { field: 'validDays', title: '有效期', key: 'validDays', minWidth: 100, render: (h, p) => h('span', `领取后${p.row.validDays}天`) },
  { field: 'status', title: '状态', slot: 'status', minWidth: 80, align: 'center' },
  { title: '操作', slot: 'action', minWidth: 120 }
]

const { visibleFields, colSettingVisible, displayColumns, resetColumns } = useTableColumns(columns, 'couponConfig:columnVisible')

const getTypeText = (type) => {
  return { 1: '满减券', 2: '折扣券', 3: '免费时长券' }[type] || type
}

const getDiscountText = (row) => {
  if (row.type === 1) return `减¥${row.discount}`
  if (row.type === 2) return `${row.discount}折`
  if (row.type === 3) return `免费${row.discount}分钟`
  return row.discount
}

// 优惠券列表（全部，按创建时间倒序）
const loadData = async () => {
  state.loading = true
  try {
    const res = await chargeApi.getCouponList()
    state.tableData = res.data || []
  } catch (e) {
    console.error('获取优惠券列表失败', e)
  } finally {
    state.loading = false
  }
}

const handleAdd = () => {
  state.modalType = 'add'
  state.formData = { id: null, name: '', type: 1, threshold: 0, discount: 10, totalCount: 100, validDays: 30, status: 1 }
  state.modalVisible = true
}

const handleEdit = (row) => {
  state.modalType = 'edit'
  state.formData = { ...row }
  state.modalVisible = true
}

const handleDelete = (row) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除优惠券"${row.name}"吗？`,
    onOk: async () => {
      try {
        await chargeApi.deleteCoupon(row.id)
        Message.success('删除成功')
        loadData()
      } catch (e) {
        console.error('删除优惠券失败', e)
      }
    }
  })
}

const handleToggleStatus = async (row) => {
  try {
    await chargeApi.editCoupon({ ...row, status: row.status === 1 ? 0 : 1 })
    Message.success(`已${row.status === 1 ? '停用' : '启用'}`)
    loadData()
  } catch (e) {
    console.error('切换优惠券状态失败', e)
  }
}

const handleSubmit = async () => {
  if (!state.formData.name) {
    Message.warning('请输入优惠券名称')
    return
  }
  try {
    if (state.modalType === 'add') {
      await chargeApi.addCoupon({ ...state.formData })
    } else {
      await chargeApi.editCoupon({ ...state.formData })
    }
    Message.success(state.modalType === 'add' ? '新增成功' : '编辑成功')
    state.modalVisible = false
    loadData()
  } catch (e) {
    console.error('保存优惠券失败', e)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="less" scoped>
.coupon-config-page {
  .filter-bar {
    padding: var(--spacing-xl);
    background-color: var(--bg-color);
    border-radius: var(--border-radius-base);
    margin-bottom: var(--spacing-lg);
    box-shadow: var(--shadow-base);
  }

  .coupon-table {
    background-color: var(--bg-color);
    border-radius: var(--border-radius-base);
    padding: var(--spacing-xl);
    box-shadow: var(--shadow-base);

    .coupon-type {
      padding: 2px 8px;
      border-radius: var(--border-radius-sm);
      font-size: var(--font-size-xs);

      &.type-1 { background: rgba(22, 93, 255, 0.1); color: #165DFF; }
      &.type-2 { background: rgba(0, 180, 42, 0.1); color: #00B42A; }
      &.type-3 { background: rgba(255, 125, 0, 0.1); color: #FF7D00; }
    }

    .text-danger { color: var(--error-color); }
  }

  .form-tip {
    display: block;
    font-size: var(--font-size-xs);
    color: var(--text-color-secondary);
    margin-top: 4px;
  }
}
</style>