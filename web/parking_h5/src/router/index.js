import { createRouter, createWebHashHistory } from 'vue-router'
import { showToast } from 'vant'

const routes = [
  {
    // 欢迎页面（根页面，不显示 tab 菜单）
    path: '/welcome',
    name: 'welcome',
    component: () => import('@/views/welcome/Welcome.vue'),
    meta: {
      title: '欢迎',
      public: true,
    },
  },
  {
    // 登录页面（不显示 tab 菜单）
    path: '/app/:appId/login',
    name: 'login',
    component: () => import('@/views/login/Login.vue'),
    props: true,
    meta: {
      title: '登录',
        public: true,
    },
  },
  {
    // app 布局（显示 tab 菜单的页面都在此 children 下）
    path: '/app/:appId/main',
    name: 'main',
    component: () => import('@/views/main/Main.vue'),
    props: true,
    children: [
      {
        // app 首页（显示 tab 菜单）
        path: '',
        name: 'mainIndex',
        component: () => import('@/views/main/MainIndex.vue'),
        props: true,
        meta: {
          title: '首页',
          showTabBar: true,
        },
      },
      {
        // 我的页面（显示 tab 菜单）
        path: 'me',
        name: 'me',
        component: () => import('@/views/me/Me.vue'),
        props: true,
        meta: {
          title: '我的',
          showTabBar: true,
        },
      },
    ],
  },
  // ====== 业务子页面（不显示 tab 菜单，均带 appId） ======
  {
    // 订单列表
    path: '/app/:appId/order',
    name: 'order',
    component: () => import('@/views/order/Order.vue'),
    props: true,
    meta: {
      title: '我的订单',
    },
  },
  {
    // 订单详情
    path: '/app/:appId/order/:orderId',
    name: 'orderDetail',
    component: () => import('@/views/order/OrderDetail.vue'),
    props: true,
    meta: {
      title: '订单详情',
    },
  },
  {
    // 我的车辆
    path: '/app/:appId/vehicle',
    name: 'vehicle',
    component: () => import('@/views/vehicle/Vehicle.vue'),
    props: true,
    meta: {
      title: '我的车辆',
    },
  },
  {
    // 添加车辆
    path: '/app/:appId/vehicle/add',
    name: 'vehicleAdd',
    component: () => import('@/views/vehicle/VehicleAdd.vue'),
    props: true,
    meta: {
      title: '添加车辆',
    },
  },
  {
    // 编辑车辆
    path: '/app/:appId/vehicle/edit/:vehicleId',
    name: 'vehicleEdit',
    component: () => import('@/views/vehicle/VehicleEdit.vue'),
    props: true,
    meta: {
      title: '编辑车辆',
    },
  },
  {
    // 卡包
    path: '/app/:appId/cardPackage',
    name: 'cardPackage',
    component: () => import('@/views/cardPackage/CardPackage.vue'),
    props: true,
    meta: {
      title: '卡包',
    },
  },
  {
    // 优惠券
    path: '/app/:appId/coupon',
    name: 'coupon',
    component: () => import('@/views/coupon/Coupon.vue'),
    props: true,
    meta: {
      title: '优惠券',
    },
  },
  {
    // 出场缴费（输入车牌 → 查费用 → 支付，单页完成，二维码地址指向这里）
    path: '/app/:appId/scanExit',
    name: 'scanExit',
    component: () => import('@/views/scanExit/ScanExit.vue'),
    props: true,
    meta: {
      title: '输入车牌',
    },
  },
  {
    // 旧出场页路由：出场流程已合并进 scanExit，早先印出去的二维码仍指向这里，
    // 原样带参转到新页面，别让旧码扫出一片空白
    path: '/app/:appId/parkingExit',
    redirect: (to) => ({ name: 'scanExit', params: to.params, query: to.query }),
  },
  {
    // 错误页面
    path: '/:pathMatch(.*)*',
    name: 'notFound',
    component: () => import('@/views/error/NotFound.vue'),
    meta: {
      title: '页面不存在',
      public: true,
    },
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

/**
 * 消费第三方授权凭证：平台回跳把 code / auth_code 带到地址上后，在这里换登录态。
 * 必须赶在目标页挂载之前完成 —— 页面自己的 onMounted 就会调业务接口，
 * 晚一步就是没有 JWT 的裸奔请求（出场三接口不在白名单，直接 6001/6010）。
 *
 * @return 需要重定向的 location（摘掉凭证），无事可做返回 undefined
 */
const consumeAuthCode = async (to) => {
  // 微信回跳带 code，支付宝回跳带 auth_code
  const { code, auth_code: authCode } = to.query
  if (!code && !authCode) {
    return undefined
  }
  // 动态引入：这条路径只在平台回跳时走一次，别为它把 axios 拖进入口包
  const [{ wechatMpLogin, alipayLogin }, { loginInfo }] = await Promise.all([
    import('@/api/thirdAuth'),
    import('@/stores/loginInfo'),
  ])
  try {
    // 后端两个登录接口的入参字段都叫 code，支付宝那边填的是 auth_code
    const res = code
      ? await wechatMpLogin({ appId: to.params.appId, code })
      : await alipayLogin({ appId: to.params.appId, code: authCode })
    const data = res?.data || {}
    if (data.jwtToken) {
      loginInfo().setToken(data.jwtToken, data.refreshToken)
      // 不 await：用户信息是给"我的"页面用的，别为它多等一个来回再跳转
      loginInfo().fetchUserInfo()
    }
  } catch (err) {
    showToast(err?.msg || '登录失败')
  }
  // 凭证都是一次性的，成功失败都得从地址栏摘掉，免得刷新时拿旧值重放
  const { code: _code, auth_code: _authCode, ...restQuery } = to.query
  return { path: to.path, query: restQuery, replace: true }
}

// 路由守卫：设置页面标题、验证 appId、换第三方登录态、拦截未登录
router.beforeEach(async (to) => {
  // 用 meta.title 作为页面标题
  const title = to.meta?.title
  if (title) {
    document.title = title
  }

  if (to.matched.some(record => record.path.includes(':appId'))) {
    const appId = to.params.appId
    if (!appId) {
      // 没有 appId，重定向到欢迎页
      return { name: 'welcome' }
    }
  }

  // 顺序要紧：平台回跳的 code 必须先换成 token，再判登录态。
  // 反过来的话 /app/1/scanExit?code=xxx 会在换到 token 之前就被判成未登录踢去登录页，code 白拿
  const authRedirect = await consumeAuthCode(to)
  if (authRedirect) {
    return authRedirect
  }

  // 未登录跳登录页。默认一律要登录，只有声明了 meta.public 的页面（欢迎页、登录页、404）放行 ——
  // 反过来写（声明了才拦）容易漏，新加的路由会默认裸奔
  if (!to.meta?.public) {
    if (!to.params.appId) {
      // 兜底：带 :appId 的路由上面已校验过，这里防的是拼不出 login 路径直接白屏
      return { name: 'welcome' }
    }
    // 动态引入：静态 import 会顺着 stores/loginInfo → api/app → axios 把 axios 拖进入口包
    const { loginInfo } = await import('@/stores/loginInfo')
    if (!loginInfo().isLogin) {
      // 带上 redirect，登录后回到原本想去的页面 —— 扫码出场的深链不能丢
      return {
        name: 'login',
        params: { appId: to.params.appId },
        query: { redirect: to.fullPath },
      }
    }
  }
})

export default router
