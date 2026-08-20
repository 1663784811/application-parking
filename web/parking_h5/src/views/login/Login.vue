<template>
  <div class="login">
    <!-- ===== 品牌头部 ===== -->
    <header class="login-hero">
      <div class="hero-glow"></div>
      <div class="dot-grid"></div>

      <div class="hero-inner">
        <div class="logo">
          <div class="logo-ring"></div>
          <svg viewBox="0 0 64 64" width="60" height="60" role="img" aria-label="智慧停车">
            <rect x="3" y="3" width="58" height="58" rx="18" fill="#ffffff" />
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
            >P</text>
          </svg>
        </div>
        <h1 class="app-name">智慧停车</h1>
        <p class="slogan">让停车更简单 · 扫码即出场</p>
      </div>
    </header>

    <!-- ===== 表单卡片 ===== -->
    <div class="login-card">
      <div class="card-head">
        <h2 class="card-title">账号登录</h2>
        <p class="card-sub">欢迎回来，请输入账号信息</p>
      </div>

      <van-form @submit="onSubmit">
        <van-cell-group class="field-group" :border="false">
          <!-- 账号 -->
          <van-field
            v-model="state.username"
            name="username"
            left-icon="manager-o"
            placeholder="请输入账号"
            clearable
            :border="false"
            :rules="[{ required: true, message: '请输入账号' }]"
          />

          <!-- 密码 -->
          <van-field
            v-model="state.password"
            :type="showPassword ? 'text' : 'password'"
            name="password"
            left-icon="lock"
            placeholder="请输入密码"
            :border="false"
            :rules="[{ required: true, message: '请输入密码' }]"
          >
            <template #right-icon>
              <van-icon
                :name="showPassword ? 'eye-o' : 'closed-eye'"
                class="pwd-toggle"
                @click="togglePassword"
              />
            </template>
          </van-field>

          <!-- 验证码 -->
          <van-field
            v-model="state.code"
            name="code"
            left-icon="shield-o"
            placeholder="请输入验证码"
            clearable
            :border="false"
            :rules="[{ required: true, message: '请输入验证码' }]"
          >
            <template #button>
              <div class="verify-slot">
                <template v-if="state.verifyImg">
                  <img
                    class="verify-img"
                    :src="state.verifyImg"
                    alt="验证码"
                    @click="loadVerifyCode"
                  />
                  <van-icon
                    name="replay"
                    class="verify-refresh"
                    @click="loadVerifyCode"
                  />
                </template>
                <van-button
                  v-else
                  class="verify-btn"
                  size="small"
                  @click="loadVerifyCode"
                >
                  <span class="verify-btn-inner">
                    <van-icon name="replay" />
                    <span>获取验证码</span>
                  </span>
                </van-button>
              </div>
            </template>
          </van-field>
        </van-cell-group>

        <!-- 记住账号 / 忘记密码 -->
        <div class="form-options">
          <van-checkbox v-model="remember" shape="square" icon-size="16px">
            记住账号
          </van-checkbox>
          <span class="forgot" @click="onForgot">忘记密码？</span>
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
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getBrowserFingerprint } from '@/api/browser'
import { getVerifyCode, storeAdminLogin } from '@/api/app'
import { useLoginInfoStore } from '@/stores/loginInfo'

const route = useRoute()
const router = useRouter()
const loginInfoStore = useLoginInfoStore()

// 模拟登录开关：true 时使用默认账号密码本地登录，不请求接口（联调后置为 false）
const MOCK_LOGIN = true
const MOCK_ACCOUNT = 'admin'
const MOCK_PASSWORD = '123456'
const MOCK_CODE = '1234'

// 记住账号的本地存储键
const REMEMBER_KEY = 'parking_remember_account'

const state = reactive({
  username: MOCK_LOGIN ? MOCK_ACCOUNT : '',
  password: MOCK_LOGIN ? MOCK_PASSWORD : '',
  code: MOCK_LOGIN ? MOCK_CODE : '',
  // 浏览器指纹
  fingerprint: '',
  // 验证码编码 Key
  verifyKey: '',
  // 验证码图片地址
  verifyImg: '',
  // 提交中
  loading: false,
})

// 密码可见性
const showPassword = ref(false)
// 记住账号
const remember = ref(false)

