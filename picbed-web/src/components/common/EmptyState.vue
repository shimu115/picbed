<script setup>
import { useI18n } from 'vue-i18n'

defineProps({
  message: { type: String, default: '' },
  showUpload: { type: Boolean, default: false }
})
defineEmits(['upload'])
const { t } = useI18n()
</script>

<template>
  <div class="empty-state">
    <el-icon class="empty-icon"><svg viewBox="0 0 24 24" width="64" height="64" fill="#c0c4cc"><path d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z"/></svg></el-icon>
    <p class="empty-text">{{ message || t('gallery.empty') }}</p>
    <el-button v-if="showUpload" type="primary" @click="$emit('upload')">
      {{ t('upload.title') }}
    </el-button>
  </div>
</template>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: #909399;
}
.empty-icon {
  margin-bottom: 16px;
}
.empty-text {
  font-size: 16px;
  margin-bottom: 20px;
}

@media (max-width: 767px) {
  .empty-state {
    padding: 48px 16px;
  }
  .empty-text {
    font-size: 14px;
  }
}
</style>
