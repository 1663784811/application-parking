<template>
  <Breadcrumb separator=">">
    <BreadcrumbItem v-for="item in matchedRoutes" :key="item.path">
      {{ item.meta.title }}
    </BreadcrumbItem>
  </Breadcrumb>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Breadcrumb, BreadcrumbItem } from 'view-ui-plus'

const route = useRoute()

// 过滤有 title 的路由
const matchedRoutes = computed(() => {
  return route.matched.filter((item) => item.meta && item.meta.title)
})
</script>

<style lang="less" scoped></style>