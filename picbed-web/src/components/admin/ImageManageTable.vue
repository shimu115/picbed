<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getPublicImages, deleteImage, batchDeleteImages } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const { t } = useI18n()
const images = ref([])
const loading = ref(false)
const selection = ref([])
const total = ref(0)
const page = ref(0)
const pageSize = ref(20)

async function loadImages() {
  loading.value = true
  try {
    const res = await getPublicImages(page.value, pageSize.value)
    images.value = res.data.data.content
    total.value = res.data.data.totalElements
  } finally {
    loading.value = false
  }
}

async function handleDelete(img) {
  try {
    await ElMessageBox.confirm(
      t('manage.deleteConfirm', { name: img.filename }),
      t('common.confirm'),
      { type: 'warning' }
    )
    await deleteImage(img.id)
    ElMessage.success(t('manage.deleteSuccess'))
    await loadImages()
  } catch { /* cancelled */ }
}

async function handleBatchDelete() {
  try {
    await ElMessageBox.confirm(
      t('manage.batchDeleteConfirm', { count: selection.value.length }),
      t('common.confirm'),
      { type: 'warning' }
    )
    const ids = selection.value.map(i => i.id)
    await batchDeleteImages(ids)
    ElMessage.success(t('manage.batchDeleteSuccess', { count: ids.length }))
    selection.value = []
    await loadImages()
  } catch { /* cancelled */ }
}

function handlePageChange(p) {
  page.value = p - 1
  loadImages()
}

function copyUrl(url) {
  navigator.clipboard.writeText(url)
  ElMessage.success(t('manage.copyUrlSuccess'))
}

function fileSizeLabel(bytes) {
  if (!bytes) return ''
  if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1048576).toFixed(1) + ' MB'
}

onMounted(loadImages)
</script>

<template>
  <div class="image-table">
    <div class="table-actions">
      <el-button
        type="danger"
        size="small"
        :disabled="selection.length === 0"
        @click="handleBatchDelete"
      >
        {{ t('manage.deleteSelected', { count: selection.length }) }}
      </el-button>
    </div>

    <el-table
      :data="images"
      v-loading="loading"
      @selection-change="s => selection = s"
      style="margin-top: 10px"
    >
      <el-table-column type="selection" width="45" />
      <el-table-column :label="t('manage.preview')" width="80">
        <template #default="{ row }">
          <img :src="row.ossUrl" class="thumb" />
        </template>
      </el-table-column>
      <el-table-column prop="filename" :label="t('manage.filename')" min-width="180" show-overflow-tooltip />
      <el-table-column prop="contentType" :label="t('manage.type')" width="110" />
      <el-table-column :label="t('manage.size')" width="90">
        <template #default="{ row }">{{ fileSizeLabel(row.fileSize) }}</template>
      </el-table-column>
      <el-table-column :label="t('manage.created')" width="160">
        <template #default="{ row }">{{ row.createdAt?.replace('T', ' ')?.substring(0, 19) }}</template>
      </el-table-column>
      <el-table-column :label="t('manage.actions')" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text @click="copyUrl(row.ossUrl)">{{ t('manage.copyUrl') }}</el-button>
          <el-button size="small" text type="danger" @click="handleDelete(row)">{{ t('manage.delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="total > pageSize"
      :total="total"
      :page-size="pageSize"
      :current-page="page + 1"
      layout="prev, pager, next, total"
      @current-change="handlePageChange"
      style="margin-top: 16px; justify-content: center"
    />
  </div>
</template>

<style scoped>
.thumb {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
}
.table-actions {
  display: flex;
  justify-content: flex-end;
}
</style>
