<template>
  <div class="welcome-page">
    <!-- ===================== 首屏 ===================== -->
    <section class="hero">
      <!-- 顶部导航 -->
      <header class="nav">
        <div class="nav-logo">
          <span class="nav-logo-icon">
            <Icon type="ios-car" />
          </span>
          <span class="nav-logo-text">智慧停车场管理平台</span>
        </div>
        <div class="nav-actions">
          <Button v-if="!loggedIn" type="primary" :loading="state.checking" @click="goEnter">免费开通</Button>
          <Button v-else type="primary" @click="goHome">进入工作台</Button>
        </div>
      </header>

      <!-- 首屏内容 -->
      <div class="hero-content">
        <span class="hero-badge">
          <Icon type="ios-ribbon" />
          <span>新一代智慧停车解决方案</span>
        </span>
        <h1 class="hero-title">让停车场运营更高效、更智能</h1>
        <p class="hero-desc">
          覆盖车场管理、智能监控、自动计费、会员服务、设备运维与数据报表全流程的一站式平台。
        </p>
        <div class="hero-actions">
          <Button
            type="primary"
            size="large"
            class="hero-cta"
            :loading="state.checking"
            @click="goEnter"
          >
            <span>{{ loggedIn ? '进入工作台' : (state.checking ? '校验中...' : '立即免费开通') }}</span>
            <Icon type="ios-arrow-forward" class="hero-cta-arrow" />
          </Button>
        </div>

        <!-- 平台亮点 -->
        <div class="hero-highlights">
          <div class="highlight" v-for="item in highlights" :key="item.label">
            <Icon :type="item.icon" />
            <span class="highlight-value">{{ item.value }}</span>
            <span class="highlight-label">{{ item.label }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- ===================== 核心功能 ===================== -->
    <section class="features">
      <div class="container">
        <h2 class="section-title">核心功能</h2>
        <p class="section-subtitle">覆盖停车场运营全流程</p>
        <div class="features-grid">
          <div class="feature-card" v-for="item in features" :key="item.title">
            <div class="feature-icon" :style="{ background: item.bg }">
              <Icon :type="item.icon" />
            </div>
            <h3 class="feature-name">{{ item.title }}</h3>
            <p class="feature-desc">{{ item.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- ===================== 页脚 ===================== -->
    <footer class="footer">
      <p>© {{ year }} 智慧停车场管理平台 · 让停车更智能</p>
    </footer>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Icon, Button } from 'view-ui-plus'
import { useUserStore } from '@/stores/user'
import { enterpriseApi } from '@/api'

const router = useRouter()
const userStore = useUserStore()

// 页面状态：企业存在性校验中
const state = reactive({
  checking: false
})

const loggedIn = userStore.isLoggedIn()
const year = new Date().getFullYear()

// 核心功能
const features = [
  {
    title: '停车场管理',
    desc: '多车场统一管理，车位状态实时掌握',
    icon: 'ios-car',
    bg: 'linear-gradient(135deg, #165DFF, #4080FF)'
  },
  {
    title: '智能监控',
    desc: '出入口全天候监控，异常自动预警',
    icon: 'ios-eye',
    bg: 'linear-gradient(135deg, #722ED1, #9254DE)'
  },
  {
    title: '自动计费',
    desc: '灵活收费规则，订单自动结算',
    icon: 'logo-yen',
    bg: 'linear-gradient(135deg, #00B42A, #23D130)'
  },
  {
    title: '会员服务',
    desc: '月卡 / 季卡 / 年卡，续费便捷',
    icon: 'ios-people',
    bg: 'linear-gradient(135deg, #FF7D00, #FF9A3C)'
  },
  {
    title: '设备运维',
    desc: '设备状态监控，故障及时报修',
    icon: 'ios-construct',
    bg: 'linear-gradient(135deg, #0FC6C2, #37D9D5)'
  },
  {
    title: '数据报表',
    desc: '营收车流多维分析，辅助运营决策',
    icon: 'ios-stats',
    bg: 'linear-gradient(135deg, #F53F3F, #FF7875)'
  }
]

// 平台亮点
const highlights = [
  { value: '7×24', label: '全天候监控', icon: 'ios-time' },
  { value: '多车场', label: '统一管理', icon: 'ios-apps' },
  { value: '毫秒级', label: '识别开闸', icon: 'ios-flash' }
]

// 查询是否已存在企业，未登录时据此分流：
//   存在 → 登录页；不存在 → 注册企业页；查询失败 → 兜底至注册企业页
const checkEnterpriseAndRedirect = async () => {
  try {
    state.checking = true
    const res = await enterpriseApi.checkEnterpriseExists()
    // res.data 为企业对象(AuEnterprise) 表示已存在企业，为 null 表示不存在
    if (res.data) {
      router.push({ name: 'login' })
    } else {
      router.push({ name: 'register' })
    }
  } catch (e) {
    // 接口异常时兜底引导至注册页
    router.push({ name: 'register' })
  } finally {
    state.checking = false
  }
}

// 进入系统入口：
//   已登录 → 工作台；未登录 → 按企业存在性分流（登录 / 注册）
const goEnter = () => {
  if (loggedIn) {
    router.push({ name: 'home' })
  } else {
    checkEnterpriseAndRedirect()
  }
}

// 进入工作台
const goHome = () => router.push({ name: 'home' })
</script>

<style lang="less" scoped>
.welcome-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: var(--bg-color-page);
}

.container {
  width: 100%;
  max-width: 1080px;
  margin: 0 auto;
  padding: 0 var(--spacing-xxl);
}

// ===================== 首屏 =====================
.hero {
  position: relative;
  padding: 0 var(--spacing-xxl);
  background: linear-gradient(160deg, #F7F9FF 0%, var(--bg-color-page) 100%);
  overflow: hidden;
}

.nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: var(--header-height);
  max-width: 1080px;
  margin: 0 auto;

  .nav-logo {
    display: flex;
    align-items: center;

    .nav-logo-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 34px;
      height: 34px;
      margin-right: var(--spacing-sm);
      font-size: 18px;
      color: #fff;
      background: linear-gradient(135deg, var(--primary-color), var(--primary-hover-color));
      border-radius: var(--border-radius-lg);
    }

    .nav-logo-text {
      font-size: var(--font-size-md);
      font-weight: 700;
      color: var(--text-color-title);
      letter-spacing: 1px;
    }
  }

  .nav-actions {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);
  }
}

