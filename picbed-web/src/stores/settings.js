import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getSettings } from '@/api'

export const useSettingsStore = defineStore('settings', () => {
  const uploadSizeLimitEnabled = ref(false)
  const uploadSizeLimitMb = ref(50)
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
      loaded.value = true
    } catch {
      // keep defaults
    }
  }

  function applySettings(data) {
    uploadSizeLimitEnabled.value = data.uploadSizeLimitEnabled
    uploadSizeLimitMb.value = data.uploadSizeLimitMb
  }

  return {
    uploadSizeLimitEnabled,
    uploadSizeLimitMb,
    maxUploadBytes,
    loaded,
    fetchSettings,
    applySettings
  }
})
