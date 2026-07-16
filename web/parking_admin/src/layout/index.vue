<template>
  <div class="layout-wrapper">
    <!-- 侧边栏 -->
    <LayoutSider />
    <!-- 主体区域 -->
    <div class="layout-main">
      <!-- 头部 -->
      <LayoutHeader />
      <!-- 内容区域 -->
      <div class="layout-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import LayoutHeader from './components/Header.vue'
import LayoutSider from './components/Sider.vue'



</script>

<style lang="less" scoped>
.layout-wrapper {
  display: flex;
  width: 100%;
  height: 100vh;
  overflow: hidden;

  .layout-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .layout-content {
      flex: 1;
      padding: var(--spacing-md);
      overflow-y: auto;
      background-color: var(--bg-hover-color);
    }
  }
}
</style>
