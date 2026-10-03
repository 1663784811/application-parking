<template>
  <div class="plate-input" @click="show = true">
    <div class="plate-cells">
      <!-- 省份前缀 -->
      <span class="plate-cell plate-cell--prefix">{{ prefix }}</span>
      <!-- 7 个号牌位：常规车牌用到第 6 位，新能源车第 7 位补“新”；
           第 7 位空着就留浅色“新”作提示（含已输满 6 位时），别留一个说不清的灰框 -->
      <span
        v-for="(ch, i) in slots"
        :key="i"
        class="plate-cell"
        :class="{ 'is-active': i === number.length && number.length < 7 }"
      >
        <span v-if="ch">{{ ch }}</span>
        <span v-else-if="i === 6" class="plate-hint">新</span>
      </span>
    </div>

    <PlateKeyboard
      v-model:show="show"
      :prefix="prefix"
      :number="number"
      @update:prefix="emit('update:prefix', $event)"
      @update:number="emit('update:number', $event)"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import PlateKeyboard from './PlateKeyboard.vue'

const props = defineProps({
  // 省份前缀（如 '京'）
  prefix: { type: String, default: '' },
  // 车牌号主体（前缀之后的部分，最多 7 位）
  number: { type: String, default: '' },
})

const emit = defineEmits(['update:prefix', 'update:number'])

// 键盘显隐，组件内部自管理
const show = ref(false)

// 7 个号牌位，按 number 逐位填充
const slots = computed(() =>
  Array.from({ length: 7 }, (_, i) => props.number[i] || '')
)
</script>

<style scoped lang="less">
.plate-input {
  width: 100%;

  .plate-cells {
    display: flex;
    align-items: center;
    gap: 6px;
    width: 100%;
  }

  .plate-cell {
    flex: 1;
    height: 44px;
    min-width: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    font-weight: 600;
    font-family: 'PingFang SC', sans-serif;
    color: var(--text-primary);
    background: var(--bg-primary);
    border: 1px solid var(--border-primary);
    border-radius: var(--radius-sm);

    // 省份前缀：主色底，与后面的字符格拉开区分
    &.plate-cell--prefix {
      color: var(--on-brand-primary);
      background: var(--brand-primary);
      border-color: var(--brand-primary);
    }

    // 第 7 位占位提示“新”：比已输字符更淡，看得出是提示不是已输入
    .plate-hint {
      color: var(--text-disabled);
      opacity: 0.55;
      font-weight: 400;
    }

    // 下一个待输入位：主色描边加粗，不用 box-shadow 撑宽，避免格子宽度抖动
    &.is-active {
      border-color: var(--brand-primary);
      box-shadow: inset 0 0 0 1px var(--brand-primary);
    }
  }
}
</style>
