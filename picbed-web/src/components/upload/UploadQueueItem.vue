<script setup>
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'

defineProps({
  item: { type: Object, required: true }
})
const { t } = useI18n()

function copyUrl(text) {
  if (navigator.clipboard && window.isSecureContext) {
    navigator.clipboard.writeText(text).then(() => {
      ElMessage.success(t('common.copied'))
    })
  } else {
    const ta = document.createElement('textarea')
    ta.value = text
    ta.style.position = 'fixed'
    ta.style.left = '-9999px'
    document.body.appendChild(ta)
    ta.select()
    document.execCommand('copy')
    document.body.removeChild(ta)
    ElMessage.success(t('common.copied'))
  }
}
</script>

<template>
  <div :class="['queue-item', item.status]">
    <div class="item-info">
      <span class="item-name">{{ item.file?.name || t('common.unknown') }}</span>
      <span class="item-status">
        <template v-if="item.status === 'pending'">{{ t('upload.waiting') }}</template>
        <template v-else-if="item.status === 'uploading'">{{ t('upload.uploading', { progress: item.progress }) }}</template>
        <template v-else-if="item.status === 'success'">
          <el-icon color="#67c23a"><svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/></svg></el-icon>
          {{ t('upload.done') }}
        </template>
        <template v-else-if="item.status === 'error'">
          <el-tooltip :content="item.errorMsg">
            <el-icon color="#f56c6c"><svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/></svg></el-icon>
          </el-tooltip>
          {{ t('upload.error') }}
        </template>
      </span>
    </div>
    <el-progress
      v-if="item.status === 'uploading'"
      :percentage="item.progress"
      :stroke-width="6"
      :show-text="false"
    />
    <div v-if="item.status === 'success' && item.accessUrl" class="item-url">
      <el-input :model-value="item.accessUrl" readonly size="small">
        <template #append>
          <el-button size="small" @click="copyUrl(item.accessUrl)">{{ t('common.copy') }}</el-button>
        </template>
      </el-input>
    </div>
  </div>
</template>

<style scoped>
.queue-item {
  background: #fff;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 10px;
  border: 1px solid #e4e7ed;
}
.queue-item.success {
  border-left: 4px solid #67c23a;
}
.queue-item.error {
  border-left: 4px solid #f56c6c;
}
.queue-item.uploading {
  border-left: 4px solid #409eff;
}
.item-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.item-name {
  font-size: 14px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  margin-right: 12px;
}
.item-status {
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}
.item-url {
  margin-top: 8px;
}
</style>
