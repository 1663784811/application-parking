<template>
  <router-view/>
</template>

<script setup>
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html,
body {
  width: 100%;
  height: 100%;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  font-size: 14px;
  color: var(--color-body, #4E5969);
  background: var(--color-bg, #F2F3F5);
  line-height: 1.5;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 全局滚动条 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: var(--color-border, #E5E6EB);
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background: var(--color-text-secondary, #86909C);
}

/* 选中文本 */
::selection {
  background: rgba(22, 93, 255, 0.15);
  color: var(--color-title, #1D2129);
}
</style>