onMounted(() => {
  // 恢复记住的账号（非模拟登录时生效）
  const saved = localStorage.getItem(REMEMBER_KEY)
  if (saved && !MOCK_LOGIN) {
    state.username = saved
    remember.value = true
  }
  // 非模拟登录：进入即自动拉取验证码
  if (!MOCK_LOGIN) {
    loadVerifyCode()
  }
})

// 切换密码可见性
const togglePassword = () => {
  showPassword.value = !showPassword.value
}

// 忘记密码（暂无独立页，提示联系管理员）
const onForgot = () => {
  showToast('请联系管理员重置密码')
}

// 获取验证码：先取指纹，再换 key，再拼图片地址
const loadVerifyCode = () => {
  // 模拟登录：不请求验证码接口
  if (MOCK_LOGIN) return
  getBrowserFingerprint().then((fp) => {
    state.fingerprint = fp
    return getVerifyCode({ fingerprint: fp })
  }).then((res) => {
    // 返回业务数据为验证码编码 Key 字符串
    state.verifyKey = res.data
    const baseUrl = import.meta.env.VITE_BASE_URL
    // 加随机参数防止缓存
    state.verifyImg = `${baseUrl}/api/common/verify/getVerifyImg/${state.verifyKey}?t=${Date.now()}`
  }).catch((err) => {
    showToast(err?.msg || '获取验证码失败')
  })
}

// 提交登录
const onSubmit = () => {
  // 模拟登录：直接写入假 token 跳转，不请求接口
  if (MOCK_LOGIN) {
    handleRemember()
    loginInfoStore.setToken('mock-token', 'mock-refresh-token')
    router.replace({
      name: 'mainIndex',
      params: { appId: route.params.appId },
    })
    return
  }
  if (!state.verifyKey) {
    showToast('请先获取验证码')
    return
  }
  state.loading = true
  storeAdminLogin({
    username: state.username,
    password: state.password,
    code: state.code,
    fingerprint: state.verifyKey,
  }).then((res) => {
    // 业务数据: { jwtToken, refreshToken }
    handleRemember()
    loginInfoStore.setToken(res.data.jwtToken, res.data.refreshToken)
    router.replace({
      name: 'mainIndex',
      params: { appId: route.params.appId },
    })
  }).catch((err) => {
    showToast(err?.msg || '登录失败')
    // 登录失败刷新验证码
    loadVerifyCode()
  }).finally(() => {
    state.loading = false
  })
}

// 记住 / 清除账号
const handleRemember = () => {
  if (remember.value) {
    localStorage.setItem(REMEMBER_KEY, state.username)
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

      :deep(.van-field__right-icon) {
        display: flex;
        align-items: center;
        padding: 0 4px;
      }
    }

    .pwd-toggle {
      font-size: 20px;
      color: var(--text-tertiary);
      cursor: pointer;
    }

    // 验证码槽位：按钮态 / 图片态
    .verify-slot {
      display: flex;
      align-items: center;
      gap: 6px;
    }

    .verify-btn {
      --van-button-default-height: 32px;
      --van-button-default-background: var(--brand-primary-5);
      --van-button-default-color: var(--brand-primary-1);
      --van-button-default-border-color: var(--brand-primary-4);
      padding: 0 12px;
      border-radius: var(--radius-sm);
      font-size: 12px;
      font-weight: 500;
      white-space: nowrap;

      :deep(.verify-btn-inner) {
        display: inline-flex;
        align-items: center;
        gap: 4px;
      }
    }

    .verify-img {
      display: block;
      height: 30px;
      border: 1px solid var(--border-secondary);
      border-radius: 6px;
      background: var(--bg-primary);
      cursor: pointer;
    }

    .verify-refresh {
      font-size: 16px;
      color: var(--brand-primary);
      cursor: pointer;
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

      .forgot {
        font-size: 13px;
        color: var(--text-secondary);
        cursor: pointer;
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
  0% { opacity: 0; transform: translateY(-16px); }
  100% { opacity: 1; transform: translateY(0); }
}

@keyframes fade-up {
  0% { opacity: 0; transform: translateY(16px); }
  100% { opacity: 1; transform: translateY(0); }
}

@keyframes pop {
  0% { opacity: 0; transform: scale(0.6); }
  100% { opacity: 1; transform: scale(1); }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.6; }
  50% { transform: scale(1.14); opacity: 0.3; }
}

@keyframes float-a {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-10px, 14px); }
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
