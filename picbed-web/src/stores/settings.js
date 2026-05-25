import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getSettings } from '@/api'

export const useSettingsStore = defineStore('settings', () => {
  const uploadSizeLimitEnabled = ref(false)
  const uploadSizeLimitMb = ref(50)
  const tokenRefreshCron = ref('0 0 2 */3 * ?')
  const loaded = ref(false)

  const maxUploadBytes = computed(() => {
    if (!uploadSizeLimitEnabled.value) return null
    return uploadSizeLimitMb.value * 1024 * 1024
  })

  async function fetchSettings() {
    try {
      const res = await getSettings()
      const data = res.data.data
      uploadSizeLimitEnabled.value = data.uploadSizeLimitEnabled
      uploadSizeLimitMb.value = data.uploadSizeLimitMb
      tokenRefreshCron.value = data.tokenRefreshCron || '0 0 2 */3 * ?'
      loaded.value = true
    } catch {
      // keep defaults
    }
  }

  function applySettings(data) {
    uploadSizeLimitEnabled.value = data.uploadSizeLimitEnabled
    uploadSizeLimitMb.value = data.uploadSizeLimitMb
    if (data.tokenRefreshCron) tokenRefreshCron.value = data.tokenRefreshCron
  }

  return {
    uploadSizeLimitEnabled,
    uploadSizeLimitMb,
    tokenRefreshCron,
    maxUploadBytes,
    loaded,
    fetchSettings,
    applySettings
  }
})
