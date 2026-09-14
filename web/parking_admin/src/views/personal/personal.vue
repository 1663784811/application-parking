<template>
  <div class="personal-page">
    <div class="personal-content">
      <!-- 左侧导航 -->
      <div class="profile-nav">
        <div class="user-card">
          <div class="avatar-wrapper">
            <div class="avatar">
              <img v-if="state.userInfo.avatar" :src="state.userInfo.avatar" class="avatar-img" />
              <Icon v-else type="ios-person" />
            </div>
            <Upload
              :show-upload-list="false"
              :on-success="handleAvatarSuccess"
              :format="['jpg','jpeg','png']"
              :max-size="2048"
              action="#"
            >
              <div class="avatar-mask">
                <Icon type="ios-camera" />
              </div>
            </Upload>
          </div>
          <div class="user-info">
            <h3 class="user-name">{{ state.userInfo.name }}</h3>
            <p class="user-role">{{ state.userInfo.roleName }}</p>
          </div>
        </div>

        <div class="nav-list">
          <div
            v-for="item in navList"
            :key="item.key"
            class="nav-item"
            :class="{ active: state.activeTab === item.key }"
            @click="state.activeTab = item.key"
          >
            <Icon :type="item.icon" />
            <span>{{ item.title }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧内容 -->
      <div class="profile-content">
        <!-- 基本信息 -->
        <div v-show="state.activeTab === 'info'" class="content-panel">
          <div class="panel-header">
            <h3 class="panel-title">基本信息</h3>
          </div>
          <div class="panel-body">
            <Form :model="state.infoForm" :label-width="100" class="info-form">
              <FormItem label="用户名">
                <Input v-model="state.infoForm.username" disabled />
              </FormItem>
              <FormItem label="姓名">
                <Input v-model="state.infoForm.name" placeholder="请输入姓名" />
              </FormItem>
              <FormItem label="手机号">
                <Input v-model="state.infoForm.phone" placeholder="请输入手机号" />
              </FormItem>
              <FormItem label="邮箱">
                <Input v-model="state.infoForm.email" placeholder="请输入邮箱" />
              </FormItem>
              <FormItem label="昵称">
                <Input v-model="state.infoForm.nickName" placeholder="请输入昵称" maxlength="20" />
              </FormItem>
              <FormItem label="角色">
                <Tag color="blue">{{ state.userInfo.roleName }}</Tag>
              </FormItem>
              <FormItem>
                <Button type="primary" :loading="state.saving" @click="handleSaveInfo">
                  保存修改
                </Button>
              </FormItem>
            </Form>
          </div>
        </div>

        <!-- 修改密码 -->
        <div v-show="state.activeTab === 'password'" class="content-panel">
          <div class="panel-header">
            <h3 class="panel-title">修改密码</h3>
          </div>
          <div class="panel-body">
            <Form :model="state.passwordForm" :label-width="120" class="password-form">
              <FormItem label="当前密码">
                <Input
                  v-model="state.passwordForm.oldPassword"
                  type="password"
                  placeholder="请输入当前密码"
                />
              </FormItem>
              <FormItem label="新密码">
                <Input
                  v-model="state.passwordForm.newPassword"
                  type="password"
                  placeholder="请输入新密码"
                />
              </FormItem>
              <FormItem label="确认新密码">
                <Input
                  v-model="state.passwordForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入新密码"
                />
              </FormItem>
              <FormItem>
                <Button type="primary" :loading="state.passwordSaving" @click="handleChangePassword">
                  确认修改
                </Button>
                <Button style="margin-left: 12px" @click="handleResetPassword">
                  重置
                </Button>
              </FormItem>
            </Form>
          </div>
        </div>

        <!-- 账号安全 -->
        <div v-show="state.activeTab === 'security'" class="content-panel">
          <div class="panel-header">
            <h3 class="panel-title">账号安全</h3>
          </div>
          <div class="panel-body">
            <div class="security-item">
              <div class="security-info">
                <div class="security-icon success">
                  <Icon type="ios-phone-portrait" />
                </div>
                <div class="security-detail">
                  <h4>绑定手机</h4>
                  <p>已绑定手机：{{ state.infoForm.phone || '未绑定' }}</p>
                </div>
              </div>
              <Button size="small" @click="state.activeTab = 'info'">修改</Button>
            </div>

            <div class="security-item">
              <div class="security-info">
                <div class="security-icon success">
                  <Icon type="ios-mail" />
                </div>
                <div class="security-detail">
                  <h4>绑定邮箱</h4>
                  <p>已绑定邮箱：{{ state.infoForm.email || '未绑定' }}</p>
                </div>
              </div>
              <Button size="small" @click="state.activeTab = 'info'">修改</Button>
            </div>

            <div class="security-item">
              <div class="security-info">
                <div class="security-icon" :class="state.userInfo.twoFactorEnabled ? 'success' : 'warning'">
                  <Icon type="ios-shield" />
                </div>
                <div class="security-detail">
                  <h4>两步验证</h4>
                  <p>{{ state.userInfo.twoFactorEnabled ? '已开启' : '未开启' }}</p>
                </div>
              </div>
              <Button size="small" type="primary" ghost>
                {{ state.userInfo.twoFactorEnabled ? '关闭' : '开启' }}
              </Button>
            </div>

            <div class="security-item">
              <div class="security-info">
                <div class="security-icon">
                  <Icon type="ios-keypad" />
                </div>
                <div class="security-detail">
                  <h4>登录日志</h4>
                  <p>查看账号登录记录</p>
                </div>
              </div>
              <Button size="small" @click="showLoginLog">查看详情</Button>
            </div>
          </div>
        </div>

        <!-- 通知设置 -->
        <div v-show="state.activeTab === 'notification'" class="content-panel">
          <div class="panel-header">
            <h3 class="panel-title">通知设置</h3>
          </div>
          <div class="panel-body">
            <div class="notification-list">
              <div class="notification-item">
                <div class="notification-info">
                  <h4>账单通知</h4>
                  <p>接收每日账单汇总邮件</p>
                </div>
                <i-switch v-model="state.notifications.bill" />
              </div>

              <div class="notification-item">
                <div class="notification-info">
                  <h4>异常告警</h4>
                  <p>停车场设备异常时接收通知</p>
                </div>
                <i-switch v-model="state.notifications.alert" />
              </div>

              <div class="notification-item">
                <div class="notification-info">
                  <h4>系统公告</h4>
                  <p>接收系统更新和新功能通知</p>
                </div>
                <i-switch v-model="state.notifications.announcement" />
              </div>

              <div class="notification-item">
                <div class="notification-info">
                  <h4>安全提醒</h4>
                  <p>异地登录和异常操作提醒</p>
                </div>
                <i-switch v-model="state.notifications.security" />
              </div>
            </div>

            <div class="save-bar">
              <Button type="primary" :loading="state.notificationSaving" @click="handleSaveNotification">
                保存设置
              </Button>
            </div>
          </div>
        </div>

        <!-- 操作日志 -->
        <div v-show="state.activeTab === 'log'" class="content-panel">
          <div class="panel-header">
            <h3 class="panel-title">操作日志</h3>
          </div>
          <div class="panel-body">
            <TableColumnSetting :columns="logColumns" v-model:visible="visibleFields" v-model:open="colSettingVisible" @reset="resetColumns" />
            <Table
              :columns="displayColumns"
              :data="state.logList"
              :loading="state.logLoading"
            >
              <template #actionType="{ row }">
                <Tag :color="getActionColor(row.actionType)">
                  {{ getActionText(row.actionType) }}
                </Tag>
              </template>
            </Table>
            <div class="pagination-wrapper">
              <Page
                v-model:current="state.logPage"
                :total="state.logTotal"
                :page-size="state.logPageSize"
                show-elevator
                show-sizer
                @on-change="loadLogList"
                @on-page-size-change="handlePageSizeChange"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted, watch } from 'vue'
