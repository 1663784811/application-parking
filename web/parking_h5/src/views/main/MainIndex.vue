<template>
  <div class="home-page">
    <div class="header">
      <div class="location">
        <van-icon name="location-o" />
        <span>当前位置</span>
      </div>
      <div class="scan" @click="handleScan">
        <van-icon name="scan" />
      </div>
    </div>

    <van-swipe class="banner" :autoplay="3000" :height="150">
      <van-swipe-item>
        <div class="banner-placeholder">
          <van-icon name="parking-o" size="40" />
        </div>
      </van-swipe-item>
      <van-swipe-item>
        <div class="banner-placeholder">
          <van-icon name="location-o" size="40" />
        </div>
      </van-swipe-item>
    </van-swipe>

    <div class="section">
      <div class="section-title">
        <h3>附近停车场</h3>
      </div>

      <!-- 加载中 -->
      <van-skeleton
        v-if="state.loading"
        title
        :row="3"
        :row-width="['100%', '60%', '40%']"
      />

      <!-- 空态：停车场要么没建、要么都设成了「不对外开放」 -->
      <van-empty
        v-else-if="!state.parkingList.length"
        description="附近暂无停车场"
      />

      <template v-else>
        <div
          v-for="item in state.parkingList"
          :key="item.id"
          class="parking-card"
          @click="handleParkingClick(item)"
        >
          <img
            v-if="item.image"
            class="parking-img"
            :src="item.image"
            :alt="item.name"
          />
          <!-- 没配图时给品牌色渐变底 + 图标，别留一块空白 -->
          <div v-else class="parking-img parking-img--empty">
            <van-icon name="parking-o" size="28" />
          </div>
          <div class="parking-info">
            <h4 class="name">{{ item.name }}</h4>
            <p class="address">{{ item.address }}</p>
            <div class="tags">
              <span class="tag" :class="{ 'tag-green': item.remainSpaces > 10 }">
                剩余 {{ item.remainSpaces }} 位
              </span>
              <!-- 未授权定位 / 该场未配坐标时后端不返回距离，此时不显示 -->
              <span v-if="item.distance" class="distance">{{ item.distance }}</span>
            </div>
          </div>
          <div class="nav-btn" @click.stop="handleNavigate(item)">
            <van-icon name="location" size="18" />
            <span>导航</span>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getParkingList } from '@/api/appParking'

const route = useRoute()
const router = useRouter()

// appId 贯穿全路由，列表要按当前应用过滤
const appId = computed(() => route.params.appId)

const state = reactive({
  parkingList: [],
  loading: true,
})

/**
 * 取当前位置。拿不到就当作没有定位 —— 权限被拒、超时、浏览器不支持都属于正常情况，
 * 后端本来就把 lng/lat 设计成可选，绝不为了「有距离」把首页卡住或假装定位成功。
 *
 * @return {Promise<{lng: number, lat: number} | null>}
 */
const getPosition = () => {
  return new Promise((resolve) => {
    if (!navigator.geolocation) {
      resolve(null)
      return
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => resolve({ lng: pos.coords.longitude, lat: pos.coords.latitude }),
      () => resolve(null),
      { timeout: 5000, maximumAge: 60000 },
    )
  })
}

const loadParkingList = async () => {
  state.loading = true
  try {
    const position = await getPosition()
    const res = await getParkingList({
      appId: appId.value,
      ...(position ? { lng: position.lng, lat: position.lat } : {}),
    })
    // 拦截器已把 BaseResult 解包，列表在 data 里
    state.parkingList = res?.data || []
  } catch (err) {
    state.parkingList = []
    showToast(err?.msg || '停车场列表加载失败')
  } finally {
    state.loading = false
  }
}

onMounted(loadParkingList)

