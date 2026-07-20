<template>
  <div class="settings-page">
    <Alert type="warning" show-icon>
      只有管理员才能访问此页面。修改设置后将自动保存，部分设置需要重启设备后生效。
    </Alert>

    <div class="settings-content">
      <!-- 设备参数设置 -->
      <div class="settings-card">
        <div class="card-header">
          <h3><Icon type="ios-construct-outline" /> 设备参数设置</h3>
        </div>
        <div class="card-body">
          <Form :label-width="150">
            <FormItem label="识别灵敏度">
              <Slider v-model="deviceParams.sensitivity" :min="1" :max="10" :step="1" show-input />
              <span class="param-hint">数值越高，识别越灵敏，但可能误识别</span>
            </FormItem>
            <FormItem label="道闸开速度">
              <Select v-model="deviceParams.gateSpeed" style="width: 200px">
                <Option value="fast">快速（1秒）</Option>
                <Option value="normal">正常（2秒）</Option>
                <Option value="slow">慢速（3秒）</Option>
              </Select>
            </FormItem>
            <FormItem label="地感灵敏度">
              <Slider v-model="deviceParams.sensorSensitivity" :min="1" :max="10" :step="1" show-input />
            </FormItem>
            <FormItem label="语音播报">
              <i-switch v-model="deviceParams.voiceEnabled" />
              <span class="switch-hint">{{ deviceParams.voiceEnabled ? '已开启' : '已关闭' }}</span>
            </FormItem>
            <FormItem label="语音音量">
              <Slider v-model="deviceParams.voiceVolume" :min="0" :max="100" :step="5" :disabled="!deviceParams.voiceEnabled" show-input />
            </FormItem>
            <FormItem>
              <Button type="primary" @click="handleSaveDeviceParams" :loading="saving">
                保存并应用
              </Button>
              <Button @click="handleRestartDevice">
                <Icon type="ios-refresh" />
                重启设备生效
              </Button>
            </FormItem>
          </Form>
        </div>
      </div>

      <!-- 联系方式设置 -->
      <div class="settings-card">
        <div class="card-header">
          <h3><Icon type="ios-call-outline" /> 联系方式设置</h3>
        </div>
        <div class="card-body">
          <Form :label-width="150" ref="contactFormRef" :model="contactForm" :rules="contactRules">
            <FormItem label="现场值守电话">
              <Input v-model="contactForm.guardPhone" placeholder="请输入值守室联系电话" style="width: 200px" />
            </FormItem>
            <FormItem label="管理员电话">
              <Input v-model="contactForm.adminPhone" placeholder="请输入管理员联系电话" style="width: 200px" />
            </FormItem>
            <FormItem label="紧急求助电话">
              <Input v-model="contactForm.emergencyPhone" placeholder="请输入紧急求助电话" style="width: 200px" />
            </FormItem>
            <FormItem label="物业电话">
              <Input v-model="contactForm.propertyPhone" placeholder="请输入物业电话" style="width: 200px" />
            </FormItem>
            <FormItem>
              <Button type="primary" @click="handleSaveContact">
                保存联系方式
              </Button>
            </FormItem>
          </Form>
        </div>
      </div>

      <!-- 权限设置 -->
      <div class="settings-card">
        <div class="card-header">
          <h3><Icon type="ios-people-outline" /> 权限设置</h3>
        </div>
        <div class="card-body">
          <div class="staff-list">
            <div class="list-header">
              <span>值守人员</span>
              <Button size="small" @click="handleAddStaff">
                <Icon type="ios-add" />
                添加人员
              </Button>
            </div>
            <Table :columns="staffColumns" :data="staffList" size="small">
              <template #role="{ row }">
                <Tag :color="row.role === 'admin' ? 'error' : 'default'">
                  {{ row.role === 'admin' ? '管理员' : '值守员' }}
                </Tag>
              </template>
              <template #action="{ row }">
                <Button type="text" size="small" @click="handleEditStaff(row)">编辑</Button>
                <Button type="text" size="small" @click="handleDeleteStaff(row)" class="text-danger">删除</Button>
              </template>
            </Table>
          </div>

          <Divider />

          <div class="password-section">
            <h4>修改密码</h4>
            <Form :label-width="100" style="width: 400px">
              <FormItem label="当前密码">
                <Input type="password" v-model="passwordForm.oldPassword" password placeholder="请输入当前密码" />
              </FormItem>
              <FormItem label="新密码">
                <Input type="password" v-model="passwordForm.newPassword" password placeholder="请输入新密码" />
              </FormItem>
              <FormItem label="确认密码">
                <Input type="password" v-model="passwordForm.confirmPassword" password placeholder="请确认新密码" />
              </FormItem>
              <FormItem>
                <Button type="primary" @click="handleChangePassword">
                  修改密码
                </Button>
              </FormItem>
            </Form>
          </div>
        </div>
      </div>

      <!-- 系统设置 -->
      <div class="settings-card">
        <div class="card-header">
          <h3><Icon type="ios-settings-outline" /> 系统设置</h3>
        </div>
        <div class="card-body">
          <Form :label-width="150">
            <FormItem label="刷新频率">
              <Select v-model="systemSettings.refreshInterval" style="width: 200px">
                <Option :value="1000">1秒</Option>
                <Option :value="3000">3秒（推荐）</Option>
                <Option :value="5000">5秒</Option>
                <Option :value="10000">10秒</Option>
              </Select>
            </FormItem>
            <FormItem label="提示音">
              <i-switch v-model="systemSettings.soundEnabled" />
            </FormItem>
            <FormItem label="自动锁屏时间">
              <Select v-model="systemSettings.autoLockTime" style="width: 200px">
                <Option :value="5">5分钟</Option>
                <Option :value="10">10分钟</Option>
                <Option :value="30">30分钟</Option>
                <Option :value="0">从不</Option>
              </Select>
            </FormItem>
            <FormItem label="深色模式">
              <i-switch v-model="systemSettings.darkMode" />
            </FormItem>
            <FormItem>
              <Button type="primary" @click="handleSaveSystemSettings">
                保存系统设置
              </Button>
            </FormItem>
          </Form>
        </div>
      </div>
    </div>

    <!-- 添加人员弹窗 -->
    <Modal v-model="staffModal.visible" :title="staffModal.isEdit ? '编辑人员' : '添加人员'" width="400">
      <Form :label-width="80">
        <FormItem label="姓名">
          <Input v-model="staffForm.name" placeholder="请输入姓名" />
        </FormItem>
        <FormItem label="账号">
          <Input v-model="staffForm.username" placeholder="请输入账号" :disabled="staffModal.isEdit" />
        </FormItem>
        <FormItem label="密码" v-if="!staffModal.isEdit">
          <Input type="password" v-model="staffForm.password" password placeholder="请输入密码" />
        </FormItem>
        <FormItem label="角色">
          <RadioGroup v-model="staffForm.role">
            <Radio label="guard">值守员</Radio>
            <Radio label="admin">管理员</Radio>
          </RadioGroup>
        </FormItem>
      </Form>
      <template #footer>
        <Button @click="staffModal.visible = false">取消</Button>
        <Button type="primary" @click="handleSubmitStaff">确定</Button>
      </template>
    </Modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Alert, Button, Icon, Form, FormItem, Input, Select, Option, Slider, iSwitch, Table, Tag, Divider, Modal, Message } from 'view-ui-plus'

