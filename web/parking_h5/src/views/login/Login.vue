<template>
  <div class="login">
    <!-- ===== 品牌头部 ===== -->
    <header class="login-hero">
      <div class="hero-glow"></div>
      <div class="dot-grid"></div>

      <div class="hero-inner">
        <div class="logo">
          <div class="logo-ring"></div>
          <img
              v-if="showLogo"
              class="logo-img"
              :src="appStore.appLogo"
              alt="logo"
              @error="logoFailed = true"
          />
          <svg v-else viewBox="0 0 64 64" width="60" height="60" role="img" :aria-label="appName">
            <rect x="3" y="3" width="58" height="58" rx="18" fill="#ffffff"/>
            <rect
                x="3"
                y="3"
                width="58"
                height="58"
                rx="18"
                fill="none"
                stroke="#ffffff"
                stroke-opacity="0.7"
                stroke-width="2"
            />
            <text
                x="33"
                y="45"
                text-anchor="middle"
                font-family="PingFang SC, Arial, sans-serif"
                font-size="40"
                font-weight="800"
                fill="#10b981"
            >P
            </text>
          </svg>
        </div>
        <h1 class="app-name">{{ appName }}</h1>
        <p class="slogan">让停车更简单 · 扫码即出场</p>
      </div>
    </header>

    <!-- ===== 表单卡片 ===== -->
    <div class="login-card">
      <div class="card-head">
        <h2 class="card-title">手机号登录</h2>
        <p class="card-sub">未注册的手机号将自动创建账号</p>
      </div>

      <van-form @submit="onSubmit">
        <van-cell-group class="field-group" :border="false">
          <!-- 手机号 -->
          <van-field
              v-model="state.phone"
              type="tel"
              name="phone"
              left-icon="phone-o"
              placeholder="请输入手机号"
              clearable
              maxlength="11"
              :border="false"
              :rules="[
              { required: true, message: '请输入手机号' },
              { pattern: phoneRegex, message: '请输入正确的手机号' },
            ]"
          />

          <!-- 验证码 -->
          <van-field
              v-model="state.code"
              type="digit"
              name="code"
              left-icon="shield-o"
              placeholder="请输入验证码"
              clearable
              maxlength="6"
              :border="false"
              :rules="[{ required: true, message: '请输入验证码' }]"
          >
            <template #button>
              <van-button
                  class="code-btn"
                  size="small"
                  native-type="button"
                  :disabled="!canSendCode"
                  :loading="sendingCode"
                  @click="onSendCode"
              >
                {{ countdown > 0 ? `${countdown}s 后重获` : '获取验证码' }}
              </van-button>
            </template>
          </van-field>
        </van-cell-group>

        <!-- 记住手机号 -->
        <div class="form-options">
          <van-checkbox v-model="remember" shape="square" icon-size="16px">
            记住手机号
          </van-checkbox>
        </div>

        <!-- 登录按钮 -->
        <div class="submit">
          <van-button
              class="submit-btn"
              block
              round
              native-type="submit"
              :loading="state.loading"
          >
            登录
          </van-button>
        </div>

        <p class="agreement">
          登录即表示同意<span class="link">《用户协议》</span>与<span class="link">《隐私政策》</span>
        </p>
      </van-form>
    </div>
  </div>
</template>

<script setup>
import {computed, onMounted, onUnmounted, reactive, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {showToast} from 'vant'
import {getBrowserFingerprint} from '@/api/browser'
import {getVerifyPhoneCode, phoneLogin} from '@/api/app'
import {useLoginInfoStore} from '@/stores/loginInfo'
import {useAppStore} from '@/stores/app'

const route = useRoute()
const router = useRouter()
const loginInfoStore = useLoginInfoStore()
const appStore = useAppStore()

// 应用类型：对接 /app/login/findApp 的 appType（可在 .env 用 VITE_APP_TYPE 覆盖）
const appType = import.meta.env.VITE_APP_TYPE || 'parking'

// 模拟登录开关：true 时本地写入假 token 跳转，不请求接口（联调后置为 false）
const MOCK_LOGIN = false
const MOCK_PHONE = '13800138000'
const MOCK_CODE = '1234'

// 记住手机号的本地存储键
const REMEMBER_KEY = 'parking_remember_phone'

// 中国大陆手机号校验
const phoneRegex = /^1[3-9]\d{9}$/

const state = reactive({
  phone: MOCK_LOGIN ? MOCK_PHONE : '',
  code: MOCK_LOGIN ? MOCK_CODE : '',
  // 浏览器指纹（发送验证码时获取，登录时回传）
  fingerprint: '',
  // 提交中
  loading: false,
})

// 记住手机号
const remember = ref(false)
// logo 加载失败时回退到内置 SVG
const logoFailed = ref(false)
const showLogo = computed(() => !!appStore.appLogo && !logoFailed.value)
// 应用名称：未取到时回退到默认名
const appName = computed(() => appStore.appName || '智慧停车')

// ===== 验证码倒计时 =====
const countdown = ref(0)
const sendingCode = ref(false)
const codeSent = ref(false)
let codeTimer = null
const startCountdown = () => {
  countdown.value = 60
  codeTimer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) {
      clearInterval(codeTimer)
      codeTimer = null
      countdown.value = 0
    }
  }, 1000)
}
// 发送验证码按钮是否可用
const canSendCode = computed(
    () => phoneRegex.test(state.phone) && countdown.value === 0 && !sendingCode.value
)

