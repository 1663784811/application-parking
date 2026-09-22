/**
 * 路由配置
 */
import { createRouter, createWebHistory } from 'vue-router'
import { Message } from 'view-ui-plus'
import { enterpriseApi } from '@/api'

const routes = [
  {
    path: '/',
    redirect: '/welcome'
  },
  {
    path: '/welcome',
    name: 'welcome',
    component: () => import('@/views/welcome/welcome.vue'),
    meta: {
      title: '欢迎',
      public: true
    }
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/login/login.vue'),
    meta: {
      title: '登录',
      public: true
    }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/register/register.vue'),
    meta: {
      title: '注册企业',
      public: true
    }
  },
  {
    path: '/',
    component: () => import('@/views/layout/MainLayout.vue'),
    children: [
      // 工作台
      {
        path: '/home',
        name: 'home',
        component: () => import('@/views/home/home.vue'),
        meta: {
          title: '首页'
        }
      },

      // 停车场管理
      {
        path: '/parking/parkingList',
        name: 'parkingList',
        component: () => import('@/views/parking/parkingList/parkingList.vue'),
        meta: {
          title: '停车场列表',
        }
      },
      {
        path: '/system/chargeRuleConfig',
        name: 'chargeRuleConfig',
        component: () => import('@/views/system/chargeRuleConfig/chargeRuleConfig.vue'),
        meta: {
          title: '收费规则配置'
        }
      },
      {
        path: '/parking/channelList',
        name: 'channelList',
        component: () => import('@/views/parking/channelList/channelList.vue'),
        meta: {
          title: '通道列表',
          showParkingSidebar: true
        }
      },

      // 车辆通行
      {
        path: '/parking/spaceManagement',
        name: 'spaceManagement',
        component: () => import('@/views/parking/spaceManagement/spaceManagement.vue'),
        meta: {
          title: '在场车辆',
          showParkingSidebar: true
        }
      },
      {
        path: '/passag/realTimeMonitor',
        name: 'realTimeMonitor',
        component: () => import('@/views/passag/realTimeMonitor/realTimeMonitor.vue'),
        meta: {
          title: '实时监控',
          showParkingSidebar: true
        }
      },
      {
        path: '/passag/passageRecord',
        name: 'passageRecord',
        component: () => import('@/views/passag/passageRecord/passageRecord.vue'),
        meta: {
          title: '通行记录查询'
        }
      },
      {
        path: '/passag/exceptionRecord',
        name: 'exceptionRecord',
        component: () => import('@/views/passag/exceptionRecord/exceptionRecord.vue'),
        meta: {
          title: '异常通行记录'
        }
      },

      // 收费管理
      {
        path: '/charge/chargeFlow',
        name: 'chargeFlow',
        component: () => import('@/views/charge/chargeFlow/chargeFlow.vue'),
        meta: {
          title: '订单列表'
        }
      },
      {
        path: '/charge/orderReconcile',
        name: 'orderReconcile',
        component: () => import('@/views/charge/orderReconcile/orderReconcile.vue'),
        meta: {
          title: '订单对账'
        }
      },
      {
        path: '/charge/couponConfig',
        name: 'couponConfig',
        component: () => import('@/views/charge/couponConfig/couponConfig.vue'),
        meta: {
          title: '优惠配置'
        }
      },
      {
        path: '/charge/invoiceManage',
        name: 'invoiceManage',
        component: () => import('@/views/charge/invoiceManage/invoiceManage.vue'),
        meta: {
          title: '发票管理'
        }
      },

      // 会员管理
      {
        path: '/member/memberList',
        name: 'memberList',
        component: () => import('@/views/member/memberList/memberList.vue'),
        meta: {
          title: '固定车主列表'
        }
      },
      {
        path: '/member/packageConfig',
        name: 'packageConfig',
        component: () => import('@/views/member/packageConfig/packageConfig.vue'),
        meta: {
          title: '套餐配置'
        }
      },
      {
        path: '/member/renewalRecord',
        name: 'renewalRecord',
        component: () => import('@/views/member/renewalRecord/renewalRecord.vue'),
        meta: {
          title: '续费记录'
        }
      },

      // 设备管理
      {
        path: '/device/deviceList',
        name: 'deviceList',
        component: () => import('@/views/device/deviceList/deviceList.vue'),
        meta: {
          title: '设备列表'
        }
      },
      {
        path: '/device/faultRepair',
        name: 'faultRepair',
        component: () => import('@/views/device/faultRepair/faultRepair.vue'),
        meta: {
          title: '故障报修'
        }
      },
      {
        path: '/device/thingModel',
        name: 'thingModel',
        component: () => import('@/views/device/thingModel/thingModel.vue'),
        meta: {
          title: '物模型'
        }
      },

      // 数据报表
      {
        path: '/report/revenueReport',
        name: 'revenueReport',
        component: () => import('@/views/report/revenueReport/revenueReport.vue'),
        meta: {
          title: '营收统计'
        }
      },
      {
        path: '/report/trafficReport',
        name: 'trafficReport',
        component: () => import('@/views/report/trafficReport/trafficReport.vue'),
        meta: {
          title: '车流量报表'
        }
      },
      {
        path: '/report/spaceUsageReport',
        name: 'spaceUsageReport',
        component: () => import('@/views/report/spaceUsageReport/spaceUsageReport.vue'),
        meta: {
          title: '车位利用率'
        }
      },
      {
        path: '/report/memberRevenueReport',
        name: 'memberRevenueReport',
        component: () => import('@/views/report/memberRevenueReport/memberRevenueReport.vue'),
        meta: {
          title: '月卡营收'
        }
      },
      {
        path: '/report/exportReport',
        name: 'exportReport',
        component: () => import('@/views/report/exportReport/exportReport.vue'),
        meta: {
          title: '导出报表'
        }
      },

      // 系统设置
      {
        path: '/system/adminAccount',
        name: 'adminAccount',
        component: () => import('@/views/system/adminAccount/adminAccount.vue'),
        meta: {
          title: '管理员账号'
        }
      },
      {
        path: '/system/rolePermission',
        name: 'rolePermission',
        component: () => import('@/views/system/rolePermission/rolePermission.vue'),
        meta: {
          title: '角色权限'
        }
      },
      {
        path: '/system/smsConfig',
        name: 'smsConfig',
        component: () => import('@/views/system/smsConfig/smsConfig.vue'),
        meta: {
          title: '短信配置'
        }
      },
      {
        path: '/system/logManage',
        name: 'logManage',
        component: () => import('@/views/system/logManage/logManage.vue'),
        meta: {
          title: '日志管理'
        }
      },

      // 个人中心
      {
        path: '/personal/personal',
        name: 'personal',
        component: () => import('@/views/personal/personal.vue'),
        meta: {
          title: '个人中心'
        }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 智慧停车场管理平台`
  }

  // 判断是否需要登录
  const token = localStorage.getItem('token')
  if (!token && !to.meta.public) {
    // 未登录且访问需登录的页面 → 跳转登录
    next({ name: 'login' })
  } else if (token && (to.name === 'login' || to.name === 'register')) {
    // 已登录仍访问登录/注册页 → 跳转首页
    next({ name: 'home' })
  } else if (!token && to.name === 'register') {
    // 未登录访问注册页：若系统已存在企业则禁止重复注册，跳转登录
    try {
      const res = await enterpriseApi.checkEnterpriseExists()
      if (res.data) {
        Message.info('系统已存在企业，请直接登录')
        next({ name: 'login' })
      } else {
        next()
      }
    } catch (e) {
      // 查询异常时放行，注册提交时由后端再次校验
      next()
    }
  } else {
    next()
  }
})

export default router
