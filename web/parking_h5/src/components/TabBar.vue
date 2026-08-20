<template>
  <div v-if="showTabBar" class="tab-bar">
    <van-tabbar v-model="state.active" @change="onTabChange">
      <van-tabbar-item
        v-for="tab in tabs"
        :key="tab.name"
        :icon="tab.icon"
      >
        {{ tab.title }}
      </van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { reactive, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// tab 列表：name 与路由 name 对应
const tabs = [
  { name: 'mainIndex', title: '首页', icon: 'home-o' },
  { name: 'me', title: '我的', icon: 'user-o' },
]

const state = reactive({
  // 当前激活的 tab 索引，由路由名称计算
  active: 0,
})

// 是否显示 tab：路由 meta.showTabBar === true
const showTabBar = computed(() => route.meta.showTabBar === true)

// 根据当前路由名称计算 active
const updateActive = () => {
  const index = tabs.findIndex(tab => tab.name === route.name)
  state.active = index !== -1 ? index : state.active
}

// 监听路由变化，更新激活项
watch(() => route.name, updateActive, { immediate: true })

// 点击 tab 通过 js 路由跳转（保留 appId 参数）
const onTabChange = (index) => {
  const tab = tabs[index]
  if (tab && route.params.appId) {
    router.push({
      name: tab.name,
      params: { appId: route.params.appId },
    })
  }
}
</script>

<style scoped lang="less">
.tab-bar {
  .van-tabbar {
    // 适配底部安全区
    padding-bottom: env(safe-area-inset-bottom, 0);
  }
}
</style>
