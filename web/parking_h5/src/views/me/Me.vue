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
            :src="avatar"
          />
          <div v-if="state.isVip" class="vip-badge">
            <van-icon name="vip-card-o" />
          </div>
        </div>
        <div class="info">
          <h3 class="nickname">{{ nickname }}</h3>
          <p v-if="account" class="account">账号：{{ account }}</p>
          <p v-if="state.memberType" class="member-type">{{ state.memberType }}</p>
        </div>
      </div>
      <van-skeleton v-if="state.loading" title :row="1" class="header-stats-skeleton" />
      <div v-else class="header-stats">
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
import { computed, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { useLoginInfoStore } from '@/stores/loginInfo'
import { getMeBoard } from '@/api/appMe'

const route = useRoute()
const router = useRouter()
const loginInfoStore = useLoginInfoStore()

// 默认头像（用户没设头像时兜底）
const DEFAULT_AVATAR = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'

// 用户信息取自登录态 store：登录成功 / 授权回跳后由 fetchUserInfo 写入，且已持久化，刷新也在。
// 用 computed 而不是快照 —— 请求和页面挂载谁先谁后不确定，晚到的数据得能刷上去
const baseInfo = computed(() => loginInfoStore.userInfo?.baseInfo || {})
// 昵称字段是 nickName（后端个人中心接口的 AuUser 结构里没有 name）
const nickname = computed(() => baseInfo.value.nickName || '未登录')
// 头像字段：普通用户(AuUser)是 face，管理员/门店管理员是 avatar，两个都认
const avatar = computed(() => baseInfo.value.face || baseInfo.value.avatar || DEFAULT_AVATAR)
const account = computed(() => baseInfo.value.phone || baseInfo.value.account || '')

// 会员标识与各项统计由 /app/user/me/board 一次带回来。
// 先给零值而不是留空：请求还没回来时头部三格是 0，不会出现空白闪烁
const state = reactive({
  loading: false,
  memberType: '',
  isVip: false,
  parkingTimes: 0,
  parkingDuration: '0小时',
  couponCount: 0,
  orderCount: 0,
  vehicleCount: 0,
  cardCount: 0,
})

// 后端返回的可能是 null，展示统一落成 0
const num = (v) => {
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

const loadBoard = () => {
  state.loading = true
  getMeBoard().then((res) => {
    const data = res?.data || {}
    state.isVip = !!data.isVip
    state.memberType = data.memberType || ''
    state.parkingTimes = num(data.parkingTimes)
    state.parkingDuration = data.parkingDuration || '0小时'
    state.couponCount = num(data.couponCount)
    state.orderCount = num(data.orderCount)
    state.vehicleCount = num(data.vehicleCount)
    state.cardCount = num(data.cardCount)
  }).catch((err) => {
    showToast(err?.msg || '加载个人信息失败')
  }).finally(() => {
    state.loading = false
  })
}

// 个人信息（昵称 / 头像 / 账号）单独取。
// 登录和回跳时 store 已经拉过，但那边是 fire-and-forget，请求可能还没回来；
// 而且改了昵称头像要重新进「我的」才看得到，所以进页面补拉一次，晚到的数据由上面的 computed 刷上去
const loadUserInfo = () => {
  loginInfoStore.fetchUserInfo()
}

onMounted(() => {
  loadUserInfo()
  loadBoard()
})

// 已实现的菜单页：coupon/vehicle/cardPackage/scanExit
// 停车出场指向出场缴费页（scanExit），没有 parkingId 时该页会提示扫码进入
const menuItems = [
  { icon: 'coupon-o', label: '优惠券', name: 'coupon' },
  { icon: 'wallet-o', label: '我的车辆', name: 'vehicle' },
  { icon: 'card-o', label: '卡包', name: 'cardPackage' },
  { icon: 'parking-o', label: '停车出场', name: 'scanExit' },
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

    // 数据回来前的占位，尺寸跟着 header-stats，切换时不跳动
    .header-stats-skeleton {
      margin-top: 24px;
      padding: 16px;
      background: var(--on-brand-glass);
      border-radius: 12px;
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
