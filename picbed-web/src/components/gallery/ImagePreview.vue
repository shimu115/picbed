<script setup>
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'

const props = defineProps({
  image: { type: Object, default: null },
  visible: { type: Boolean, default: false }
})

defineEmits(['close'])
const { t } = useI18n()

function copyUrl(text) {
  if (navigator.clipboard && window.isSecureContext) {
    navigator.clipboard.writeText(text)
  } else {
    const ta = document.createElement('textarea')
    ta.value = text
    ta.style.position = 'fixed'
    ta.style.left = '-9999px'
    document.body.appendChild(ta)
    ta.select()
    document.execCommand('copy')
    document.body.removeChild(ta)
  }
  ElMessage.success(t('common.copied'))
}
</script>

<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="$emit('close')"
    :title="image?.filename || t('gallery.preview')"
    width="80%"
    top="5vh"
    destroy-on-close
  >
    <div v-if="image" class="preview-content">
      <el-image
        :src="image.ossUrl"
        :alt="image.filename"
        :preview-src-list="[image.ossUrl]"
        :hide-on-click-modal="true"
        fit="contain"
        class="preview-image"
      />
      <div class="preview-info">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item :label="t('gallery.filename')">{{ image.filename }}</el-descriptions-item>
          <el-descriptions-item :label="t('gallery.type')">{{ image.contentType }}</el-descriptions-item>
          <el-descriptions-item :label="t('gallery.size')">{{ (image.fileSize / 1024).toFixed(1) }} KB</el-descriptions-item>
          <el-descriptions-item :label="t('gallery.dimensions')">
            {{ image.width && image.height ? `${image.width}x${image.height}` : '-' }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('gallery.url')" :span="2">
            <el-input :model-value="image.ossUrl" readonly size="small">
              <template #append>
                <el-button @click="copyUrl(image.ossUrl)">{{ t('common.copy') }}</el-button>
              </template>
            </el-input>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </div>
  </el-dialog>
</template>

<style scoped>
.preview-content {
  text-align: center;
}
.preview-image {
  max-width: 100%;
  max-height: 60vh;
  border-radius: 4px;
  margin-bottom: 16px;
}
.preview-image :deep(img) {
  max-height: 60vh;
  object-fit: contain;
}
.preview-info {
  text-align: left;
}
</style>
