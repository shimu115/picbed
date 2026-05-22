<script setup>
import { useUploadStore } from '@/stores/upload'
import UploadQueueItem from './UploadQueueItem.vue'

const uploadStore = useUploadStore()
</script>

<template>
  <div v-if="uploadStore.queue.length > 0" class="upload-queue">
    <div class="queue-header">
      <h4>Upload Queue ({{ uploadStore.queue.length }})</h4>
      <el-button size="small" text @click="uploadStore.clearCompleted">Clear completed</el-button>
    </div>
    <UploadQueueItem v-for="item in uploadStore.queue" :key="item.id" :item="item" />
  </div>
</template>

<style scoped>
.upload-queue {
  margin-top: 20px;
}
.queue-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.queue-header h4 {
  font-size: 16px;
  font-weight: 600;
}
</style>
