<script setup>
import { useI18n } from 'vue-i18n'
import { useTokenStore } from '@/stores/token'
import { useOssUpload } from '@/composables/useOssUpload'
import UploadZone from '@/components/upload/UploadZone.vue'
import UploadQueue from '@/components/upload/UploadQueue.vue'
import { ElMessage } from 'element-plus'

const { t } = useI18n()
const tokenStore = useTokenStore()
const { uploadFiles } = useOssUpload()

async function handleUpload(files) {
  if (!tokenStore.hasToken) {
    ElMessage.warning(t('upload.noTokenHint'))
    return
  }
  const results = await uploadFiles(files)
  const success = results.filter(r => r.success).length
  const failed = results.filter(r => !r.success).length
  if (success > 0) ElMessage.success(t('upload.uploadSuccess', { count: success }))
  if (failed > 0) ElMessage.error(t('upload.uploadFailed', { count: failed }))
}
</script>

<template>
  <div class="upload-page">
    <h2>{{ t('upload.title') }}</h2>
    <UploadZone @upload="handleUpload" />
    <UploadQueue />
  </div>
</template>
