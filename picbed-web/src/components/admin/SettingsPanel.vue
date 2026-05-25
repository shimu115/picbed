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
const refreshCron = ref('0 0 2 */3 * ?')
const saving = ref(false)

const cronPresets = [
  { label: 'Every day 2AM', cron: '0 0 2 * * ?' },
  { label: 'Every 3 days 2AM', cron: '0 0 2 */3 * ?' },
  { label: 'Every 7 days 2AM', cron: '0 0 2 */7 * ?' },
  { label: 'Every 30 days 2AM', cron: '0 0 2 */30 * ?' }
]

onMounted(() => {
  enabled.value = settingsStore.uploadSizeLimitEnabled
  limitMb.value = settingsStore.uploadSizeLimitMb
  refreshCron.value = settingsStore.tokenRefreshCron
})

function applyPreset(cron) {
  refreshCron.value = cron
}

async function save() {
  saving.value = true
  try {
    const data = {
      uploadSizeLimitEnabled: enabled.value,
      uploadSizeLimitMb: limitMb.value,
      tokenRefreshCron: refreshCron.value
    }
    await updateSettings(data)
    settingsStore.applySettings(data)
    ElMessage.success(t('manage.settingsSaved'))
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || t('error.serverError'))
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="settings-panel">
    <div class="setting-section">
      <h4 class="section-title">{{ t('manage.uploadSettings') }}</h4>
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
    </div>

    <div class="setting-section">
      <h4 class="section-title">{{ t('manage.tokenRefresh') }}</h4>
      <div class="setting-row">
        <span class="setting-label">{{ t('manage.tokenRefreshCron') }}</span>
        <el-input v-model="refreshCron" size="small" style="width: 200px" placeholder="0 0 2 */3 * ?" />
      </div>
      <div class="setting-row">
        <span class="setting-label"></span>
        <el-button
          v-for="p in cronPresets"
          :key="p.cron"
          size="small"
          :type="refreshCron === p.cron ? 'primary' : 'default'"
          @click="applyPreset(p.cron)"
        >
          {{ p.label }}
        </el-button>
      </div>
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
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}
.setting-section {
  margin-bottom: 20px;
}
.setting-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  flex-wrap: wrap;
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
