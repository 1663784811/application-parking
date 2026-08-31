<template>
  <div class="welcome">
    <!-- 装饰光晕 -->
    <div class="glow glow--1"></div>
    <div class="glow glow--2"></div>
    <div class="dot-grid"></div>

    <div class="welcome-inner">
      <!-- 品牌 -->
      <header class="hero">
        <div class="logo">
          <div class="logo-ring"></div>
          <img
            v-if="showLogo"
            class="logo-img"
            :src="appStore.appLogo"
            alt="logo"
            @error="logoFailed = true"
          />
          <svg v-else viewBox="0 0 64 64" width="76" height="76" role="img" aria-label="智慧停车">
            <defs>
              <linearGradient id="welcome-p" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0" stop-color="#10b981" />
                <stop offset="1" stop-color="#34d399" />
              </linearGradient>
            </defs>
            <rect x="3" y="3" width="58" height="58" rx="18" fill="url(#welcome-p)" />
            <rect
              x="3"
              y="3"
              width="58"
              height="58"
              rx="18"
              fill="none"
              stroke="#ffffff"
              stroke-opacity="0.55"
              stroke-width="2"
            />
            <!-- 停车 P 标 -->
            <text
              x="33"
              y="45"
              text-anchor="middle"
              font-family="PingFang SC, Arial, sans-serif"
              font-size="40"
              font-weight="800"
              fill="#ffffff"
            >P</text>
            <!-- 车位点阵点缀 -->
            <g fill="#ffffff" fill-opacity="0.5">
              <circle cx="20" cy="54" r="1.6" />
              <circle cx="27" cy="54" r="1.6" />
              <circle cx="37" cy="54" r="1.6" />
              <circle cx="44" cy="54" r="1.6" />
            </g>
          </svg>
        </div>
        <h1 class="app-name">{{ appName }}</h1>
        <p class="slogan">让停车更简单 · 扫码即出场</p>
      </header>

      <!-- 特色 -->
      <section class="features">
        <div class="feature">
          <div class="feature-ico"><van-icon name="scan" /></div>
          <span class="feature-label">扫码出场</span>
        </div>
        <div class="feature">
          <div class="feature-ico"><van-icon name="balance-o" /></div>
          <span class="feature-label">在线支付</span>
        </div>
        <div class="feature">
          <div class="feature-ico"><van-icon name="coupon-o" /></div>
          <span class="feature-label">优惠卡包</span>
        </div>
      </section>

      <!-- 操作 -->
      <footer class="cta">
        <van-button
          class="enter-btn"
          block
          round
          :loading="appStore.loading"
          :disabled="appStore.loading"
          @click="onEnter"
        >
          <span>{{ btnText }}</span>
          <van-icon v-if="appStore.appId" name="arrow" />
        </van-button>
        <p class="agreement">
          登录即表示同意
          <span class="link">《用户协议》</span>与<span class="link">《隐私政策》</span>
        </p>
        <p class="version">v1.0.0</p>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useAppStore } from '@/stores/app'

const router = useRouter()
const appStore = useAppStore()

// 应用类型：对接 /app/login/findApp 的 appType（可在 .env 用 VITE_APP_TYPE 覆盖）
const appType = import.meta.env.VITE_APP_TYPE || 'parking'

// logo 加载失败时回退到内置 SVG
const logoFailed = ref(false)
const showLogo = computed(() => !!appStore.appLogo && !logoFailed.value)

// 应用名称：未取到时回退到默认名
const appName = computed(() => appStore.appName || '智慧停车')

// 按钮文案：加载中 / 失败重试 / 进入应用
const btnText = computed(() => {
  if (appStore.loading) return '加载中'
  if (!appStore.appId) return '重新加载'
  return '进入应用'
})

// 查询 app 信息
const loadApp = () => {
  appStore.fetchApp(appType).then((info) => {
    if (!info) {
      showToast('应用信息获取失败，请重试')
    }
  })
}

const onEnter = () => {
  if (appStore.loading) return
  // 未取到 app 信息时，点击按钮重新查询
  if (!appStore.appId) {
    loadApp()
    return
  }
  router.push({
    name: 'login',
    params: { appId: appStore.appId },
  })
}

onMounted(() => {
  loadApp()
})
</script>

