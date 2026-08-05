<template>
  <div class="layout-wrapper">
    <!-- 头部 -->
    <LayoutHeader/>
    <!-- 主体区域 -->
    <div class="layout-main">
      <router-view/>
    </div>
  </div>
</template>

<script setup>
import LayoutHeader from './components/Header.vue'
</script>

<style lang="less" scoped>
.layout-wrapper {
  flex: 1;
  display: flex;
  width: 100%;
  flex-direction: column;
  overflow: auto;

  .layout-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: auto;
  }
}
</style>
