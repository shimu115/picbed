<script setup>
import { ref, onMounted } from 'vue'
import { getPublicImages, deleteImage, batchDeleteImages } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

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
    await ElMessageBox.confirm(`Delete "${img.filename}"?`, 'Confirm', { type: 'warning' })
    await deleteImage(img.id)
    ElMessage.success('Deleted')
    await loadImages()
  } catch { /* cancelled */ }
}

async function handleBatchDelete() {
  try {
    await ElMessageBox.confirm(`Delete ${selection.value.length} selected images?`, 'Confirm Batch Delete', { type: 'warning' })
    const ids = selection.value.map(i => i.id)
    await batchDeleteImages(ids)
    ElMessage.success(`Deleted ${ids.length} images`)
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
  ElMessage.success('URL copied')
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
        Delete Selected ({{ selection.length }})
      </el-button>
    </div>

    <el-table
      :data="images"
      v-loading="loading"
      @selection-change="s => selection = s"
      style="margin-top: 10px"
    >
      <el-table-column type="selection" width="45" />
      <el-table-column label="Preview" width="80">
        <template #default="{ row }">
          <img :src="row.ossUrl" class="thumb" />
        </template>
      </el-table-column>
      <el-table-column prop="filename" label="Filename" min-width="180" show-overflow-tooltip />
      <el-table-column prop="contentType" label="Type" width="110" />
      <el-table-column label="Size" width="90">
        <template #default="{ row }">{{ fileSizeLabel(row.fileSize) }}</template>
      </el-table-column>
      <el-table-column label="Created" width="160">
        <template #default="{ row }">{{ row.createdAt?.replace('T', ' ')?.substring(0, 19) }}</template>
      </el-table-column>
      <el-table-column label="Actions" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text @click="copyUrl(row.ossUrl)">Copy URL</el-button>
          <el-button size="small" text type="danger" @click="handleDelete(row)">Delete</el-button>
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