import { Icon, Form, FormItem, Input, Button, Tag, Switch, Upload, Table, Page, Message, Modal } from 'view-ui-plus'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api'
import TableColumnSetting from '@/components/TableColumnSetting.vue'
import { useTableColumns } from '@/composables/useTableColumns'

const userStore = useUserStore()

// 角色 → 中文展示
const ROLE_MAP = {
  Admin: '管理员',
  User: '普通用户',
  Store: '门店管理员',
  Root: '超级管理员'
}

const state = reactive({
  activeTab: 'info',
  saving: false,
  passwordSaving: false,
  notificationSaving: false,
  logLoading: false,

  // 顶部用户卡片展示信息（由 findUserInfo 映射）
  userInfo: {
    name: '',
    roleName: '',
    avatar: '',
    twoFactorEnabled: false // 后端未提供两步验证字段，默认未开启
  },

  // 基本信息表单（字段对齐 AuAdmin：account/realName/nickName/phone/email）
  infoForm: {
    username: '',   // 账号 account（不可改）
    name: '',       // 真实姓名 realName
    nickName: '',   // 昵称 nickName
    phone: '',      // 手机号 phone
    email: ''       // 邮箱 email
  },

  passwordForm: {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  },

  notifications: {
    bill: true,
    alert: true,
    announcement: false,
    security: true
  },

  logList: [],
  logPage: 1,
  logPageSize: 10,
  logTotal: 0
})

