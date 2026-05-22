<script setup>
import { useTokenStore } from '@/stores/token'
import { useOssUpload } from '@/composables/useOssUpload'
import UploadZone from '@/components/upload/UploadZone.vue'
import UploadQueue from '@/components/upload/UploadQueue.vue'
import { ElMessage } from 'element-plus'

const tokenStore = useTokenStore()
const { uploadFiles } = useOssUpload()

async function handleUpload(files) {
  if (!tokenStore.hasToken) {
    ElMessage.warning('Set your auth token first')
    return
  }
  const results = await uploadFiles(files)
  const success = results.filter(r => r.success).length
  const failed = results.filter(r => !r.success).length
  if (success > 0) ElMessage.success(`${success} file(s) uploaded`)
  if (failed > 0) ElMessage.error(`${failed} file(s) failed`)
}
</script>

<template>
  <div class="upload-page">
    <h2>Upload Images</h2>
    <UploadZone @upload="handleUpload" />
    <UploadQueue />
  </div>
</template>
