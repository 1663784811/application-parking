<template>
  <div class="app">
    <router-view />
    <TabBar />
  </div>
</template>

<script setup>
import TabBar from '@/components/TabBar.vue'
</script>

<style>
/* touch-action: manipulation 关掉浏览器的"双击放大"手势（连点两下不再缩放），
   单击响应也更快。iOS Safari 忽略 viewport 的 user-scalable=no，靠这条兜底；
   写在 html/body 上即可，touch-action 会与祖先取交集，后代元素默认被覆盖到 */
html,
body {
  touch-action: manipulation;
}

.app {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: var(--bg-secondary);
}
</style>
