<template>
  <!-- 右上：车辆通行图片 -->
  <div class="card">
    <div class="card-header">
      <h3><i class="fas fa-camera"></i> 车辆通行图片</h3>
      <span class="status-badge"><i class="fas fa-images"></i> 实时抓拍</span>
    </div>
    <div class="image-grid">
      <div class="image-box" v-for="img in state.images" :key="img.id">
        <i class="fas fa-image"></i>
        <span>{{ img.label }}</span>
        <span class="plate-overlay"><i class="fas fa-car"></i> {{ img.plate }}</span>
        <span class="img-label">{{ img.time }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import {reactive} from 'vue'

const state = reactive({
  // 车辆通行抓拍图片
  images: [
    {id: 1, label: '进场 抓拍1', plate: '粤A·8K92F', time: '14:28:15'},
    {id: 2, label: '进场 抓拍2', plate: '粤A·8K92F', time: '14:28:16'},
    {id: 3, label: '出场 抓拍1', plate: '粤A·8K92F', time: '14:35:42'},
    {id: 4, label: '出场 抓拍2', plate: '粤A·8K92F', time: '14:35:43'}
  ]
})
</script>

<style lang="less" scoped>
// ============================================
// 卡片通用样式
// ============================================
.card {
  background: #ffffff;
  border-radius: 24px;
  padding: 20px 22px;
  box-shadow: 0 8px 20px -8px rgba(0, 20, 30, 0.08);
  display: flex;
  flex-direction: column;

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
    border-bottom: 1px solid #ecf1f7;
    padding-bottom: 12px;

    h3 {
      font-weight: 600;
      font-size: 18px;
      color: #1c3b57;
      display: flex;
      align-items: center;
      gap: 8px;

      i {
        color: #1f6e9c;
        font-size: 16px;
        width: 24px;
      }
    }

    .status-badge {
      font-size: 12px;
      background: #e7edf5;
      padding: 4px 12px;
      border-radius: 40px;
      color: #2a4d6e;
    }
  }
}

// ============================================
// 车辆通行图片模块
// ============================================
.image-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;

  .image-box {
    background: #f2f7fd;
    border-radius: 16px;
    aspect-ratio: 16/9;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border: 1px solid #e0e9f2;
    color: #3d6a8c;
    font-size: 13px;
    font-weight: 500;
    gap: 6px;
    position: relative;
    overflow: hidden;

    i {
      font-size: 35px;
      opacity: 0.3;
      color: #1f6e9c;
    }

    .img-label {
      background: rgba(0, 0, 0, 0.5);
      color: white;
      padding: 2px 12px;
      border-radius: 40px;
      font-size: 10px;
      position: absolute;
      bottom: 6px;
      right: 6px;
      backdrop-filter: blur(2px);
    }

    .plate-overlay {
      background: #0b2b44cc;
      color: white;
      padding: 2px 12px;
      border-radius: 40px;
      font-size: 11px;
      font-weight: 600;
      position: absolute;
      top: 6px;
      left: 6px;
    }
  }
}
</style>