<style scoped lang="less">
.welcome {
  position: relative;
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--bg-primary);
  padding: calc(var(--safe-top) + 32px) 24px calc(24px + env(safe-area-inset-bottom, 0));

  // 背景光晕（浅绿点缀，白底上的柔和装饰）
  .glow {
    position: absolute;
    border-radius: 50%;
    filter: blur(48px);
    pointer-events: none;

    &--1 {
      width: 280px;
      height: 280px;
      top: -80px;
      right: -60px;
      background: var(--brand-primary-3);
      opacity: 0.5;
      animation: float-a 7s ease-in-out infinite;
    }

    &--2 {
      width: 240px;
      height: 240px;
      bottom: -40px;
      left: -70px;
      background: var(--brand-primary-4);
      opacity: 0.6;
      animation: float-b 9s ease-in-out infinite;
    }
  }

  // 点阵网格（浅绿）
  .dot-grid {
    position: absolute;
    inset: 0;
    background-image: radial-gradient(rgba(16, 185, 129, 0.12) 1px, transparent 1px);
    background-size: 22px 22px;
    mask-image: linear-gradient(to bottom, rgba(0, 0, 0, 0.5), transparent 70%);
    -webkit-mask-image: linear-gradient(to bottom, rgba(0, 0, 0, 0.5), transparent 70%);
    pointer-events: none;
  }

  .welcome-inner {
    position: relative;
    z-index: 1;
    flex: 1;
    display: flex;
    flex-direction: column;
  }

  // ===== 品牌 =====
  .hero {
    text-align: center;
    padding-top: 24px;
    animation: fade-up 0.7s ease both;

    .logo {
      position: relative;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 20px;

      .logo-ring {
        position: absolute;
        width: 116px;
        height: 116px;
        border-radius: 50%;
        background: var(--brand-primary-4);
        animation: pulse 2.4s ease-in-out infinite;
      }

      .logo-img {
        position: relative;
        z-index: 1;
        width: 76px;
        height: 76px;
        border-radius: 18px;
        object-fit: cover;
        filter: drop-shadow(0 10px 20px var(--shadow-brand-strong));
        animation: pop 0.6s cubic-bezier(0.22, 1, 0.36, 1) both;
      }

      svg {
        position: relative;
        z-index: 1;
        filter: drop-shadow(0 10px 20px var(--shadow-brand-strong));
        animation: pop 0.6s cubic-bezier(0.22, 1, 0.36, 1) both;
      }
    }

    .app-name {
      margin: 0 0 8px;
      font-size: 30px;
      font-weight: 700;
      letter-spacing: 2px;
      color: var(--text-primary);
    }

    .slogan {
      margin: 0;
      font-size: 14px;
      letter-spacing: 1px;
      color: var(--text-secondary);
    }
  }

  // ===== 特色 =====
  .features {
    display: flex;
    gap: 12px;
    margin-top: 44px;

    .feature {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 10px;
      padding: 18px 8px;
      background: var(--bg-secondary);
      border: 1px solid var(--border-primary);
      border-radius: var(--radius-lg);
      animation: fade-up 0.7s ease both;

      &:nth-child(1) { animation-delay: 0.15s; }
      &:nth-child(2) { animation-delay: 0.25s; }
      &:nth-child(3) { animation-delay: 0.35s; }

      .feature-ico {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 40px;
        height: 40px;
        font-size: 22px;
        color: var(--on-brand-primary);
        background: var(--gradient-primary);
        border-radius: 50%;
      }

      .feature-label {
        font-size: 13px;
        font-weight: 500;
        color: var(--text-secondary);
      }
    }
  }

  // ===== 操作 =====
  .cta {
    margin-top: auto;
    padding-top: 40px;
    animation: fade-up 0.7s ease 0.45s both;

    .enter-btn {
      --van-button-default-height: 50px;
      --van-button-default-background: var(--gradient-primary);
      --van-button-default-color: var(--on-brand-primary);
      --van-button-default-border-color: transparent;
      font-size: 16px;
      font-weight: 600;
      box-shadow: 0 10px 24px var(--shadow-brand);

      :deep(span) {
        display: inline-flex;
        align-items: center;
        gap: 6px;
      }
    }

    .agreement {
      margin: 14px 0 0;
      font-size: 12px;
      text-align: center;
      color: var(--text-tertiary);

      .link {
        color: var(--brand-primary);
        text-decoration: underline;
        text-underline-offset: 2px;
      }
    }

    .version {
      margin: 10px 0 0;
      font-size: 11px;
      text-align: center;
      color: var(--text-disabled);
    }
  }
}

// ===== 动画 =====
@keyframes pop {
  0% { opacity: 0; transform: scale(0.6); }
  100% { opacity: 1; transform: scale(1); }
}

@keyframes fade-up {
  0% { opacity: 0; transform: translateY(16px); }
  100% { opacity: 1; transform: translateY(0); }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.55; }
  50% { transform: scale(1.12); opacity: 0.3; }
}

@keyframes float-a {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-10px, 14px); }
}

@keyframes float-b {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(12px, -10px); }
}
</style>