// 设备参数
const deviceParams = reactive({
  sensitivity: 7,
  gateSpeed: 'normal',
  sensorSensitivity: 5,
  voiceEnabled: true,
  voiceVolume: 70
})

// 联系方式
const contactForm = reactive({
  guardPhone: '010-12345678',
  adminPhone: '13800138000',
  emergencyPhone: '400-888-8888',
  propertyPhone: '010-87654321'
})

const contactFormRef = ref(null)
const contactRules = {
  guardPhone: [{ required: true, message: '请输入值守电话' }],
  adminPhone: [{ required: true, message: '请输入管理员电话' }]
}

// 员工列表
const staffColumns = [
  { title: '姓名', key: 'name', minWidth: 100 },
  { title: '账号', key: 'username', minWidth: 120 },
  { title: '角色', slot: 'role', minWidth: 100 },
  { title: '联系电话', key: 'phone', minWidth: 120 },
  { title: '操作', slot: 'action', minWidth: 100, fixed: 'right' }
]

const staffList = ref([
  { id: 1, name: '张三', username: 'zhangsan', role: 'guard', phone: '13800138001' },
  { id: 2, name: '李四', username: 'lisi', role: 'guard', phone: '13800138002' },
  { id: 3, name: '王五', username: 'admin', role: 'admin', phone: '13800138003' }
])

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 系统设置
const systemSettings = reactive({
  refreshInterval: 3000,
  soundEnabled: true,
  autoLockTime: 10,
  darkMode: false
})

