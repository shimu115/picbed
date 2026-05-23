<script setup>
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'

const props = defineProps({
  image: { type: Object, required: true }
})

defineEmits(['preview', 'delete'])
const { t } = useI18n()

const imgLoaded = ref(false)

function fileSizeLabel(bytes) {
  if (!bytes) return ''
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1048576).toFixed(1) + ' MB'
}

function copyUrl() {
  navigator.clipboard.writeText(props.image.ossUrl).catch(() => {})
  ElMessage.success(t('common.copySuccess'))
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString(locale(), {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

function locale() {
  return navigator.language
}
</script>

<template>
  <div class="image-card">
    <div class="card-image" @click="$emit('preview', image)">
      <div v-if="!imgLoaded" class="image-skeleton"></div>
      <img
        :src="image.ossUrl"
        :alt="image.filename"
        @load="imgLoaded = true"
        @error="imgLoaded = true"
        :class="{ loaded: imgLoaded }"
      />
    </div>
    <div class="card-info">
      <span class="filename" :title="image.filename">{{ image.filename }}</span>
      <span class="file-size">{{ fileSizeLabel(image.fileSize) }}</span>
    </div>
    <div class="card-actions">
      <el-button size="small" text @click="copyUrl">{{ t('gallery.copyUrl') }}</el-button>
      <el-button size="small" text type="danger" @click="$emit('delete', image)">{{ t('gallery.delete') }}</el-button>
    </div>
    <div class="card-date">{{ formatDate(image.createdAt) }}</div>
  </div>
</template>

<style scoped>
.image-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  transition: box-shadow 0.2s;
  display: flex;
  flex-direction: column;
}
.image-card:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.12);
}
.card-image {
  position: relative;
  width: 100%;
  padding-top: 75%;
  overflow: hidden;
  cursor: pointer;
  background: #f0f2f5;
}
.card-image img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0;
  transition: opacity 0.3s;
}
.card-image img.loaded {
  opacity: 1;
}
.image-skeleton {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: linear-gradient(90deg, #f0f2f5 25%, #e4e7ed 50%, #f0f2f5 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}
@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}
.card-info {
  padding: 10px 12px 4px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}
.filename {
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}
.file-size {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
}
.card-actions {
  padding: 0 8px;
  display: flex;
  justify-content: space-between;
}
.card-date {
  padding: 6px 12px 10px;
  font-size: 11px;
  color: #c0c4cc;
}
</style>
