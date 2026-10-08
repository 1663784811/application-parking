<template>
  <div class="role-permission-page">
    <div class="filter-bar">
      <div class="filter-actions">
        <Button type="primary" @click="handleAdd"><Icon type="ios-add" />新增角色</Button>
      </div>
    </div>
    <div class="role-list">
      <div v-for="item in state.tableData" :key="item.id" class="role-card" :class="{ active: state.currentRole?.id === item.id }" @click="handleSelectRole(item)">
        <div class="role-header">
          <span class="role-name">{{ item.name }}</span>
          <span class="role-status">{{ item.isSystem ? '系统角色' : '自定义' }}</span>
        </div>
        <div class="role-desc">{{ item.description }}</div>
        <div class="role-info"><span>成员：{{ item.memberCount }}人</span><span class="role-actions"><Button type="text" size="small" @click.stop="handleEdit(item)">编辑</Button><Button type="text" size="small" @click.stop="handleDelete(item)" class="text-danger" v-if="!item.isSystem">删除</Button></span></div>
      </div>
    </div>
    <div class="permission-panel" v-if="state.currentRole">
      <div class="panel-header">
        <h3>{{ state.currentRole.name }} - 权限配置</h3>
        <p class="panel-tip">下方为菜单结构参考，权限分配功能待 RBAC 菜单-权限映射改造后对接</p>
      </div>
      <div class="permission-tree">
        <Tree :data="state.permissionData" show-checkbox check-directly></Tree>
      </div>
      <div class="panel-footer"><Button type="primary" @click="handleSavePermission">保存配置</Button></div>
    </div>
    <Modal v-model="state.modalVisible" :title="state.modalType === 'add' ? '新增角色' : '编辑角色'" width="1000">
      <Form :model="state.formData" :rules="state.rules" :label-width="100" class="modal-form-2col">
        <FormItem label="角色名称" prop="name"><Input v-model="state.formData.name" placeholder="请输入角色名称" /></FormItem>
        <FormItem label="描述" prop="description"><Input v-model="state.formData.description" type="textarea" :rows="3" placeholder="请输入描述" /></FormItem>
      </Form>
      <template #footer><Button @click="state.modalVisible = false">取消</Button><Button type="primary" @click="handleSubmit">确定</Button></template>
    </Modal>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { Button, Icon, Tree, Modal, Form, FormItem, Input, Message } from 'view-ui-plus'
import { systemApi } from '@/api'

// 权限面板的菜单结构参考（静态，权限分配功能待 RBAC 菜单-权限映射改造后对接）
const state = reactive({
  tableData: [],
  loading: false,
  currentRole: null,
  permissionData: [
    { title: '工作台', expand: true, children: [{ title: '首页查看' }, { title: '数据统计' }] },
    { title: '停车场管理', expand: true, children: [{ title: '停车场列表' }, { title: '车位管理' }] },
    { title: '车辆通行', expand: true, children: [{ title: '实时监控' }, { title: '通行记录' }, { title: '异常记录' }] },
    { title: '收费管理', children: [{ title: '收费流水' }, { title: '订单对账' }, { title: '优惠配置' }] },
    { title: '会员管理', children: [{ title: '会员列表' }, { title: '套餐配置' }, { title: '续费记录' }] },
    { title: '设备管理', children: [{ title: '设备列表' }, { title: '物模型' }] },
    { title: '数据报表', children: [{ title: '营收统计' }, { title: '车流量报表' }, { title: '车位利用率' }, { title: '导出报表' }] },
    { title: '系统设置', children: [{ title: '管理员账号' }, { title: '角色权限' }, { title: '收费规则' }, { title: '短信配置' }, { title: '日志管理' }] }
  ],
  modalVisible: false,
  modalType: 'add',
  formData: { id: null, name: '', description: '' },
  rules: { name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }] }
})

// 角色列表（含各角色成员数，按创建时间倒序）
const loadData = async () => {
  state.loading = true
  try {
    const res = await systemApi.getRoleList()
    state.tableData = res.data || []
    // 若当前未选中角色，则默认选中首个以展示权限面板
    if (state.tableData.length && !state.currentRole) {
      state.currentRole = state.tableData[0]
    }
  } catch (e) {
    console.error('获取角色列表失败', e)
  } finally {
    state.loading = false
  }
}

