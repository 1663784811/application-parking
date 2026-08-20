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
.app {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: var(--bg-secondary);
}
</style>