onMounted(() => {
  // 已登录还进到登录页（返回手势、收藏的链接、外部跳转）：直接送去 redirect 或首页，
  // 别让用户对着一张没用的表单再登一次。
  // 这里刻意不 return —— 下面的 fetchApp 是给目标页备 appInfo 的，跳走了照样需要
  if (loginInfoStore.isLogin) {
    goAfterLogin()
  }
  // 恢复记住的手机号（非模拟登录时生效）
  const saved = localStorage.getItem(REMEMBER_KEY)
  if (saved && !MOCK_LOGIN) {
    state.phone = saved
    remember.value = true
  }
  // 非模拟登录：确保 app 信息已加载（直达登录页时回填 logo / 名称）
  if (!MOCK_LOGIN) {
    if (!appStore.appInfo) appStore.fetchApp(appType)
  }
})

onUnmounted(() => {
  if (codeTimer) {
    clearInterval(codeTimer)
    codeTimer = null
  }
})

// 发送手机验证码：先取指纹，再调 getVerifyPhoneCode，成功后开启倒计时
const onSendCode = () => {
  if (!phoneRegex.test(state.phone)) {
    showToast('请输入正确的手机号')
    return
  }
  sendingCode.value = true
  getBrowserFingerprint().then((fp) => {
    state.fingerprint = fp
    return getVerifyPhoneCode({phone: state.phone, fingerprint: fp})
  }).then(() => {
    codeSent.value = true
    showToast('验证码已发送')
    startCountdown()
  }).catch((err) => {
    showToast(err?.msg || '验证码发送失败')
  }).finally(() => {
    sendingCode.value = false
  })
}

// 登录成功后的去向：优先回守卫带过来的 redirect（用户原本想去的页面，比如扫码出场），
// 没有就回首页。只认站内路径 —— 别让 ?redirect=//evil.com 这种把页面带出去
const goAfterLogin = () => {
  const redirect = route.query.redirect
  if (typeof redirect === 'string' && redirect.startsWith('/')) {
    router.replace(redirect)
    return
  }
  router.replace({name: 'mainIndex', params: {appId: route.params.appId}})
}

// 提交登录
const onSubmit = () => {
  // 模拟登录：直接写入假 token 跳转，不请求接口
  if (MOCK_LOGIN) {
    handleRemember()
    loginInfoStore.setToken('mock-token', 'mock-refresh-token')
    goAfterLogin()
    return
  }
  if (!codeSent.value) {
    showToast('请先获取验证码')
    return
  }
  state.loading = true
  phoneLogin({
    phone: state.phone,
    code: state.code,
    fingerprint: state.fingerprint,
    appId: route.params.appId,
  }).then((res) => {
    // 业务数据: { jwtToken, refreshToken }
    handleRemember()
    loginInfoStore.setToken(res.data.jwtToken, res.data.refreshToken)
    // 不 await：昵称晚一步到没关系，"我的"页面用的是 computed，数据回来自己会刷上去
    loginInfoStore.fetchUserInfo()
    goAfterLogin()
  }).catch((err) => {
    showToast(err?.msg || '登录失败')
  }).finally(() => {
    state.loading = false
  })
}

// 记住 / 清除手机号
const handleRemember = () => {
  if (remember.value) {
    localStorage.setItem(REMEMBER_KEY, state.phone)
  } else {
    localStorage.removeItem(REMEMBER_KEY)
  }
}
</script>

