<template>
  <div class="not-found">
    <!-- 装饰光晕 -->
    <div class="glow glow--1"></div>
    <div class="glow glow--2"></div>
    <div class="dot-grid"></div>

    <div class="nf-inner">
      <!-- 插画 -->
      <div class="illustration">
        <div class="illust-ring"></div>
        <svg viewBox="0 0 220 180" width="100%" height="100%" role="img" aria-label="页面走丢了">
          <defs>
            <linearGradient id="nf-p" x1="0" y1="0" x2="1" y2="1">
              <stop offset="0" stop-color="#10b981" />
              <stop offset="1" stop-color="#34d399" />
            </linearGradient>
            <linearGradient id="nf-road" x1="0" y1="0" x2="1" y2="0">
              <stop offset="0" stop-color="transparent" />
              <stop offset="0.5" stop-color="#9ca3af" />
              <stop offset="1" stop-color="transparent" />
            </linearGradient>
          </defs>

          <!-- 路面虚线 -->
          <line x1="20" y1="150" x2="200" y2="150" stroke="url(#nf-road)" stroke-width="3" stroke-dasharray="10 10" />

          <!-- 停车 P 牌（带问号，表示迷路） -->
          <g transform="translate(58 18)">
            <!-- 牌柱 -->
            <rect x="9" y="70" width="6" height="62" rx="3" fill="#cbd5e1" />
            <!-- 牌面 -->
            <rect x="-8" y="0" width="40" height="70" rx="14" fill="url(#nf-p)" />
            <rect
              x="-8"
              y="0"
              width="40"
              height="70"
              rx="14"
              fill="none"
              stroke="#ffffff"
              stroke-opacity="0.55"
              stroke-width="2"
            />
            <!-- P 字 -->
            <text
              x="12"
              y="50"
              text-anchor="middle"
              font-family="PingFang SC, Arial, sans-serif"
              font-size="44"
              font-weight="800"
              fill="#ffffff"
            >P</text>
            <!-- 问号徽标 -->
            <circle cx="40" cy="12" r="12" fill="#ffffff" stroke="#10b981" stroke-width="2" />
            <text
              x="40"
              y="17"
              text-anchor="middle"
              font-family="PingFang SC, Arial, sans-serif"
              font-size="15"
              font-weight="800"
              fill="#10b981"
            >?</text>
          </g>

          <!-- 迷路的小车 -->
          <g transform="translate(118 116)">
            <!-- 车身阴影 -->
            <ellipse cx="26" cy="34" rx="26" ry="5" fill="#000000" opacity="0.08" />
            <!-- 车身 -->
            <rect x="0" y="10" width="52" height="22" rx="7" fill="#1f2937" />
            <!-- 车顶 -->
            <path d="M10 10 L16 0 L36 0 L42 10 Z" fill="#374151" />
            <!-- 车窗 -->
            <path d="M13 9 L18 2 L34 2 L39 9 Z" fill="#a7f3d0" opacity="0.85" />
            <!-- 车轮 -->
            <circle cx="13" cy="34" r="6" fill="#1f2937" />
            <circle cx="39" cy="34" r="6" fill="#1f2937" />
            <circle cx="13" cy="34" r="2.4" fill="#9ca3af" />
            <circle cx="39" cy="34" r="2.4" fill="#9ca3af" />
            <!-- 疑问汗滴 -->
            <circle cx="52" cy="6" r="4" fill="#f59e0b" opacity="0.9" />
          </g>

          <!-- 脚印 / 问号点缀 -->
          <text
            x="178"
            y="78"
            font-family="PingFang SC, Arial, sans-serif"
            font-size="20"
            font-weight="800"
            fill="#10b981"
            opacity="0.35"
          >?</text>
          <text
            x="30"
            y="96"
            font-family="PingFang SC, Arial, sans-serif"
            font-size="14"
            font-weight="800"
            fill="#10b981"
            opacity="0.3"
          >?</text>
        </svg>
      </div>

      <!-- 文案 -->
      <div class="copy">
        <h1 class="code">404</h1>
        <h2 class="title">页面走丢了</h2>
        <p class="desc">您访问的页面不存在或已被移除，请返回首页继续使用。</p>
      </div>

      <!-- 操作 -->
      <footer class="cta">
        <van-button class="primary-btn" block round @click="goWelcome">
          <van-icon name="home-o" />
          <span>返回首页</span>
        </van-button>
        <van-button class="ghost-btn" block round @click="goBack">
          <van-icon name="arrow-left" />
          <span>返回上一页</span>
        </van-button>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goWelcome = () => {
  router.push({ name: 'welcome' })
}

