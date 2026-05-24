<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSettingsStore } from '@/stores/settings'
import { updateSettings } from '@/api'
import { ElMessage } from 'element-plus'

const { t } = useI18n()
const settingsStore = useSettingsStore()

const enabled = ref(false)
const limitMb = ref(50)
const saving = ref(false)

onMounted(() => {
  enabled.value = settingsStore.uploadSizeLimitEnabled
  limitMb.value = settingsStore.uploadSizeLimitMb
})

async function save() {
  saving.value = true
  try {
    const data = {
      uploadSizeLimitEnabled: enabled.value,
      uploadSizeLimitMb: limitMb.value
    }
    await updateSettings(data)
    settingsStore.applySettings(data)
    ElMessage.success(t('common.save') + ' ' + t('manage.settingsSaved'))
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || t('error.serverError'))
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="settings-panel">
    <div class="setting-row">
      <span class="setting-label">{{ t('manage.uploadSizeLimit') }}</span>
      <el-switch v-model="enabled" />
    </div>
    <div v-if="enabled" class="setting-row">
      <span class="setting-label">{{ t('manage.uploadSizeLimitMb') }}</span>
      <el-input-number
        v-model="limitMb"
        :min="1"
        :max="500"
        :step="1"
        size="small"
        style="width: 140px"
      />
      <span class="setting-unit">MB</span>
    </div>
    <div class="setting-row">
      <el-button type="primary" size="small" :loading="saving" @click="save">
        {{ t('common.save') }}
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.settings-panel {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #e4e7ed;
}
.setting-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.setting-label {
  font-size: 14px;
  color: #303133;
  min-width: 120px;
}
.setting-unit {
  font-size: 13px;
  color: #909399;
}
</style>