<style scoped lang="less">
.login {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: auto;
  background: var(--bg-secondary);
  padding-bottom: calc(24px + env(safe-area-inset-bottom, 0));

  // ===== 品牌头部 =====
  .login-hero {
    position: relative;
    overflow: hidden;
    padding: calc(var(--safe-top) + 32px) 24px 64px;
    background: var(--gradient-primary);
    text-align: center;
    animation: fade-down 0.6s ease both;

    .hero-glow {
      position: absolute;
      width: 240px;
      height: 240px;
      top: -90px;
      right: -70px;
      border-radius: 50%;
      background: var(--on-brand-glass-strong);
      filter: blur(44px);
      pointer-events: none;
      animation: float-a 8s ease-in-out infinite;
    }

    // 点阵网格（白色，叠在主色上）
    .dot-grid {
      position: absolute;
      inset: 0;
      background-image: radial-gradient(rgba(255, 255, 255, 0.18) 1px, transparent 1px);
      background-size: 22px 22px;
      mask-image: linear-gradient(to bottom, rgba(0, 0, 0, 0.6), transparent 80%);
      -webkit-mask-image: linear-gradient(to bottom, rgba(0, 0, 0, 0.6), transparent 80%);
      pointer-events: none;
    }

    .hero-inner {
      position: relative;
      z-index: 1;

      .logo {
        position: relative;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 14px;

        .logo-ring {
          position: absolute;
          width: 96px;
          height: 96px;
          border-radius: 50%;
          background: var(--on-brand-glass-soft);
          animation: pulse 2.6s ease-in-out infinite;
        }

        svg {
          position: relative;
          z-index: 1;
          filter: drop-shadow(0 8px 18px var(--shadow-brand-strong));
          animation: pop 0.6s cubic-bezier(0.22, 1, 0.36, 1) both;
        }

        .logo-img {
          position: relative;
          z-index: 1;
          width: 60px;
          height: 60px;
          border-radius: 14px;
          object-fit: cover;
          filter: drop-shadow(0 8px 18px var(--shadow-brand-strong));
          animation: pop 0.6s cubic-bezier(0.22, 1, 0.36, 1) both;
        }
      }

      .app-name {
        margin: 0 0 6px;
        font-size: 26px;
        font-weight: 700;
        letter-spacing: 2px;
        color: var(--on-brand-primary);
      }

      .slogan {
        margin: 0;
        font-size: 13px;
        letter-spacing: 1px;
        color: var(--on-brand-secondary);
      }
    }
  }

  // ===== 表单卡片 =====
  .login-card {
    position: relative;
    z-index: 1;
    margin: -32px 16px;
    padding: 24px 16px 40px;
    background: var(--bg-primary);
    border-radius: 24px;
    box-shadow: 0 -6px 24px var(--shadow-light);
    animation: fade-up 0.6s ease 0.1s both;

    .card-head {
      margin: 0 4px 20px;

      .card-title {
        margin: 0 0 4px;
        font-size: 22px;
        font-weight: 600;
        color: var(--text-primary);
      }

      .card-sub {
        margin: 0;
        font-size: 13px;
        color: var(--text-tertiary);
      }
    }

    .field-group {
      background: transparent;

      // 胶囊填充式输入框
      :deep(.van-cell) {
        background: var(--bg-secondary);
        border-radius: var(--radius-md);
        margin-bottom: 12px;
        padding: 12px 14px;

        &::after {
          border-bottom: none;
        }
      }

      :deep(.van-field__left-icon) {
        margin-right: 10px;
        font-size: 20px;
        color: var(--brand-primary);
      }

      :deep(.van-field__value) {
        font-size: 15px;
      }
    }

    // 获取验证码按钮
    .code-btn {
      --van-button-default-height: 32px;
      --van-button-default-background: var(--brand-primary-5);
      --van-button-default-color: var(--brand-primary-1);
      --van-button-default-border-color: var(--brand-primary-4);
      padding: 0 12px;
      border-radius: var(--radius-sm);
      font-size: 12px;
      font-weight: 500;
      white-space: nowrap;
    }

    .form-options {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-top: 6px;
      padding: 0 4px;

      :deep(.van-checkbox__label) {
        font-size: 13px;
        color: var(--text-secondary);
      }
    }

    .submit {
      margin-top: 26px;
      padding: 0 4px;

      .submit-btn {
        --van-button-default-height: 50px;
        --van-button-default-background: var(--gradient-primary);
        --van-button-default-color: var(--on-brand-primary);
        --van-button-default-border-color: transparent;
        font-size: 16px;
        font-weight: 600;
        box-shadow: 0 10px 24px var(--shadow-brand);
      }
    }

    .agreement {
      margin: 16px 4px 0;
      font-size: 12px;
      color: var(--text-tertiary);

      .link {
        color: var(--brand-primary);
      }
    }
  }
}

// ===== 动画 =====
@keyframes fade-down {
  0% {
    opacity: 0;
    transform: translateY(-16px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fade-up {
  0% {
    opacity: 0;
    transform: translateY(16px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pop {
  0% {
    opacity: 0;
    transform: scale(0.6);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.6;
  }
  50% {
    transform: scale(1.14);
    opacity: 0.3;
  }
}

@keyframes float-a {
  0%, 100% {
    transform: translate(0, 0);
  }
  50% {
    transform: translate(-10px, 14px);
  }
}

// 无障碍：减少动效偏好
@media (prefers-reduced-motion: reduce) {
  .login-hero,
  .hero-glow,
  .logo-ring,
  .login-card,
  .login-hero svg {
    animation: none !important;
  }
}
</style>
