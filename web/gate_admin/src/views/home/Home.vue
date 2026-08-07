<template>
  <!-- 使用 flex 布局，两列 -->
  <div class="dashboard-container">
    <!-- 左列 -->
    <div class="left-column">
      <!-- 1.2.1 道闸实时监控 (左上) - 摄像头模块 -->
      <GateMonitor/>

      <!-- 左下：1.2.2 车辆通行记录 (flex:1 占满剩余空间) -->
      <VehicleRecords/>
    </div>

    <!-- 右列 -->
    <div class="right-column">
      <!-- 右上：车辆通行图片 -->
      <VehicleImages/>
      <!-- 右中：车辆通行信息 -->
      <VehicleInfo/>
      <!-- 右下：1.2.5 道闸操作 -->
      <GateOperation/>
    </div>
  </div>
</template>

<script setup>
import GateMonitor from './components/GateMonitor.vue'
import VehicleRecords from './components/VehicleRecords.vue'
import VehicleImages from './components/VehicleImages.vue'
import VehicleInfo from './components/VehicleInfo.vue'
import GateOperation from './components/GateOperation.vue'
</script>

<style lang="less" scoped>
.dashboard-container {
  flex: 1;
  overflow: auto;
  display: flex;
  gap: 20px;
  padding: 20px;
  background: #eef2f7;
  flex-direction: row;

  .left-column {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 20px;
    min-width: 0;
  }

  .right-column {
    width: 700px;
    display: flex;
    flex-direction: column;
    gap: 20px;
    min-width: 0;
  }
}


</style>
