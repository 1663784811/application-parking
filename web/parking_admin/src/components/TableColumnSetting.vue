<template>
  <!-- 列设置：表格上方右侧。用普通 div 实现右对齐，不用绝对定位 -->
  <div class="col-setting-wrap">
    <Poptip
      :modelValue="open"
      @update:modelValue="$emit('update:open', $event)"
      trigger="click"
      placement="bottom-end"
      transfer
      width="180"
    >
      <Button size="small">
        <Icon type="ios-options-outline" />
        列设置
      </Button>
      <template #content>
        <div class="col-setting-panel">
          <CheckboxGroup
            :modelValue="visible"
            @update:modelValue="$emit('update:visible', $event)"
            vertical
          >
            <Checkbox
              v-for="col in toggleableColumns"
              :key="col.field"
              :label="col.field"
            >
              {{ col.title }}
            </Checkbox>
          </CheckboxGroup>
          <div class="col-setting-footer">
            <Button size="small" @click="$emit('reset')">重置</Button>
            <span class="col-setting-tip">已选 {{ visible.length }}/{{ toggleableColumns.length }}</span>
          </div>
        </div>
      </template>
    </Poptip>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Poptip, Checkbox, CheckboxGroup, Button, Icon } from 'view-ui-plus'

const props = defineProps({
  // 完整列定义（含固定列）；组件内部按 field 过滤出可控制项
  columns: { type: Array, required: true },
  // 当前显示的 field 集合，v-model:visible
  visible: { type: Array, default: () => [] },
  // Poptip 显隐，v-model:open
  open: { type: Boolean, default: false }
})

defineEmits(['update:visible', 'update:open', 'reset'])

const toggleableColumns = computed(() => props.columns.filter(c => c.field))
</script>

<!-- Poptip transfer 后面板挂到 body，scoped 样式无法命中；wrap 也放此非 scoped 块，类名前缀隔离防冲突 -->
<style lang="less">
.col-setting-wrap {
  display: flex;
  justify-content: flex-end;
  margin-bottom: var(--spacing-md);
}

.col-setting-panel {
  padding: var(--spacing-sm) 0;

  .ivu-checkbox-group {
    display: flex;
    flex-direction: column;
    gap: var(--spacing-xs);

    .ivu-checkbox-wrapper {
      margin-right: 0;
    }
  }

  .col-setting-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: var(--spacing-sm);
    padding-top: var(--spacing-sm);
    border-top: 1px solid var(--border-color-split);

    .col-setting-tip {
      font-size: 12px;
      color: var(--text-color-secondary);
    }
  }
}
</style>
