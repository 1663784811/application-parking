<template>
  <div class="vehicle-add-page">
    <van-nav-bar title="添加车辆" left-arrow @click-left="router.back()" />

    <van-form class="form-content" @submit="handleSubmit">
      <van-cell-group inset>
        <van-field
          v-model="state.plateNumber"
          name="plateNumber"
          label="车牌号"
          placeholder="请输入车牌号"
          maxlength="8"
          :formatter="formatPlate"
        />

        <van-field
          v-model="state.vehicleType"
          name="vehicleType"
          label="车辆类型"
          is-link
          readonly
        />

        <van-field name="isDefault" label="设为默认">
          <template #input>
            <van-switch v-model="state.isDefault" size="20" />
          </template>
        </van-field>
      </van-cell-group>

      <div class="submit-btn">
        <van-button type="primary" size="large" block native-type="submit">
          保存
        </van-button>
      </div>
    </van-form>

    <div class="tips">
      <p>温馨提示：</p>
      <p>1. 车牌号格式示例：京A12345</p>
      <p>2. 一辆车只能绑定一个账号</p>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'

const router = useRouter()

const state = reactive({
  plateNumber: '',
  vehicleType: '小型汽车',
  isDefault: false,
})

// 车牌正则
const platePattern = /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-Z0-9]{4,5}[A-Z0-9挂学警港澳]$/

// 车牌转大写
const formatPlate = (val) => val.toUpperCase()

const handleSubmit = () => {
  const plate = state.plateNumber.toUpperCase()
  if (!platePattern.test(plate)) {
    showToast('请输入正确的车牌号')
    return
  }
  state.plateNumber = plate
  // TODO: 调用添加车辆接口（addVehicle）
  showToast('添加成功')
  router.back()
}
</script>

<style scoped lang="less">
.vehicle-add-page {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: auto;

  .form-content {
    margin-top: 16px;
  }

  .submit-btn {
    padding: 24px 16px;

    .van-button {
      border-radius: 8px;
    }
  }

  .tips {
    padding: 16px;
    font-size: 12px;
    color: var(--text-secondary);
    line-height: 1.8;

    p:first-child {
      margin-bottom: 8px;
      font-weight: 600;
      color: var(--text-primary);
    }
  }
}
</style>
