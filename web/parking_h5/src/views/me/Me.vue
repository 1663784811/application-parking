<template>
  <div class="profile-page">
    <div class="profile-header">
      <div class="user-info">
        <div class="avatar-wrapper">
          <van-image
            round
            width="72"
            height="72"
            fit="cover"
            :src="state.avatar"
          />
          <div v-if="state.isVip" class="vip-badge">
            <van-icon name="vip-card-o" />
          </div>
        </div>
        <div class="info">
          <h3 class="nickname">{{ state.nickname }}</h3>
          <p class="account">账号：{{ state.account }}</p>
          <p v-if="state.memberType" class="member-type">{{ state.memberType }}</p>
        </div>
      </div>
      <div class="header-stats">
        <div class="stat-item">
          <span class="value">{{ state.parkingTimes }}</span>
          <span class="label">停车次数</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="value">{{ state.parkingDuration }}</span>
          <span class="label">累计停车时长</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item" @click="goPage('coupon')">
          <span class="value">{{ state.couponCount }}</span>
          <span class="label">优惠券</span>
        </div>
      </div>
    </div>

    <div class="stats-row">
      <div class="stat-item" @click="goPage('order')">
        <span class="value">{{ state.orderCount }}</span>
        <span class="label">订单</span>
      </div>
      <div class="stat-item" @click="goPage('vehicle')">
        <span class="value">{{ state.vehicleCount }}</span>
        <span class="label">车辆</span>
      </div>
      <div class="stat-item" @click="goPage('cardPackage')">
        <span class="value">{{ state.cardCount }}</span>
        <span class="label">卡包</span>
      </div>
    </div>

    <van-cell-group class="menu-group">
      <van-cell
        v-for="item in menuItems"
        :key="item.name"
        :icon="item.icon"
        :title="item.label"
        is-link
        @click="onMenuClick(item)"
      />
    </van-cell-group>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showConfirmDialog } from 'vant'
import { useLoginInfoStore } from '@/stores/loginInfo'

const route = useRoute()
const router = useRouter()
const loginInfoStore = useLoginInfoStore()

const state = reactive({
  // TODO: 接口获取用户信息及统计（getUserInfo）
  avatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
  nickname: loginInfoStore.userInfo?.baseInfo?.name || '未登录',
  account: '13800138000',
  memberType: 'VIP会员',
  isVip: true,
  parkingTimes: 128,
  parkingDuration: '256小时',
  couponCount: 5,
  orderCount: 20,
  vehicleCount: 2,
  cardCount: 3,
})

// 已实现的菜单页：coupon/vehicle/cardPackage/parkingExit
const menuItems = [
  { icon: 'coupon-o', label: '优惠券', name: 'coupon' },
  { icon: 'wallet-o', label: '我的车辆', name: 'vehicle' },
  { icon: 'card-o', label: '卡包', name: 'cardPackage' },
  { icon: 'parking-o', label: '停车出场', name: 'parkingExit' },
  { icon: 'orders-o', label: '我的订单', name: 'order' },
  { icon: 'cross', label: '退出登录', name: 'logout' },
]

// 跳转到子页面，保留 appId
const goPage = (name) => {
  router.push({
    name,
    params: { appId: route.params.appId },
  })
}

const onMenuClick = (item) => {
  if (item.name === 'logout') {
    onLogout()
    return
  }
  goPage(item.name)
}

const onLogout = () => {
  showConfirmDialog({
    title: '提示',
    message: '确定要退出登录吗？',
  }).then(() => {
    loginInfoStore.logout()
    router.replace({ name: 'welcome' })
  }).catch(() => {})
}
</script>

<style scoped lang="less">
.profile-page {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: auto;

  .profile-header {
    padding: 32px 16px 20px;
    padding-top: calc(var(--safe-top) + 32px);
    background: linear-gradient(135deg, var(--brand-primary), var(--brand-primary-2));
    color: var(--on-brand-primary);

    .user-info {
      display: flex;
      align-items: center;
      position: relative;

      .avatar-wrapper {
        position: relative;

        .vip-badge {
          position: absolute;
          bottom: -2px;
          right: -2px;
          width: 24px;
          height: 24px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: var(--accent-gold);
          border-radius: 50%;
          color: var(--accent-gold-deep);
          font-size: 14px;
        }
      }

      .info {
        flex: 1;
        margin-left: 16px;

        .nickname {
          font-size: 22px;
          font-weight: 600;
          margin-bottom: 4px;
        }

        .account {
          font-size: 13px;
          opacity: 0.85;
        }

        .member-type {
          display: inline-block;
          margin-top: 6px;
          padding: 2px 10px;
          font-size: 11px;
          background: var(--on-brand-glass);
          border-radius: 10px;
        }
      }
    }

    .header-stats {
      display: flex;
      justify-content: space-around;
      margin-top: 24px;
      padding: 16px;
      background: var(--on-brand-glass);
      border-radius: 12px;

      .stat-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        flex: 1;

        .value {
          font-size: 20px;
          font-weight: 600;
          margin-bottom: 4px;
        }

        .label {
          font-size: 12px;
          opacity: 0.85;
        }
      }

      .stat-divider {
        width: 1px;
        height: 36px;
        background: var(--on-brand-divider);
      }
    }
  }

  .stats-row {
    display: flex;
    justify-content: space-around;
    padding: 20px 16px;
    background: var(--bg-primary);
    margin-bottom: 12px;

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;

      .value {
        display: block;
        font-size: 20px;
        font-weight: 600;
        color: var(--text-primary);
      }

      .label {
        font-size: 12px;
        color: var(--text-secondary);
        margin-top: 4px;
      }
    }
  }

  .menu-group {
    margin-bottom: 12px;
  }
}
</style>