const navList = [
  { key: 'info', title: '基本信息', icon: 'ios-person' },
  { key: 'password', title: '修改密码', icon: 'ios-key' },
  { key: 'security', title: '账号安全', icon: 'ios-shield' },
  { key: 'notification', title: '通知设置', icon: 'ios-notifications' },
  { key: 'log', title: '操作日志', icon: 'ios-list' }
]

const logColumns = [
  { field: 'time', title: '时间', key: 'time', minWidth: 180 },
  { field: 'actionType', title: '操作类型', slot: 'actionType', minWidth: 120 },
  { field: 'content', title: '操作内容', key: 'content' },
  { field: 'ip', title: 'IP地址', key: 'ip', minWidth: 140 },
  { field: 'device', title: '设备', key: 'device', minWidth: 160 }
]

const { visibleFields, colSettingVisible, displayColumns, resetColumns } = useTableColumns(logColumns, 'personal:log:columnVisible')

// 基本信息保存：后端个人中心接口暂未提供资料修改接口，不模拟成功
const handleSaveInfo = () => {
  Message.info('个人资料修改接口尚未提供')
}

const handleChangePassword = () => {
  if (!state.passwordForm.oldPassword) {
    Message.warning('请输入当前密码')
    return
  }
  if (!state.passwordForm.newPassword) {
    Message.warning('请输入新密码')
    return
  }
  if (state.passwordForm.newPassword.length < 6) {
    Message.warning('密码长度不能少于6位')
    return
  }
  if (state.passwordForm.newPassword !== state.passwordForm.confirmPassword) {
    Message.warning('两次输入的密码不一致')
    return
  }
  Message.info('修改密码接口尚未提供')
}

const handleResetPassword = () => {
  state.passwordForm = { oldPassword: '', newPassword: '', confirmPassword: '' }
}

// 通知设置保存：后端暂未提供，不模拟成功
const handleSaveNotification = () => {
  Message.info('通知设置接口尚未提供')
}

// 头像上传：后端暂未提供上传接口
const handleAvatarSuccess = () => {
  Message.info('头像上传接口尚未提供')
}

const getActionColor = (type) => {
  const colors = { login: 'blue', edit: 'green', delete: 'red', export: 'orange' }
  return colors[type] || 'default'
}

const getActionText = (type) => {
  const texts = { login: '登录', edit: '编辑', delete: '删除', export: '导出' }
  return texts[type] || type
}

// 操作日志：后端个人中心接口未提供，暂无数据
const loadLogList = () => {
  state.logList = []
  state.logTotal = 0
}

const handlePageSizeChange = (size) => {
  state.logPageSize = size
  loadLogList()
}

const showLoginLog = () => {
  Modal.info({
    title: '登录日志',
    content: '该功能正在开发中...'
  })
}

// 将 findUserInfo 返回映射到页面展示状态
// findUserInfo.data = { baseInfo, role, permission, auEnterprise }
// baseInfo（Admin 角色）= AuAdmin：account/phone/nickName/avatar/realName/gender/birthday/email
const mapUserInfo = (info) => {
  if (!info) return
  const base = info.baseInfo || {}
  state.userInfo.name = base.nickName || base.realName || base.account || '管理员'
  state.userInfo.roleName = ROLE_MAP[info.role] || info.role || '—'
  state.userInfo.avatar = base.avatar || ''
  state.infoForm.username = base.account || ''
  state.infoForm.name = base.realName || ''
  state.infoForm.nickName = base.nickName || ''
  state.infoForm.phone = base.phone || ''
  state.infoForm.email = base.email || ''
}

// 拉取当前登录用户信息：优先复用 store（MainLayout 已拉取），否则主动调 findUserInfo
const loadUserInfo = async () => {
  if (userStore.state.userInfo) {
    mapUserInfo(userStore.state.userInfo)
    return
  }
  try {
    const res = await userApi.findUserInfo()
    userStore.setUserInfo(res.data)
    mapUserInfo(res.data)
  } catch (e) {
    // 获取失败由 authRequest 拦截器提示
  }
}

// store 中用户信息异步到达时同步映射
watch(() => userStore.state.userInfo, (info) => {
  mapUserInfo(info)
})

onMounted(() => {
  loadUserInfo()
  loadLogList()
})
</script>