const handleSelectRole = r => { state.currentRole = r }

const handleAdd = () => {
  state.modalType = 'add'
  state.formData = { id: null, name: '', description: '' }
  state.modalVisible = true
}

const handleEdit = r => {
  state.modalType = 'edit'
  state.formData = { ...r }
  state.modalVisible = true
}

const handleSubmit = async () => {
  if (!state.formData.name) {
    Message.warning('请输入角色名称')
    return
  }
  try {
    const payload = { ...state.formData }
    if (state.modalType === 'add') {
      await systemApi.addRole(payload)
      Message.success('新增成功')
    } else {
      await systemApi.editRole(payload)
      Message.success('编辑成功')
    }
    state.modalVisible = false
    loadData()
  } catch (e) {
    console.error('保存角色失败', e)
  }
}

const handleDelete = r => {
  const tip = r.memberCount > 0
    ? `角色"${r.name}"下有 ${r.memberCount} 名成员，删除后成员将失去该角色权限，确认删除？`
    : `删除角色"${r.name}"？`
  Modal.confirm({
    title: '确认删除',
    content: tip,
    onOk: async () => {
      try {
        await systemApi.deleteRole(r.id)
        Message.success('删除成功')
        if (state.currentRole && state.currentRole.id === r.id) {
          state.currentRole = null
        }
        loadData()
      } catch (e) {
        console.error('删除角色失败', e)
      }
    }
  })
}

// 权限分配功能待 RBAC 菜单-权限映射改造后对接
const handleSavePermission = () => {
  Message.info('权限分配功能待 RBAC 菜单-权限映射改造后对接')
}

onMounted(() => {
  loadData()
})
</script>

<style lang="less" scoped>
.role-permission-page {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: var(--spacing-lg);
  min-height: calc(100vh - var(--header-height) - 64px);

  .filter-bar {
    grid-column: 1 / -1;
    padding: var(--spacing-xl);
    background: var(--bg-color);
    border-radius: var(--border-radius-base);
    box-shadow: var(--shadow-base);
  }

  .filter-actions {
    display: flex;
    gap: var(--spacing-md);
  }

  .role-list {
    display: flex;
    flex-direction: column;
    gap: var(--spacing-md);
    background: var(--bg-color);
    border-radius: var(--border-radius-base);
    box-shadow: var(--shadow-base);
    padding: var(--spacing-lg);

    .role-card {
      padding: var(--spacing-lg);
      border: 1px solid var(--border-color);
      border-radius: var(--border-radius-base);
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        border-color: var(--primary-color);
      }

      &.active {
        border-color: var(--primary-color);
        background: rgba(22, 93, 255, 0.05);
      }

      .role-header {
        display: flex;
        justify-content: space-between;
        margin-bottom: var(--spacing-sm);

        .role-name {
          font-weight: 600;
          color: var(--text-color-title);
        }

        .role-status {
          font-size: var(--font-size-xs);
          color: var(--text-color-secondary);
        }
      }

      .role-desc {
        font-size: var(--font-size-sm);
        color: var(--text-color-secondary);
        margin-bottom: var(--spacing-md);
      }

      .role-info {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: var(--font-size-sm);
        color: var(--text-color-secondary);

        .role-actions {
          display: flex;
          gap: var(--spacing-sm);
        }
      }
    }
  }

  .text-danger {
    color: var(--error-color);
  }

  .permission-panel {
    background: var(--bg-color);
    border-radius: var(--border-radius-base);
    box-shadow: var(--shadow-base);
    display: flex;
    flex-direction: column;

    .panel-header {
      padding: var(--spacing-xl);
      border-bottom: 1px solid var(--border-color);

      h3 {
        font-size: var(--font-size-md);
        font-weight: 600;
      }

      .panel-tip {
        margin-top: var(--spacing-sm);
        font-size: var(--font-size-xs);
        color: var(--text-color-secondary);
      }
    }

    .permission-tree {
      flex: 1;
      padding: var(--spacing-xl);
      overflow-y: auto;
    }

    .panel-footer {
      padding: var(--spacing-lg);
      border-top: 1px solid var(--border-color);
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>