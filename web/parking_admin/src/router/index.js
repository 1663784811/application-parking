/**
 * 路由配置
 */
import { createRouter, createWebHistory } from 'vue-router'

// 路由懒加载
const Layout = () => import('@/layout/index.vue')
const Login = () => import('@/views/login/login.vue')

// 工作台
const Home = () => import('@/views/home/home.vue')

// 车场管理
const ParkingList = () => import('@/views/parking/parkingList/parkingList.vue')
const SpaceManagement = () => import('@/views/parking/spaceManagement/spaceManagement.vue')

// 车辆通行
const RealTimeMonitor = () => import('@/views/passag/realTimeMonitor/realTimeMonitor.vue')
const PassageRecord = () => import('@/views/passag/passageRecord/passageRecord.vue')
const ExceptionRecord = () => import('@/views/passag/exceptionRecord/exceptionRecord.vue')

// 收费管理
const ChargeFlow = () => import('@/views/charge/chargeFlow/chargeFlow.vue')
const OrderReconcile = () => import('@/views/charge/orderReconcile/orderReconcile.vue')
const CouponConfig = () => import('@/views/charge/couponConfig/couponConfig.vue')
const InvoiceManage = () => import('@/views/charge/invoiceManage/invoiceManage.vue')

// 会员管理
const MemberList = () => import('@/views/member/memberList/memberList.vue')
const PackageConfig = () => import('@/views/member/packageConfig/packageConfig.vue')
const RenewalRecord = () => import('@/views/member/renewalRecord/renewalRecord.vue')

// 设备管理
const DeviceList = () => import('@/views/device/deviceList/deviceList.vue')
const FaultRepair = () => import('@/views/device/faultRepair/faultRepair.vue')

// 数据报表
const RevenueReport = () => import('@/views/report/revenueReport/revenueReport.vue')
const TrafficReport = () => import('@/views/report/trafficReport/trafficReport.vue')
const SpaceUsageReport = () => import('@/views/report/spaceUsageReport/spaceUsageReport.vue')
const ExportReport = () => import('@/views/report/exportReport/exportReport.vue')

// 系统设置
const AdminAccount = () => import('@/views/system/adminAccount/adminAccount.vue')
const RolePermission = () => import('@/views/system/rolePermission/rolePermission.vue')
const ChargeRuleConfig = () => import('@/views/system/chargeRuleConfig/chargeRuleConfig.vue')
const SmsConfig = () => import('@/views/system/smsConfig/smsConfig.vue')
const LogManage = () => import('@/views/system/logManage/logManage.vue')

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/login',
    name: 'login',
    component: Login,
    meta: {
      title: '登录'
    }
  },
  {
    path: '/',
    component: Layout,
    children: [
      // 工作台
      {
        path: '/home',
        name: 'home',
        component: Home,
        meta: {
          title: '首页'
        }
      },

      // 车场管理
      {
        path: '/parking/parkingList',
        name: 'parkingList',
        component: ParkingList,
        meta: {
          title: '车场列表'
        }
      },
      {
        path: '/parking/spaceManagement',
        name: 'spaceManagement',
        component: SpaceManagement,
        meta: {
          title: '车位管理'
        }
      },

      // 车辆通行
      {
        path: '/passag/realTimeMonitor',
        name: 'realTimeMonitor',
        component: RealTimeMonitor,
        meta: {
          title: '实时监控'
        }
      },
      {
        path: '/passag/passageRecord',
        name: 'passageRecord',
        component: PassageRecord,
        meta: {
          title: '通行记录查询'
        }
      },
      {
        path: '/passag/exceptionRecord',
        name: 'exceptionRecord',
        component: ExceptionRecord,
        meta: {
          title: '异常通行记录'
        }
      },

      // 收费管理
      {
        path: '/charge/chargeFlow',
        name: 'chargeFlow',
        component: ChargeFlow,
        meta: {
          title: '收费流水'
        }
      },
      {
        path: '/charge/orderReconcile',
        name: 'orderReconcile',
        component: OrderReconcile,
        meta: {
          title: '订单对账'
        }
      },
      {
        path: '/charge/couponConfig',
        name: 'couponConfig',
        component: CouponConfig,
        meta: {
          title: '优惠配置'
        }
      },
      {
        path: '/charge/invoiceManage',
        name: 'invoiceManage',
        component: InvoiceManage,
        meta: {
          title: '发票管理'
        }
      },

      // 会员管理
      {
        path: '/member/memberList',
        name: 'memberList',
        component: MemberList,
        meta: {
          title: '固定车主列表'
        }
      },
      {
        path: '/member/packageConfig',
        name: 'packageConfig',
        component: PackageConfig,
        meta: {
          title: '套餐配置'
        }
      },
      {
        path: '/member/renewalRecord',
        name: 'renewalRecord',
        component: RenewalRecord,
        meta: {
          title: '续费记录'
        }
      },

      // 设备管理
      {
        path: '/device/deviceList',
        name: 'deviceList',
        component: DeviceList,
        meta: {
          title: '设备列表'
        }
      },
      {
        path: '/device/faultRepair',
        name: 'faultRepair',
        component: FaultRepair,
        meta: {
          title: '故障报修'
        }
      },

      // 数据报表
      {
        path: '/report/revenueReport',
        name: 'revenueReport',
        component: RevenueReport,
        meta: {
          title: '营收统计'
        }
      },
      {
        path: '/report/trafficReport',
        name: 'trafficReport',
        component: TrafficReport,
        meta: {
          title: '车流量报表'
        }
      },
      {
        path: '/report/spaceUsageReport',
        name: 'spaceUsageReport',
        component: SpaceUsageReport,
        meta: {
          title: '车位利用率'
        }
      },
      {
        path: '/report/exportReport',
        name: 'exportReport',
        component: ExportReport,
        meta: {
          title: '导出报表'
        }
      },

      // 系统设置
      {
        path: '/system/adminAccount',
        name: 'adminAccount',
        component: AdminAccount,
        meta: {
          title: '管理员账号'
        }
      },
      {
        path: '/system/rolePermission',
        name: 'rolePermission',
        component: RolePermission,
        meta: {
          title: '角色权限'
        }
      },
      {
        path: '/system/chargeRuleConfig',
        name: 'chargeRuleConfig',
        component: ChargeRuleConfig,
        meta: {
          title: '收费规则配置'
        }
      },
      {
        path: '/system/smsConfig',
        name: 'smsConfig',
        component: SmsConfig,
        meta: {
          title: '短信配置'
        }
      },
      {
        path: '/system/logManage',
        name: 'logManage',
        component: LogManage,
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
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 智慧停车场管理平台`
  }

  // 判断是否需要登录
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next({ name: 'login' })
  } else if (to.path === '/login' && token) {
    next({ name: 'home' })
  } else {
    next()
  }
})

export default router