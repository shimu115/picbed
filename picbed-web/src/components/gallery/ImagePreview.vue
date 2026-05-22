<script setup>
import { watch } from 'vue'

const props = defineProps({
  image: { type: Object, default: null },
  visible: { type: Boolean, default: false }
})

defineEmits(['close'])

watch(() => props.visible, (val) => {
  if (!val) return
})
</script>

<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="$emit('close')"
    :title="image?.filename || 'Preview'"
    width="80%"
    top="5vh"
    destroy-on-close
  >
    <div v-if="image" class="preview-content">
      <img :src="image.ossUrl" :alt="image.filename" class="preview-image" />
      <div class="preview-info">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="Filename">{{ image.filename }}</el-descriptions-item>
          <el-descriptions-item label="Type">{{ image.contentType }}</el-descriptions-item>
          <el-descriptions-item label="Size">{{ (image.fileSize / 1024).toFixed(1) }} KB</el-descriptions-item>
          <el-descriptions-item label="Dimensions">
            {{ image.width && image.height ? `${image.width}x${image.height}` : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="URL" :span="2">
            <el-input :model-value="image.ossUrl" readonly size="small">
              <template #append>
                <el-button @click="navigator.clipboard.writeText(image.ossUrl)">Copy</el-button>
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
.preview-info {
  text-align: left;
}
</style>