const handleScan = () => {
  // 本 H5 还没有扫码能力：调起相机需要微信 JS-SDK / 支付宝 JSAPI，
  // 两者都要公众号/应用凭证 + 后端签名接口，目前都不具备。
  // 而出场页必须带 parkingId/channelId 才能查费用，扫不出参数跳过去也是空跑，
  // 所以这里如实提示车主用相机扫出口通道的二维码。
  // 接入扫码后，把这里换成「取扫码结果 URL → 解析 parkingId/channelId → 跳 scanExit」。
  showToast('请扫描出口通道的二维码进入缴费')
}

// 点卡片直接进该停车场的出场缴费页：带上真实 parkingId，ScanExit 靠它定位停车场
const handleParkingClick = (item) => {
  router.push({
    name: 'scanExit',
    params: { appId: appId.value },
    query: { parkingId: item.id },
  })
}

const handleNavigate = (item) => {
  // TODO: 调用地图导航（需要地图 SDK 凭证，暂缺）
  console.log('导航到:', item.name)
}
</script>

<style scoped lang="less">
.home-page {
  display: flex;
  flex-direction: column;
  // 固定为一屏高度，让 .header 的 sticky 生效（始终固定在顶部）；
  // 上层 .content/.home-page 均是 overflow:auto 但无确定高度，body 滚动会使 sticky 失效
  height: 100vh;
  overflow: auto;
  // 底部留出 TabBar（van-tabbar fixed，50px + 安全区）高度，避免末项被遮挡
  padding-bottom: calc(50px + env(safe-area-inset-bottom, 0));

  .header {
    position: sticky;
    top: 0;
    z-index: 100;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    padding-top: calc(var(--safe-top) + 12px);
    background: var(--bg-primary);

    .location {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 14px;
      color: var(--text-secondary);
    }

    .scan {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 36px;
      height: 36px;
      background: var(--bg-secondary);
      border-radius: 50%;
      font-size: 20px;
      color: var(--brand-primary);
    }
  }

  .banner {
    margin: 12px 16px;
    border-radius: 8px;
    overflow: hidden;
    min-height: 150px;

    .banner-placeholder {
      width: 100%;
      height: 150px;
      background: var(--gradient-primary);
      display: flex;
      align-items: center;
      justify-content: center;
      color: var(--on-brand-primary);
    }
  }

  .section {
    padding: 0 16px 16px;

    .section-title {
      display: flex;
      align-items: center;
      margin-bottom: 16px;

      h3 {
        font-size: 18px;
        font-weight: 600;
        color: var(--text-primary);
      }
    }
  }

  .parking-card {
    display: flex;
    gap: 12px;
    padding: 12px;
    margin-bottom: 12px;
    background: var(--bg-primary);
    border-radius: var(--radius-md);

    .parking-img {
      width: 100px;
      height: 75px;
      border-radius: 8px;
      object-fit: cover;
      background: var(--bg-tertiary);
      flex-shrink: 0;
    }

    // 没配图时的占位块：品牌色浅渐变 + 图标
    .parking-img--empty {
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--gradient-primary-light);
      color: var(--brand-primary);
    }

    .parking-info {
      flex: 1;
      min-width: 0;
      display: flex;
      flex-direction: column;
      justify-content: space-between;

      .name {
        font-size: 16px;
        font-weight: 600;
        color: var(--text-primary);
      }

      .address {
        font-size: 12px;
        color: var(--text-secondary);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .tags {
        display: flex;
        align-items: center;
        gap: 8px;

        .tag {
          padding: 2px 8px;
          font-size: 11px;
          color: var(--color-warning);
          background: var(--color-warning-light);
          border-radius: 4px;
        }

        .tag-green {
          color: var(--color-success);
          background: var(--color-success-light);
        }

        .distance {
          font-size: 12px;
          color: var(--text-secondary);
        }
      }
    }

    .nav-btn {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 8px 12px;
      color: var(--brand-primary);

      span {
        font-size: 11px;
        margin-top: 2px;
      }
    }
  }
}
</style>