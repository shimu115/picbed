<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { getPublicImage, getAdminImage } from '@/api'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const image = ref(null)
const loading = ref(true)

function fileSizeLabel(bytes) {
  if (!bytes) return ''
  if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1048576).toFixed(1) + ' MB'
}

function formatDate(dateStr) {
  return dateStr?.replace('T', ' ')?.substring(0, 19)
}

function copyUrl(url) {
  if (navigator.clipboard && window.isSecureContext) {
    navigator.clipboard.writeText(url).then(() => {
      ElMessage.success(t('manage.copyUrlSuccess'))
    })
  } else {
    const ta = document.createElement('textarea')
    ta.value = url
    ta.style.position = 'fixed'
    ta.style.left = '-9999px'
    document.body.appendChild(ta)
    ta.select()
    document.execCommand('copy')
    document.body.removeChild(ta)
    ElMessage.success(t('manage.copyUrlSuccess'))
  }
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}

onMounted(async () => {
  const id = route.params.id
  const token = localStorage.getItem('auth_token')
  try {
    if (token) {
      const res = await getAdminImage(id)
      console.log("admin")
      image.value = res.data.data
    } else {
      const res = await getPublicImage(id)
      console.log("public")
      image.value = res.data.data
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || t('error.serverError'))
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="image-view-page">
    <div class="view-header">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        {{ t('common.back') }}
      </el-button>
    </div>

    <div v-if="loading" class="view-loading">
      <el-skeleton :rows="6" animated />
    </div>

    <div v-else-if="image" class="view-content">
      <el-image
        :src="image.ossUrl"
        :alt="image.filename"
        :preview-src-list="[image.ossUrl]"
        :hide-on-click-modal="true"
        fit="contain"
        class="view-image"
      />

      <div class="view-meta">
        <el-popover
          trigger="click"
          placement="bottom"
          :width="300"
          :show-after="0"
        >
          <template #reference>
            <span class="filename-text">{{ image.filename }}</span>
          </template>
          <div class="filename-full">{{ image.filename }}</div>
        </el-popover>

        <div class="meta-grid">
          <div class="meta-item" v-if="image.uploadedBy">
            <span class="meta-label">{{ t('token.name') }}</span>
            <span class="meta-value">{{ image.uploadedBy }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">{{ t('gallery.type') }}</span>
            <span class="meta-value">{{ image.contentType }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">{{ t('gallery.size') }}</span>
            <span class="meta-value">{{ fileSizeLabel(image.fileSize) }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">{{ t('gallery.dimensions') }}</span>
            <span class="meta-value">{{ image.width && image.height ? `${image.width}x${image.height}` : '-' }}</span>
          </div>
          <div class="meta-item" v-if="image.isPublished !== undefined">
            <span class="meta-label">{{ t('manage.published') }}</span>
            <el-tag :type="image.isPublished ? 'success' : 'info'" size="small">
              {{ image.isPublished ? t('common.yes') : t('common.no') }}
            </el-tag>
          </div>
          <div class="meta-item">
            <span class="meta-label">{{ t('manage.created') }}</span>
            <span class="meta-value">{{ formatDate(image.createdAt) }}</span>
          </div>
        </div>

        <div class="url-row">
          <el-input
            :model-value="image.ossUrl"
            readonly
            size="small"
          >
            <template #append>
              <el-button @click="copyUrl(image.ossUrl)">{{ t('common.copy') }}</el-button>
            </template>
          </el-input>
        </div>
      </div>
    </div>

    <div v-else class="view-error">
      <el-empty :description="t('error.notFound')" />
    </div>
  </div>
</template>

<style scoped>
.image-view-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.view-header {
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  position: sticky;
  top: 0;
  z-index: 10;
}

.view-loading {
  padding: 20px;
}

.view-content {
  background: #fff;
  margin: 0;
}

.view-image {
  width: 100%;
  display: block;
}

.view-image :deep(img) {
  width: 100%;
  max-height: 60vh;
  object-fit: contain;
  background: #f0f0f0;
}

.view-meta {
  padding: 16px;
}

.filename-text {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 16px;
  cursor: pointer;
}

.filename-full {
  word-break: break-all;
  white-space: normal;
  line-height: 1.6;
  font-size: 14px;
}

.meta-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 16px;
}

.meta-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.meta-label {
  font-size: 13px;
  color: #909399;
  flex-shrink: 0;
}

.meta-value {
  font-size: 13px;
  color: #303133;
  text-align: right;
  max-width: 60%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.url-row {
  margin-top: 4px;
}

.view-error {
  padding: 40px 20px;
  text-align: center;
}
</style>
