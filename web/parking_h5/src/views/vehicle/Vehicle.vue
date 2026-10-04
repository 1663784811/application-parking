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

      <van-empty
        v-if="!state.loading && state.vehicleList.length === 0"
        description="暂无车辆"
      />

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
import { onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { getMeVehicleList, deleteMeVehicle } from '@/api/appMe'

const route = useRoute()
const router = useRouter()

const state = reactive({
  loading: false,
  vehicleList: [],
})

// 没有 keep-alive，onMounted 每次进页面都会跑，
// 所以从添加/编辑页返回时列表会自动刷一次，不需要单独监听返回
const loadVehicles = () => {
  state.loading = true
  getMeVehicleList().then((res) => {
    state.vehicleList = res?.data || []
  }).catch((err) => {
    showToast(err?.msg || '加载车辆失败')
  }).finally(() => {
    state.loading = false
  })
}

onMounted(() => {
  loadVehicles()
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
    // 删完重新拉一遍而不是本地 filter：默认车辆标记会在服务端重算，
    // 本地只删一条会留下过期状态
    return deleteMeVehicle(id).then(() => {
      showToast('删除成功')
      loadVehicles()
    }).catch((err) => {
      showToast(err?.msg || '删除失败')
    })
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
