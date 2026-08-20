<template>
  <div class="main-layout">
    <div class="content">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const state = reactive({})
</script>

<style scoped lang="less">
.main-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;

  .content {
    flex: 1;
    overflow: auto;
  }
}
</style>