const goBack = () => {
  // 没有历史记录时兜底回首页
  if (window.history.length <= 1) {
    router.push({ name: 'welcome' })
  } else {
    router.back()
  }
}
</script>

<style scoped lang="less">
.not-found {
  position: relative;
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--bg-primary);
  padding: calc(var(--safe-top) + 32px) 24px calc(24px + env(safe-area-inset-bottom, 0));

  // 背景光晕
  .glow {
    position: absolute;
    border-radius: 50%;
    filter: blur(48px);
    pointer-events: none;

    &--1 {
      width: 280px;
      height: 280px;
      top: -90px;
      right: -70px;
      background: var(--brand-primary-3);
      opacity: 0.45;
      animation: float-a 7s ease-in-out infinite;
    }

    &--2 {
      width: 240px;
      height: 240px;
      bottom: -50px;
      left: -80px;
      background: var(--brand-primary-4);
      opacity: 0.55;
      animation: float-b 9s ease-in-out infinite;
    }
  }

  // 点阵网格
  .dot-grid {
    position: absolute;
    inset: 0;
    background-image: radial-gradient(rgba(16, 185, 129, 0.12) 1px, transparent 1px);
    background-size: 22px 22px;
    mask-image: linear-gradient(to bottom, rgba(0, 0, 0, 0.5), transparent 75%);
    -webkit-mask-image: linear-gradient(to bottom, rgba(0, 0, 0, 0.5), transparent 75%);
    pointer-events: none;
  }

  .nf-inner {
    position: relative;
    z-index: 1;
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
  }

  // ===== 插画 =====
  .illustration {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 220px;
    height: 180px;
    margin-bottom: 8px;
    animation: fade-up 0.7s ease both;

    .illust-ring {
      position: absolute;
      width: 220px;
      height: 220px;
      border-radius: 50%;
      background: var(--brand-primary-5);
      animation: pulse 2.6s ease-in-out infinite;
    }

    svg {
      position: relative;
      z-index: 1;
      animation: pop 0.6s cubic-bezier(0.22, 1, 0.36, 1) both;

      // 小车微微抖动，表现"迷路"
      g[transform*="translate(118 116)"] {
        transform-box: fill-box;
        transform-origin: center;
        animation: lost-wobble 3s ease-in-out infinite;
      }
    }
  }

  // ===== 文案 =====
  .copy {
    animation: fade-up 0.7s ease 0.1s both;

    .code {
      margin: 0;
      font-size: 56px;
      font-weight: 800;
      letter-spacing: 4px;
      line-height: 1.1;
      background: var(--gradient-primary);
      -webkit-background-clip: text;
      background-clip: text;
      -webkit-text-fill-color: transparent;
      color: transparent;
    }

    .title {
      margin: 6px 0 10px;
      font-size: 20px;
      font-weight: 600;
      color: var(--text-primary);
    }

    .desc {
      margin: 0 auto;
      max-width: 260px;
      font-size: 14px;
      line-height: 1.6;
      color: var(--text-tertiary);
    }
  }

  // ===== 操作 =====
  .cta {
    width: 100%;
    max-width: 320px;
    margin-top: 36px;
    display: flex;
    flex-direction: column;
    gap: 12px;
    animation: fade-up 0.7s ease 0.2s both;

    .primary-btn {
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

    .ghost-btn {
      --van-button-default-height: 46px;
      --van-button-default-background: var(--bg-secondary);
      --van-button-default-color: var(--text-secondary);
      --van-button-default-border-color: var(--border-primary);
      font-size: 15px;
      font-weight: 500;

      :deep(span) {
        display: inline-flex;
        align-items: center;
        gap: 6px;
      }
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
  0%, 100% { transform: scale(1); opacity: 0.6; }
  50% { transform: scale(1.12); opacity: 0.35; }
}

@keyframes float-a {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-10px, 14px); }
}

@keyframes float-b {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(12px, -10px); }
}

@keyframes lost-wobble {
  0%, 100% { transform: rotate(-3deg) translateX(0); }
  50% { transform: rotate(3deg) translateX(-3px); }
}

// 尊重无障碍：减少动效偏好
@media (prefers-reduced-motion: reduce) {
  .glow,
  .illust-ring,
  svg,
  svg g[transform*="translate(118 116)"],
  .copy,
  .cta {
    animation: none !important;
  }
}
</style>
