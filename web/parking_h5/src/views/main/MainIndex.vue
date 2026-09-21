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

      <div
        v-for="item in state.parkingList"
        :key="item.id"
        class="parking-card"
      >
        <img class="parking-img" :src="item.image" :alt="item.name" />
        <div class="parking-info">
          <h4 class="name">{{ item.name }}</h4>
          <p class="address">{{ item.address }}</p>
          <div class="tags">
            <span class="tag" :class="{ 'tag-green': item.remainSpaces > 10 }">
              剩余 {{ item.remainSpaces }} 位
            </span>
            <span class="distance">{{ item.distance }}</span>
          </div>
        </div>
        <div class="nav-btn" @click="handleNavigate(item)">
          <van-icon name="location" size="18" />
          <span>导航</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'

const state = reactive({
  // TODO: 接口获取附近停车场列表（getParkingList）
  parkingList: [
    { id: '1', name: '万达广场停车场', address: '朝阳区建国路93号', remainSpaces: 56, totalSpaces: 200, price: '5', distance: '500m', image: '' },
    { id: '2', name: '国贸中心地下停车场', address: '朝阳区建国门外大街1号', remainSpaces: 12, totalSpaces: 150, price: '8', distance: '1.2km', image: '' },
    { id: '3', name: '银泰中心停车场', address: '朝阳区建国门外大街2号', remainSpaces: 88, totalSpaces: 300, price: '6', distance: '800m', image: '' },
    { id: '4', name: '华贸中心停车场', address: '朝阳区建国路89号', remainSpaces: 3, totalSpaces: 100, price: '7', distance: '1km', image: '' },
    { id: '5', name: 'SKP-S购物中心停车场', address: '朝阳区建国路87号', remainSpaces: 42, totalSpaces: 250, price: '10', distance: '1.5km', image: '' },
    { id: '6', name: '华润大厦停车场', address: '朝阳区姚家园路68号', remainSpaces: 0, totalSpaces: 80, price: '6', distance: '2km', image: '' },
    { id: '7', name: '东方新天地停车场', address: '东城区东长安街1号', remainSpaces: 35, totalSpaces: 180, price: '8', distance: '2.3km', image: '' },
    { id: '8', name: '来福士广场停车场', address: '东城区东直门南大街1号', remainSpaces: 67, totalSpaces: 200, price: '7', distance: '2.5km', image: '' },
    { id: '9', name: '三里屯SOHO停车场', address: '朝阳区工人体育场北路8号', remainSpaces: 28, totalSpaces: 150, price: '9', distance: '2.8km', image: '' },
    { id: '10', name: '太古里南区停车场', address: '朝阳区三里屯路19号', remainSpaces: 15, totalSpaces: 120, price: '10', distance: '3km', image: '' },
    { id: '11', name: '颐堤港停车场', address: '朝阳区酒仙桥路18号', remainSpaces: 73, totalSpaces: 280, price: '6', distance: '3.2km', image: '' },
    { id: '12', name: '朝阳大悦城停车场', address: '朝阳区朝阳北路101号', remainSpaces: 45, totalSpaces: 350, price: '5', distance: '3.5km', image: '' },
    { id: '13', name: '合生汇停车场', address: '朝阳区西大望路21号', remainSpaces: 92, totalSpaces: 400, price: '4', distance: '3.8km', image: '' },
    { id: '14', name: '蓝色港湾停车场', address: '朝阳区朝阳公园路6号', remainSpaces: 38, totalSpaces: 200, price: '7', distance: '4km', image: '' },
    { id: '15', name: '侨福芳草地停车场', address: '朝阳区东大桥路9号', remainSpaces: 22, totalSpaces: 100, price: '8', distance: '4.2km', image: '' },
    { id: '16', name: '国贸商城停车场', address: '朝阳区建国门外大街1号', remainSpaces: 55, totalSpaces: 500, price: '8', distance: '1.8km', image: '' },
    { id: '17', name: '燕莎友谊商城停车场', address: '朝阳区亮马桥路52号', remainSpaces: 18, totalSpaces: 150, price: '6', distance: '4.5km', image: '' },
    { id: '18', name: '凤凰汇停车场', address: '朝阳区三元桥凤凰城', remainSpaces: 60, totalSpaces: 220, price: '7', distance: '5km', image: '' },
    { id: '19', name: '凯德MALL太阳宫停车场', address: '朝阳区太阳宫中路12号', remainSpaces: 85, totalSpaces: 300, price: '5', distance: '5.5km', image: '' },
    { id: '20', name: '望京SOHO停车场', address: '朝阳区望京街10号', remainSpaces: 40, totalSpaces: 250, price: '6', distance: '6km', image: '' },
  ],
})

const route = useRoute()
const router = useRouter()

const handleScan = () => {
  // 本 H5 还没有扫码能力：调起相机需要微信 JS-SDK / 支付宝 JSAPI，
  // 两者都要公众号/应用凭证 + 后端签名接口，目前都不具备。
  // 而出场页必须带 parkingId/channelId 才能查费用，扫不出参数跳过去也是空跑，
  // 所以这里如实提示车主用相机扫出口通道的二维码。
  // 接入扫码后，把这里换成「取扫码结果 URL → 解析 parkingId/channelId → 跳 scanExit」。
  showToast('请扫描出口通道的二维码进入缴费')
}

const handleNavigate = (item) => {
  // TODO: 调用地图导航
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
    }

    .parking-info {
      flex: 1;
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
