<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSettingsStore } from '@/stores/settings'
import { updateSettings } from '@/api'
import { ElMessage } from 'element-plus'
import { WarningFilled } from '@element-plus/icons-vue'

const { t } = useI18n()
const settingsStore = useSettingsStore()

const enabled = ref(false)
const limitMb = ref(50)
const refreshCron = ref('0 0 0 */3 * ?')
const autoRefreshEnabled = ref(false)
const saving = ref(false)

const cronPresets = [
  { label: '每天 0:00', cron: '0 0 0 * * ?' },
  { label: '每三天 0:00', cron: '0 0 0 */3 * ?' },
  { label: '每七天 0:00', cron: '0 0 0 */7 * ?' },
  { label: '每三十天 0:00', cron: '0 0 0 */30 * ?' }
]

onMounted(() => {
  enabled.value = settingsStore.uploadSizeLimitEnabled
  limitMb.value = settingsStore.uploadSizeLimitMb
  refreshCron.value = settingsStore.tokenRefreshCron
  autoRefreshEnabled.value = settingsStore.tokenAutoRefreshEnabled
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
      tokenRefreshCron: refreshCron.value,
      tokenAutoRefreshEnabled: autoRefreshEnabled.value
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
      <h4 class="section-title">
        {{ t('manage.tokenRefresh') }}
        <el-popover placement="top" :width="280" trigger="hover" :content="t('manage.tokenRefreshBetaWarn')">
          <template #reference>
            <el-icon class="beta-warn-icon"><WarningFilled /></el-icon>
          </template>
        </el-popover>
      </h4>
      <div class="setting-row">
        <span class="setting-label">{{ t('manage.tokenAutoRefreshEnable') }}</span>
        <el-switch v-model="autoRefreshEnabled" />
      </div>
      <div class="setting-row">
        <span class="setting-label">{{ t('manage.tokenRefreshCron') }}</span>
        <el-input v-model="refreshCron" size="small" style="width: 200px" placeholder="0 0 0 */3 * ?" />
      </div>
      <div class="setting-row">
        <span class="setting-label"></span>
        <div class="cron-presets">
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
  display: flex;
  align-items: center;
  gap: 6px;
}
.beta-warn-icon {
  color: #e6a23c;
  font-size: 16px;
  cursor: pointer;
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
.cron-presets {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
.cron-presets .el-button {
  margin: 0;
}

@media (max-width: 767px) {
  .cron-presets {
    grid-template-columns: 1fr 1fr;
    width: 100%;
  }
}
</style>
