<template>
  <teleport to="body">
    <Transition name="pk-fade">
      <div v-if="show" class="pk-mask" @click="close" />
    </Transition>
    <Transition name="pk-slide">
      <div v-if="show" class="pk-panel">
        <div class="pk-header">
          <div class="pk-tabs">
            <button
              class="pk-tab"
              :class="{ active: tab === 'province' }"
              @click="tab = 'province'"
            >
              省份
            </button>
            <button
              class="pk-tab"
              :class="{ active: tab === 'alnum' }"
              @click="tab = 'alnum'"
            >
              字母数字
            </button>
          </div>
          <button class="pk-done" @click="onConfirm">完成</button>
        </div>

        <!-- 省份面板 -->
        <div v-if="tab === 'province'" class="pk-grid">
          <button
            v-for="ch in provinces"
            :key="ch"
            class="pk-key"
            :class="{ selected: prefix === ch }"
            @click="onProvince(ch)"
          >
            {{ ch }}
          </button>
        </div>

        <!-- 字母数字面板 -->
        <div v-else class="pk-alnum">
          <div class="pk-row">
            <button
              v-for="d in digits"
              :key="'d' + d"
              class="pk-key"
              :disabled="needLetter || atMax"
              @click="onInput(d)"
            >
              {{ d }}
            </button>
          </div>
          <div class="pk-row">
            <button
              v-for="l in row1"
              :key="'l' + l"
              class="pk-key"
              :disabled="atMax"
              @click="onInput(l)"
            >
              {{ l }}
            </button>
          </div>
          <div class="pk-row">
            <button
              v-for="l in row2"
              :key="'l' + l"
              class="pk-key"
              :disabled="atMax"
              @click="onInput(l)"
            >
              {{ l }}
            </button>
          </div>
          <div class="pk-row">
            <button
              v-for="l in row3"
              :key="'l' + l"
              class="pk-key"
              :disabled="atMax"
              @click="onInput(l)"
            >
              {{ l }}
            </button>
            <button
              class="pk-key pk-back"
              :disabled="!number"
              @click="onBackspace"
            >
              ⌫
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </teleport>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  show: { type: Boolean, default: false },
  // 省份前缀（如 '京'）
  prefix: { type: String, default: '' },
  // 车牌号主体（前缀之后的部分）
  number: { type: String, default: '' },
})

const emit = defineEmits(['update:show', 'update:prefix', 'update:number', 'confirm'])

// 当前面板：province 省份 | alnum 字母数字
const tab = ref('province')

// 打开时根据是否已有前缀决定默认面板
watch(() => props.show, (v) => {
  if (v) tab.value = props.prefix ? 'alnum' : 'province'
}, { immediate: true })

// 31 省份 + 港澳台
const provinces = '京津沪渝冀晋辽吉黑苏浙皖闽赣鲁豫鄂湘粤桂琼川贵云陕甘青蒙宁新藏港澳台'.split('')
// QWERTY 排布，剔除易混的 I、O
const digits = '0123456789'.split('')
const row1 = 'QWERTYUP'.split('')
const row2 = 'ASDFGHJKL'.split('')
const row3 = 'ZXCVBNM'.split('')

// 号牌主体最长 7 位（新能源 8 位 = 前缀 + 7）
const MAX = 7
// 第二位（主体首位）必须是字母：此时禁用数字
const needLetter = computed(() => props.number.length === 0)
const atMax = computed(() => props.number.length >= MAX)

const onProvince = (ch) => {
  emit('update:prefix', ch)
  // 选完省份后切到字母数字面板继续输入
  tab.value = 'alnum'
}

const onInput = (ch) => {
  if (atMax.value) return
  if (needLetter.value && /\d/.test(ch)) return
  emit('update:number', props.number + ch)
}

const onBackspace = () => {
  if (props.number.length) emit('update:number', props.number.slice(0, -1))
}

const close = () => emit('update:show', false)
const onConfirm = () => {
  emit('confirm')
  close()
}
</script>

<style scoped lang="less">
.pk-mask {
  position: fixed;
  inset: 0;
  z-index: 3000;
  background: var(--bg-mask);
}

.pk-panel {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 3001;
  --pk-gap: 6px;
  padding: 12px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom, 0));
  background: var(--bg-secondary);
  border-top: 1px solid var(--border-primary);

  .pk-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 10px;

    .pk-tabs {
      display: flex;
      gap: 8px;

      .pk-tab {
        padding: 4px 12px;
        font-size: 13px;
        color: var(--text-secondary);
        background: var(--bg-tertiary);
        border: none;
        border-radius: 14px;

        &.active {
          color: var(--bg-primary);
          background: var(--brand-primary);
        }
      }
    }

    .pk-done {
      padding: 4px 12px;
      font-size: 14px;
      color: var(--brand-primary);
      background: none;
      border: none;
    }
  }

  // 统一键宽：按一行 10 个键计算
  .pk-key {
    width: calc((100% - 9 * var(--pk-gap)) / 10);
    height: 44px;
    font-size: 18px;
    color: var(--text-primary);
    background: var(--bg-primary);
    border: 1px solid var(--border-primary);
    border-radius: 8px;

    &:active {
      background: var(--bg-tertiary);
    }

    &:disabled {
      color: var(--text-disabled);
      background: var(--bg-tertiary);
    }

    &.selected {
      color: var(--bg-primary);
      background: var(--brand-primary);
      border-color: var(--brand-primary);
    }
  }

  .pk-grid {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: var(--pk-gap);

    .pk-key {
      font-size: 16px;
    }
  }

  .pk-alnum {
    .pk-row {
      display: flex;
      justify-content: center;
      gap: var(--pk-gap);
      margin-bottom: var(--pk-gap);

      &:last-child {
        margin-bottom: 0;
      }
    }

    .pk-back {
      font-size: 20px;
      color: var(--brand-primary);
    }
  }
}

// 蒙层淡入淡出
.pk-fade-enter-active,
.pk-fade-leave-active {
  transition: opacity 0.25s ease;
}
.pk-fade-enter-from,
.pk-fade-leave-to {
  opacity: 0;
}

// 面板上滑
.pk-slide-enter-active,
.pk-slide-leave-active {
  transition: transform 0.25s ease;
}
.pk-slide-enter-from,
.pk-slide-leave-to {
  transform: translateY(100%);
}
</style>