<style lang="less" scoped>
.personal-page {
  flex: 1;
  display: flex;
  flex-direction: column;

  .personal-content {
    display: flex;
    gap: var(--spacing-xl);
  }

  .profile-nav {
    width: 280px;
    flex-shrink: 0;

    .user-card {
      background-color: var(--bg-color);
      border-radius: var(--border-radius-base);
      box-shadow: var(--shadow-base);
      padding: var(--spacing-xl);
      text-align: center;
      margin-bottom: var(--spacing-lg);

      .avatar-wrapper {
        position: relative;
        display: inline-block;
        margin-bottom: var(--spacing-lg);

        .avatar {
          width: 80px;
          height: 80px;
          border-radius: 50%;
          background: linear-gradient(135deg, #165DFF, #4080FF);
          display: flex;
          align-items: center;
          justify-content: center;
          overflow: hidden;

          .ivu-icon {
            font-size: 40px;
            color: #fff;
          }

          .avatar-img {
            width: 100%;
            height: 100%;
            border-radius: 50%;
            object-fit: cover;
          }
        }

        .avatar-mask {
          position: absolute;
          top: 0;
          left: 0;
          width: 80px;
          height: 80px;
          border-radius: 50%;
          background-color: rgba(0, 0, 0, 0.5);
          display: flex;
          align-items: center;
          justify-content: center;
          opacity: 0;
          cursor: pointer;
          transition: opacity 0.3s;

          .ivu-icon {
            font-size: 24px;
            color: #fff;
          }
        }

        &:hover .avatar-mask {
          opacity: 1;
        }
      }

      .user-name {
        font-size: var(--font-size-md);
        font-weight: 600;
        color: var(--text-color-title);
        margin-bottom: var(--spacing-xs);
      }

      .user-role {
        font-size: var(--font-size-sm);
        color: var(--text-color-secondary);
      }
    }

    .nav-list {
      background-color: var(--bg-color);
      border-radius: var(--border-radius-base);
      box-shadow: var(--shadow-base);
      padding: var(--spacing-sm);

      .nav-item {
        display: flex;
        align-items: center;
        padding: var(--spacing-md) var(--spacing-lg);
        border-radius: var(--border-radius-sm);
        cursor: pointer;
        transition: all 0.2s;
        color: var(--text-color);

        .ivu-icon {
          font-size: 18px;
          margin-right: var(--spacing-md);
        }

        &:hover {
          background-color: var(--bg-color-hover);
          color: var(--primary-color);
        }

        &.active {
          background-color: var(--primary-color);
          color: #fff;
        }
      }
    }
  }

  .profile-content {
    flex: 1;
    min-width: 0;

    .content-panel {
      background-color: var(--bg-color);
      border-radius: var(--border-radius-base);
      box-shadow: var(--shadow-base);

      .panel-header {
        padding: var(--spacing-lg) var(--spacing-xl);
        border-bottom: 1px solid var(--border-color);

        .panel-title {
          font-size: var(--font-size-md);
          font-weight: 600;
          color: var(--text-color-title);
        }
      }

      .panel-body {
        padding: var(--spacing-xl);
      }
    }

    .info-form,
    .password-form {
      max-width: 500px;
    }

    .security-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: var(--spacing-lg) 0;
      border-bottom: 1px solid var(--border-color);

      &:last-child {
        border-bottom: none;
      }

      .security-info {
        display: flex;
        align-items: center;

        .security-icon {
          width: 44px;
          height: 44px;
          border-radius: var(--border-radius-base);
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: var(--spacing-lg);
          background-color: var(--bg-color-page);

          .ivu-icon {
            font-size: 20px;
            color: var(--text-color-secondary);
          }

          &.success {
            background-color: rgba(52, 199, 98, 0.1);
            .ivu-icon {
              color: var(--success-color);
            }
          }

          &.warning {
            background-color: rgba(255, 149, 0, 0.1);
            .ivu-icon {
              color: var(--warning-color);
            }
          }
        }

        .security-detail {
          h4 {
            font-size: var(--font-size-sm);
            font-weight: 600;
            color: var(--text-color-title);
            margin-bottom: var(--spacing-xs);
          }

          p {
            font-size: var(--font-size-sm);
            color: var(--text-color-secondary);
          }
        }
      }
    }

    .notification-list {
      .notification-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: var(--spacing-lg) 0;
        border-bottom: 1px solid var(--border-color);

        &:last-child {
          border-bottom: none;
        }

        .notification-info {
          h4 {
            font-size: var(--font-size-sm);
            font-weight: 600;
            color: var(--text-color-title);
            margin-bottom: var(--spacing-xs);
          }

          p {
            font-size: var(--font-size-sm);
            color: var(--text-color-secondary);
          }
        }
      }
    }

    .save-bar {
      margin-top: var(--spacing-xl);
      padding-top: var(--spacing-xl);
      border-top: 1px solid var(--border-color);
    }

    .pagination-wrapper {
      margin-top: var(--spacing-lg);
      text-align: right;
    }
  }
}
</style>