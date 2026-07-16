<template>
  <div class="charge-rule-config-page">
    <div class="config-section">
      <h3 class="section-title">临时车收费规则</h3>
      <div class="rule-form">
        <Form :model="state.tempRule" :label-width="150">
          <FormItem label="首小时费用"><InputNumber v-model="state.tempRule.firstHour" :min="0" :precision="2" /> <span class="unit">元</span></FormItem>
          <FormItem label="超时费用"><InputNumber v-model="state.tempRule.hourlyRate" :min="0" :precision="2" /> <span class="unit">元/小时</span></FormItem>
          <FormItem label="24小时封顶"><InputNumber v-model="state.tempRule.dailyCap" :min="0" :precision="2" /> <span class="unit">元</span></FormItem>
          <FormItem label="免费时长"><InputNumber v-model="state.tempRule.freeMinutes" :min="0" /> <span class="unit">分钟</span></FormItem>
        </Form>
      </div>
    </div>
    <div class="config-section">
      <h3 class="section-title">固定车套餐规则</h3>
      <div class="rule-form">
        <Form :model="state.fixedRule" :label-width="150">
          <FormItem label="月卡价格"><InputNumber v-model="state.fixedRule.monthlyPrice" :min="0" :precision="2" /> <span class="unit">元/月</span></FormItem>
          <FormItem label="季卡价格"><InputNumber v-model="state.fixedRule.quarterlyPrice" :min="0" :precision="2" /> <span class="unit">元/季</span></FormItem>
          <FormItem label="年卡价格"><InputNumber v-model="state.fixedRule.yearlyPrice" :min="0" :precision="2" /> <span class="unit">元/年</span></FormItem>
          <FormItem label="免费出场"><Checkbox v-model="state.fixedRule.freeExit">固定车免费出场</Checkbox></FormItem>
        </Form>
      </div>
    </div>
    <div class="config-section">
      <h3 class="section-title">优惠规则</h3>
      <div class="rule-form">
        <Form :label-width="150">
          <FormItem label="节假日折扣"><InputNumber v-model="state.discountRule.holidayDiscount" :min="0" :max="10" :precision="1" /> <span class="unit">折</span><span class="tip">设置为10表示不打折</span></FormItem>
          <FormItem label="优惠券上限"><InputNumber v-model="state.discountRule.couponMaxAmount" :min="0" :precision="2" /> <span class="unit">元</span><span class="tip">单次最多抵扣金额</span></FormItem>
        </Form>
      </div>
    </div>
    <div class="config-actions"><Button type="primary" size="large" @click="handleSave">保存配置</Button></div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { Form, FormItem, InputNumber, Checkbox, Button, Message } from 'view-ui-plus'

const state = reactive({
  tempRule: { firstHour: 5, hourlyRate: 3, dailyCap: 50, freeMinutes: 15 },
  fixedRule: { monthlyPrice: 300, quarterlyPrice: 800, yearlyPrice: 2800, freeExit: true },
  discountRule: { holidayDiscount: 8, couponMaxAmount: 10 }
})

const handleSave = () => Message.success('收费规则保存成功')
</script>

<style lang="less" scoped>
.charge-rule-config-page { max-width: 800px; .config-section { background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); padding: var(--spacing-xl); margin-bottom: var(--spacing-lg); .section-title { font-size: var(--font-size-md); font-weight: 600; margin-bottom: var(--spacing-xl); padding-bottom: var(--spacing-md); border-bottom: 1px solid var(--border-color); } .rule-form { .unit { margin-left: var(--spacing-sm); color: var(--text-color-secondary); } .tip { margin-left: var(--spacing-md); font-size: var(--font-size-xs); color: var(--text-color-secondary); } } } .config-actions { display: flex; justify-content: center; } }
</style>