<template>
  <div class="vehicle-edit-page">
    <van-nav-bar title="编辑车辆" left-arrow @click-left="router.back()" />

    <van-loading v-if="state.loading" class="loading-center" />

    <van-form
      v-else
      class="form-content"
      @submit="handleSubmit"
    >
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
            <van-switch v-model="state.isDefault" size="20" @change="handleSetDefault" />
          </template>
        </van-field>
      </van-cell-group>

      <div class="submit-btn">
        <van-button type="primary" size="large" block native-type="submit">
          保存
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'

const route = useRoute()
const router = useRouter()

const state = reactive({
  plateNumber: '',
  vehicleType: '小型汽车',
  isDefault: false,
  loading: true,
})

const platePattern = /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-Z0-9]{4,5}[A-Z0-9挂学警港澳]$/

const formatPlate = (val) => val.toUpperCase()

onMounted(() => {
  const vehicleId = route.params.vehicleId
  // TODO: 接口获取车辆详情，参数 vehicleId
  // 静态数据模拟
  setTimeout(() => {
    state.plateNumber = '京A12345'
    state.vehicleType = '小型汽车'
    state.isDefault = true
    state.loading = false
  }, 300)
})

const handleSubmit = () => {
  const plate = state.plateNumber.toUpperCase()
  if (!platePattern.test(plate)) {
    showToast('请输入正确的车牌号')
    return
  }
  state.plateNumber = plate
  // TODO: 调用编辑车辆接口（commonSave）
  showToast('保存成功')
  router.back()
}

const handleSetDefault = () => {
  state.isDefault = true
}
</script>

<style scoped lang="less">
.vehicle-edit-page {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: auto;

  .loading-center {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
  }

  .form-content {
    margin-top: 16px;
  }

  .submit-btn {
    padding: 24px 16px;

    .van-button {
      border-radius: 8px;
    }
  }
}
</style>
