<template>
  <div class="personal-page">
    <div class="personal-content">
      <!-- 左侧导航 -->
      <div class="profile-nav">
        <div class="user-card">
          <div class="avatar-wrapper">
            <div class="avatar">
              <Icon type="ios-person" />
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
              <FormItem label="所属部门">
                <Input v-model="state.infoForm.department" disabled />
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
            <Table
              :columns="logColumns"
              :data="state.logList"
              :loading="state.logLoading"
              size="small"
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
import { reactive, onMounted } from 'vue'
import { Icon, Form, FormItem, Input, Button, Tag, Switch, Upload, Table, Page, Message, Modal } from 'view-ui-plus'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const state = reactive({
  activeTab: 'info',
  saving: false,
  passwordSaving: false,
  notificationSaving: false,
  logLoading: false,

  userInfo: {
    name: '管理员',
    roleName: '超级管理员',
    twoFactorEnabled: false
  },

  infoForm: {
    username: 'admin',
    name: '管理员',
    phone: '138****8888',
    email: 'admin@example.com',
    department: '技术部'
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
  { title: '时间', key: 'time', minWidth: 180 },
  { title: '操作类型', slot: 'actionType', minWidth: 120 },
  { title: '操作内容', key: 'content' },
  { title: 'IP地址', key: 'ip', minWidth: 140 },
  { title: '设备', key: 'device', minWidth: 160 }
]

const handleSaveInfo = () => {
  state.saving = true
  setTimeout(() => {
    Message.success('保存成功')
    state.saving = false
  }, 800)
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
  state.passwordSaving = true
  setTimeout(() => {
    Message.success('密码修改成功')
    state.passwordForm = { oldPassword: '', newPassword: '', confirmPassword: '' }
    state.passwordSaving = false
    state.activeTab = 'info'
  }, 800)
}

const handleResetPassword = () => {
  state.passwordForm = { oldPassword: '', newPassword: '', confirmPassword: '' }
}

const handleSaveNotification = () => {
  state.notificationSaving = true
  setTimeout(() => {
    Message.success('保存成功')
    state.notificationSaving = false
  }, 500)
}

const handleAvatarSuccess = () => {
  Message.success('头像更新成功')
}

const getActionColor = (type) => {
  const colors = { login: 'blue', edit: 'green', delete: 'red', export: 'orange' }
  return colors[type] || 'default'
}

const getActionText = (type) => {
  const texts = { login: '登录', edit: '编辑', delete: '删除', export: '导出' }
  return texts[type] || type
}

const loadLogList = () => {
  state.logLoading = true
  setTimeout(() => {
    state.logList = [
      { time: '2024-01-15 14:32:15', actionType: 'edit', content: '修改了停车场「中心停车场」的基本信息', ip: '192.168.1.100', device: 'Windows Chrome' },
      { time: '2024-01-15 10:15:00', actionType: 'export', content: '导出了2024年1月的营收报表', ip: '192.168.1.100', device: 'Windows Chrome' },
      { time: '2024-01-14 16:45:30', actionType: 'login', content: '管理员登录系统', ip: '192.168.1.100', device: 'Windows Chrome' },
      { time: '2024-01-14 09:20:00', actionType: 'edit', content: '添加了新设备「2号入口相机」', ip: '192.168.1.100', device: 'Windows Chrome' },
      { time: '2024-01-13 15:30:00', actionType: 'delete', content: '删除了用户「张三」的账号', ip: '192.168.1.100', device: 'Windows Chrome' }
    ]
    state.logTotal = 25
    state.logLoading = false
  }, 500)
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

onMounted(() => {
  if (userStore.state.userInfo) {
    state.userInfo.name = userStore.state.userInfo.name || '管理员'
  }
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

          .ivu-icon {
            font-size: 40px;
            color: #fff;
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