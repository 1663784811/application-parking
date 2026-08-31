<template>
  <div class="vehicle-page">
    <van-nav-bar title="我的车辆" left-arrow @click-left="router.back()" />

    <div class="vehicle-list">
      <div
        v-for="item in state.vehicleList"
        :key="item.id"
        class="vehicle-card"
      >
        <div class="vehicle-info">
          <div class="plate-box">
            <span class="plate">{{ item.plateNumber }}</span>
          </div>
          <p class="type">{{ item.vehicleType }}</p>
        </div>
        <div class="actions">
          <van-icon
            name="edit"
            size="20"
            @click="goEdit(item.id)"
          />
          <van-icon
            name="delete-o"
            size="20"
            @click="handleDelete(item.id)"
          />
        </div>
      </div>

      <van-empty v-if="state.vehicleList.length === 0" description="暂无车辆" />

      <div class="add-btn">
        <van-button
          type="primary"
          block
          round
          icon="plus"
          @click="goAdd"
        >
          添加车辆
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'

const route = useRoute()
const router = useRouter()

const state = reactive({
  // TODO: 接口获取车辆列表（getVehicleList）
  vehicleList: [
    { id: '1', plateNumber: '京A12345', vehicleType: '小型汽车', isDefault: true },
    { id: '2', plateNumber: '京B67890', vehicleType: '小型汽车', isDefault: false },
    { id: '3', plateNumber: '京C11111', vehicleType: '新能源汽车', isDefault: false },
    { id: '4', plateNumber: '京D22222', vehicleType: '小型汽车', isDefault: false },
    { id: '5', plateNumber: '京E33333', vehicleType: '大型汽车', isDefault: false },
  ],
})

const goAdd = () => {
  router.push({
    name: 'vehicleAdd',
    params: { appId: route.params.appId },
  })
}

const goEdit = (id) => {
  router.push({
    name: 'vehicleEdit',
    params: { appId: route.params.appId, vehicleId: id },
  })
}

const handleDelete = (id) => {
  showConfirmDialog({
    title: '提示',
    message: '确定要删除该车辆吗？',
  }).then(() => {
    // TODO: 调用删除车辆接口（deleteVehicle）
    state.vehicleList = state.vehicleList.filter(item => item.id !== id)
    showToast('删除成功')
  }).catch(() => {})
}
</script>

<style scoped lang="less">
.vehicle-page {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;

  .vehicle-list {
    flex: 1;
    padding: 16px;
    overflow: auto;
  }

  .vehicle-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    margin-bottom: 12px;
    background: var(--bg-primary);
    border-radius: 12px;

    .vehicle-info {
      .plate-box {
        display: flex;
        align-items: center;
        gap: 8px;

        .plate {
          font-size: 18px;
          font-weight: 600;
          font-family: 'PingFang SC', sans-serif;
          letter-spacing: 2px;
          color: var(--text-primary);
        }
      }

      .type {
        margin-top: 6px;
        font-size: 12px;
        color: var(--text-secondary);
      }
    }

    .actions {
      display: flex;
      gap: 16px;

      .van-icon {
        color: var(--text-secondary);
      }
    }
  }

  .add-btn {
    margin-top: 24px;
  }
}
</style>
