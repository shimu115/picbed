<script setup>
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useTokenStore } from '@/stores/token'

const emit = defineEmits(['upload'])
const { t } = useI18n()
const tokenStore = useTokenStore()
const dragging = ref(false)

const acceptTypes = '.png,.jpg,.jpeg,.gif,.webp,.svg,.bmp,.tiff,.tif'

function onDragOver(e) {
  e.preventDefault()
  dragging.value = true
}

function onDragLeave() {
  dragging.value = false
}

function onDrop(e) {
  e.preventDefault()
  dragging.value = false
  const files = Array.from(e.dataTransfer.files).filter(f => f.type.startsWith('image/'))
  if (files.length) emit('upload', files)
}

function onFileSelect(e) {
  const files = Array.from(e.target.files)
  if (files.length) emit('upload', files)
}
</script>

<template>
  <div
    :class="['upload-zone', { dragging, disabled: !tokenStore.hasToken }]"
    @dragover="onDragOver"
    @dragleave="onDragLeave"
    @drop="onDrop"
    @click="tokenStore.hasToken && $refs.fileInput.click()"
  >
    <input
      ref="fileInput"
      type="file"
      :accept="acceptTypes"
      multiple
      hidden
      @change="onFileSelect"
    />
    <el-icon class="upload-icon"><svg viewBox="0 0 24 24" width="48" height="48" fill="#c0c4cc"><path d="M9 16h6v-6h4l-7-7-7 7h4zm-4 2h14v2H5z"/></svg></el-icon>
    <p v-if="tokenStore.hasToken" class="upload-hint">{{ t('upload.dragHint') }}</p>
    <p v-else class="upload-hint disabled-hint">{{ t('upload.noTokenHint') }}</p>
    <p class="upload-types">{{ t('upload.typeHint') }}</p>
  </div>
</template>

<style scoped>
.upload-zone {
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  padding: 50px 20px;
  text-align: center;
  cursor: pointer;
  transition: border-color 0.3s, background 0.3s;
  background: #fafafa;
}
.upload-zone:hover {
  border-color: #409eff;
  background: #ecf5ff;
}
.upload-zone.dragging {
  border-color: #409eff;
  background: #ecf5ff;
}
.upload-zone.disabled {
  cursor: not-allowed;
  opacity: 0.6;
}
.upload-icon {
  margin-bottom: 12px;
}
.upload-hint {
  font-size: 15px;
  color: #606266;
  margin-bottom: 6px;
}
.disabled-hint {
  color: #e6a23c;
}
.upload-types {
  font-size: 12px;
  color: #c0c4cc;
}
</style>