.hero-content {
  max-width: 720px;
  margin: 0 auto;
  padding: 96px 0 80px;
  text-align: center;

  .hero-badge {
    display: inline-flex;
    align-items: center;
    padding: 6px 14px;
    margin-bottom: var(--spacing-xl);
    font-size: var(--font-size-sm);
    color: var(--primary-color);
    background: rgba(22, 93, 255, 0.08);
    border-radius: var(--border-radius-xl);

    .ivu-icon {
      margin-right: 6px;
      font-size: 16px;
    }
  }

  .hero-title {
    font-size: 44px;
    font-weight: 800;
    line-height: 1.25;
    color: var(--text-color-title);
    letter-spacing: 1px;
    margin-bottom: var(--spacing-lg);
  }

  .hero-desc {
    font-size: var(--font-size-md);
    line-height: var(--line-height-lg);
    color: var(--text-color-secondary);
    margin-bottom: var(--spacing-xxl);
  }

  .hero-actions {
    display: flex;
    justify-content: center;
    gap: var(--spacing-md);
    margin-bottom: var(--spacing-xxl);
  }

  .hero-highlights {
    display: flex;
    justify-content: center;
    gap: var(--spacing-xxl);

    .highlight {
      display: flex;
      align-items: center;
      gap: var(--spacing-sm);

      .ivu-icon {
        font-size: 22px;
        color: var(--primary-color);
      }

      .highlight-value {
        font-size: var(--font-size-lg);
        font-weight: 700;
        color: var(--text-color-title);
      }

      .highlight-label {
        font-size: var(--font-size-sm);
        color: var(--text-color-secondary);
      }
    }
  }
}

// 首屏按钮
.hero-cta {
  display: inline-flex;
  align-items: center;
  padding: 0 28px;
  font-weight: 600;

  .hero-cta-arrow {
    margin-left: 6px;
    transition: transform 0.25s ease;
  }

  &:hover .hero-cta-arrow {
    transform: translateX(3px);
  }
}

// ===================== 核心功能 =====================
.features {
  padding: 80px 0;
  background: var(--bg-color);

  .section-title {
    font-size: 28px;
    font-weight: 700;
    color: var(--text-color-title);
    text-align: center;
    margin-bottom: var(--spacing-sm);
  }

  .section-subtitle {
    font-size: var(--font-size-md);
    color: var(--text-color-secondary);
    text-align: center;
    margin-bottom: var(--spacing-xxl);
  }

  .features-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: var(--spacing-lg);

    .feature-card {
      padding: var(--spacing-xl);
      border: 1px solid var(--border-color-light);
      border-radius: var(--border-radius-xl);
      transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;

      &:hover {
        transform: translateY(-4px);
        border-color: var(--border-color);
        box-shadow: var(--shadow-medium);
      }

      .feature-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 48px;
        height: 48px;
        margin-bottom: var(--spacing-lg);
        border-radius: var(--border-radius-lg);

        .ivu-icon {
          font-size: 24px;
          color: #fff;
        }
      }

      .feature-name {
        font-size: var(--font-size-md);
        font-weight: 600;
        color: var(--text-color-title);
        margin-bottom: var(--spacing-sm);
      }

      .feature-desc {
        font-size: var(--font-size-sm);
        line-height: var(--line-height-lg);
        color: var(--text-color-secondary);
      }
    }
  }
}

// ===================== 页脚 =====================
.footer {
  padding: var(--spacing-xl);
  text-align: center;
  border-top: 1px solid var(--border-color-light);
  background: var(--bg-color);

  p {
    font-size: var(--font-size-sm);
    color: var(--text-color-secondary);
  }
}

// ===================== 响应式 =====================
@media (max-width: 768px) {
  .hero-content {
    padding: 64px 0 56px;

    .hero-title {
      font-size: 30px;
    }

    .hero-desc {
      font-size: var(--font-size-sm);
    }

    .hero-actions {
      flex-direction: column;
      align-items: stretch;
    }

    .hero-highlights {
      flex-direction: column;
      gap: var(--spacing-lg);
    }
  }

  .features .features-grid {
    grid-template-columns: 1fr;
  }
}
</style>
