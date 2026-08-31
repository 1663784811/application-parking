<template>
  <div class="plate-input" @click="show = true">
    <div class="plate-cells">
      <!-- 省份前缀 -->
      <span class="plate-cell plate-cell--prefix">{{ prefix }}</span>
      <!-- 7 个号牌位：0-5 常规位，6 为新能源位 -->
      <span
        v-for="(ch, i) in slots"
        :key="i"
        class="plate-cell"
        :class="{
          'is-energy': i === 6,
          'is-active': i === number.length && number.length < 7,
        }"
      >{{ ch }}</span>
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
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  padding: 16px;
  border-bottom: 1px dashed var(--border-primary);

  .plate-cells {
    display: flex;
    align-items: center;
    gap: 6px;
    width: 100%;
  }

  .plate-cell {
    flex: 1;
    height: 40px;
    min-width: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    font-weight: 600;
    font-family: 'PingFang SC', sans-serif;
    color: var(--text-primary);
    background: var(--bg-secondary);
    border: 1px solid var(--border-primary);
    border-radius: 6px;

    // 省份前缀：主色底
    &.plate-cell--prefix {
      color: var(--bg-primary);
      background: var(--brand-primary);
      border-color: var(--brand-primary);
    }

    // 新能源位：更窄、虚线、浅绿
    &.is-energy {
      flex: 0.6;
      border-style: dashed;
      background: var(--brand-primary-5);
      border-color: var(--brand-primary-4);
    }

    // 下一个待输入位高亮
    &.is-active {
      border-color: var(--brand-primary);
      box-shadow: 0 0 0 1px var(--brand-primary);
    }
  }
}
</style>
