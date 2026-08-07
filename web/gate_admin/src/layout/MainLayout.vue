<template>
  <div class="layout-wrapper">
    <!-- 头部 -->
    <LayoutHeader/>
    <!-- 主体区域 -->
    <div class="layout-main">
      <router-view v-slot="{ Component }">
        <Transition name="page-fade" mode="out-in">
          <keep-alive>
            <component :is="Component"/>
          </keep-alive>
        </Transition>
      </router-view>
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
  overflow: hidden;
  background: var(--color-bg);

  .layout-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: auto;
    position: relative;
  }
}

// 页面切换过渡动画
.page-fade-enter-active {
  transition: all 0.25s ease-out;
}
.page-fade-leave-active {
  transition: all 0.15s ease-in;
}
.page-fade-enter-from {
  opacity: 0;
  transform: translateY(8px);
}
.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