// 员工弹窗
const staffModal = reactive({
  visible: false,
  isEdit: false
})

const staffForm = reactive({
  name: '',
  username: '',
  password: '',
  role: 'guard'
})

// 状态
const saving = ref(false)

// 方法
const handleSaveDeviceParams = () => {
  saving.value = true
  setTimeout(() => {
    saving.value = false
    Message.success('设备参数已保存')
  }, 500)
}

const handleRestartDevice = () => {
  Modal.confirm({
    title: '确认重启设备',
    content: '确定要重启所有设备吗？重启期间将无法正常通行。',
    okText: '确认重启',
    onOk: () => {
      Message.info('设备重启中...')
      setTimeout(() => {
        Message.success('设备重启完成')
      }, 3000)
    }
  })
}

const handleSaveContact = () => {
  Message.success('联系方式已保存')
}

const handleAddStaff = () => {
  staffForm.name = ''
  staffForm.username = ''
  staffForm.password = ''
  staffForm.role = 'guard'
  staffModal.isEdit = false
  staffModal.visible = true
}

const handleEditStaff = (row) => {
  staffForm.name = row.name
  staffForm.username = row.username
  staffForm.role = row.role
  staffModal.isEdit = true
  staffModal.visible = true
}

const handleDeleteStaff = (row) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除人员 "${row.name}" 吗？`,
    okText: '确认',
    okType: 'error',
    onOk: () => {
      const index = staffList.value.findIndex(s => s.id === row.id)
      if (index > -1) {
        staffList.value.splice(index, 1)
      }
      Message.success('删除成功')
    }
  })
}

const handleSubmitStaff = () => {
  if (!staffForm.name || !staffForm.username) {
    Message.warning('请填写完整信息')
    return
  }

  if (!staffModal.isEdit && !staffForm.password) {
    Message.warning('请输入密码')
    return
  }

  if (staffModal.isEdit) {
    const staff = staffList.value.find(s => s.username === staffForm.username)
    if (staff) {
      staff.name = staffForm.name
      staff.role = staffForm.role
    }
  } else {
    staffList.value.push({
      id: Date.now(),
      name: staffForm.name,
      username: staffForm.username,
      role: staffForm.role,
      phone: ''
    })
  }

  staffModal.visible = false
  Message.success(staffModal.isEdit ? '修改成功' : '添加成功')
}

const handleChangePassword = () => {
  if (!passwordForm.oldPassword) {
    Message.warning('请输入当前密码')
    return
  }
  if (!passwordForm.newPassword) {
    Message.warning('请输入新密码')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    Message.warning('两次输入的密码不一致')
    return
  }

  Message.success('密码修改成功')
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

const handleSaveSystemSettings = () => {
  Message.success('系统设置已保存')
}

// 生命周期
onMounted(() => {
  // 加载设置数据
})
</script>

<style lang="less" scoped>
.settings-page {
  max-width: 1200px;
}

.settings-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
  margin-top: var(--spacing-lg);
}

.settings-card {
  background-color: var(--color-bg-card);
  border-radius: var(--border-radius-base);
  box-shadow: var(--shadow-base);

  .card-header {
    padding: var(--spacing-md) var(--spacing-lg);
    border-bottom: 1px solid var(--color-border-light);

    h3 {
      font-size: var(--font-size-md);
      font-weight: var(--font-weight-bold);
      color: var(--color-title);
      display: flex;
      align-items: center;
      gap: var(--spacing-sm);
    }
  }

  .card-body {
    padding: var(--spacing-lg);

    :deep(.ivu-form-item) {
      margin-bottom: var(--spacing-lg);
    }
  }
}

.param-hint, .switch-hint {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
  margin-left: var(--spacing-md);
}

.staff-list {
  .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--spacing-md);
    font-weight: var(--font-weight-medium);
    color: var(--color-title);
  }
}

.text-danger {
  color: var(--color-danger);
}

.password-section {
  h4 {
    font-size: var(--font-size-sm);
    font-weight: var(--font-weight-medium);
    color: var(--color-title);
    margin-bottom: var(--spacing-md);
  }
}
</style>