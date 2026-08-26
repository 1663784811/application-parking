import { ref, computed, watch, unref } from 'vue'

/**
 * 表格列显隐控制（列设置）
 *
 * 约定：列对象带 `field` 属性才可被「列设置」控制显隐；
 *      type=selection/index、操作列等不带 field，始终显示，避免用户把操作列藏掉后无法恢复。
 *      field 取值应在该表内唯一且稳定（通常 field = key；slot 列 field = slot 名；
 *      若两列 key 相同，需手动合成不同的 field，如 parkingList 的 'capacity' 与 'remaining'）。
 *
 * @param columns      列定义数组，或返回列数组的 ref/computed/函数（支持按 tab 切换列的场景）
 * @param storageKey   localStorage 持久化键（每页一个，避免互相覆盖）
 */
export function useTableColumns(columns, storageKey) {
  const getCols = () => {
    if (typeof columns === 'function') return columns()
    return unref(columns)
  }

  // 可控制显隐的列（带 field）
  const toggleableColumns = computed(() => getCols().filter(c => c.field))
  // 默认全部显示
  const defaultVisibleFields = computed(() => toggleableColumns.value.map(c => c.field))

  const loadVisibleFields = () => {
    try {
      const saved = JSON.parse(localStorage.getItem(storageKey) || 'null')
      if (Array.isArray(saved) && saved.length) return saved
    } catch (e) {
      console.error('读取列设置失败', e)
    }
    return [...defaultVisibleFields.value]
  }

  const visibleFields = ref(loadVisibleFields())
  const colSettingVisible = ref(false)

  // 当前显示的列：固定列始终保留，其余按 visibleFields 过滤
  const displayColumns = computed(() =>
    getCols().filter(c => !c.field || visibleFields.value.includes(c.field))
  )

  // 列设置变更后持久化
  watch(visibleFields, (val) => {
    try {
      localStorage.setItem(storageKey, JSON.stringify(val))
    } catch (e) {
      console.error('保存列设置失败', e)
    }
  }, { deep: true })

  // 重置为默认（全部显示）
  const resetColumns = () => {
    visibleFields.value = [...defaultVisibleFields.value]
  }

  return { toggleableColumns, visibleFields, colSettingVisible, displayColumns, resetColumns }
}
