<template>
  <div class="layout-wrapper">
    <!-- 侧边栏 -->
    <LayoutSider/>
    <!-- 主体区域 -->
    <div class="layout-main">
      <!-- 头部 -->
      <LayoutHeader/>
      <!-- 内容区域 -->
      <div class="layout-content">
        <!-- 停车场列表侧栏 -->
        <ParkingSidebar v-if="showParkingSidebar" />
        <!-- 主内容区 -->
        <div class="mainContent">
          <router-view/>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import LayoutHeader from './components/Header.vue'
import LayoutSider from './components/Sider.vue'
import ParkingSidebar from './components/ParkingSidebar.vue'

const route = useRoute()

const showParkingSidebar = computed(() => {
  return route.meta.showParkingSidebar === true
})
</script>

<style lang="less" scoped>
.layout-wrapper {
  display: flex;
  width: 100%;
  height: 100%;
  overflow: auto;

  .layout-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: auto;

    .layout-content {
      flex: 1;
      display: flex;
      flex-direction: row;
      gap: 10px;
      padding: var(--spacing-md);
      overflow: auto;
      background-color: var(--bg-color-page);

      .mainContent {
        display: flex;
        flex-direction: column;
        flex: 1;
        overflow: auto;
        height: 100%;
      }
    }
  }
}

</style>
