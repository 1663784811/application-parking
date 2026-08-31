import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    // 欢迎页面（根页面，不显示 tab 菜单）
    path: '/welcome',
    name: 'welcome',
    component: () => import('@/views/welcome/Welcome.vue'),
    meta: {
      title: '欢迎',
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
    // 停车出场
    path: '/app/:appId/parkingExit',
    name: 'parkingExit',
    component: () => import('@/views/parkingExit/ParkingExit.vue'),
    props: true,
    meta: {
      title: '停车出场',
    },
  },
  {
    // 错误页面
    path: '/:pathMatch(.*)*',
    name: 'notFound',
    component: () => import('@/views/error/NotFound.vue'),
    meta: {
      title: '页面不存在',
    },
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

// 路由守卫：设置页面标题、验证 appId
router.beforeEach((to) => {
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
})

export default router
