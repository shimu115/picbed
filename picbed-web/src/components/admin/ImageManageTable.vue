<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useTokenStore } from '@/stores/token'
import { getAdminImages, deleteImage, batchDeleteImages, batchPublishImages } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DocumentCopy } from '@element-plus/icons-vue'
import ImagePreview from '@/components/gallery/ImagePreview.vue'

const router = useRouter()
const { t } = useI18n()
const tokenStore = useTokenStore()
const images = ref([])
const loading = ref(false)
const selection = ref([])
const total = ref(0)
const page = ref(0)
const pageSize = ref(20)
const selectMode = ref(false)
const publishedFilter = ref(undefined)
const searchText = ref('')
const searchType = ref('filename')
const previewImage = ref(null)
const showPreview = ref(false)

const selectedIds = computed(() => new Set(selection.value.map(i => i.id)))
const allSelected = computed(() =>
  images.value.length > 0 && selection.value.length === images.value.length
)

function canTogglePublish(img) {
  if (!tokenStore.isAdmin) return true
  if (img.isPublished) return true
  return img.tokenId === tokenStore.tokenId
}

async function loadImages() {
  loading.value = true
  try {
    const res = await getAdminImages(page.value, pageSize.value, publishedFilter.value, searchText.value.trim(), searchType.value)
    images.value = res.data.data.content
    total.value = res.data.data.totalElements
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 0
  selection.value = []
  selectMode.value = false
  loadImages()
}

function onFilterChange() {
  page.value = 0
  selection.value = []
  selectMode.value = false
  loadImages()
}

function enterSelectMode(img) {
  selectMode.value = true
  toggleSelect(img)
}

function toggleSelect(img) {
  const idx = selection.value.findIndex(i => i.id === img.id)
  if (idx >= 0) {
    selection.value.splice(idx, 1)
    if (selection.value.length === 0) {
      selectMode.value = false
    }
  } else {
    selection.value.push(img)
  }
}

function toggleSelectAll() {
  if (allSelected.value) {
    selection.value = []
    selectMode.value = false
  } else {
    selection.value = images.value.slice()
  }
}

function exitSelectMode() {
  selectMode.value = false
  selection.value = []
}

async function handleDelete(img) {
  try {
    await ElMessageBox.confirm(
      t('manage.deleteConfirm', { name: img.filename }),
      t('common.confirm'),
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await deleteImage(img.id)
    ElMessage.success(t('manage.deleteSuccess'))
    selection.value = selection.value.filter(i => i.id !== img.id)
    await loadImages()
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || t('error.serverError'))
    if (e.response?.status === 401) {
      tokenStore.clearToken()
    }
  }
}

async function handleBatchDelete() {
  try {
    await ElMessageBox.confirm(
      t('manage.batchDeleteConfirm', { count: selection.value.length }),
      t('common.confirm'),
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    const ids = selection.value.map(i => i.id)
    await batchDeleteImages(ids)
    ElMessage.success(t('manage.batchDeleteSuccess', { count: ids.length }))
    selection.value = []
    selectMode.value = false
    await loadImages()
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || t('error.serverError'))
    if (e.response?.status === 401) {
      tokenStore.clearToken()
    }
  }
}

async function handleBatchPublish(published) {
  const ids = selection.value.map(i => i.id)
  try {
    await batchPublishImages(ids, published)
    ElMessage.success(t('manage.batchPublishSuccess', { count: ids.length }))
    selection.value = []
    selectMode.value = false
    await loadImages()
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || t('error.serverError'))
    if (e.response?.status === 401) {
      tokenStore.clearToken()
    }
  }
}

async function handleTogglePublish(img) {
  if (!canTogglePublish(img)) return
  const newVal = !img.isPublished
  try {
    await batchPublishImages([img.id], newVal)
    img.isPublished = newVal
    ElMessage.success(t('manage.batchPublishSuccess', { count: 1 }))
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || t('error.serverError'))
    if (e.response?.status === 401) {
      tokenStore.clearToken()
    }
  }
}

function handlePageChange(p) {
  page.value = p - 1
  loadImages()
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

function fileSizeLabel(bytes) {
  if (!bytes) return ''
  if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1048576).toFixed(1) + ' MB'
}

function openPreview(img) {
  if (window.innerWidth < 768) {
    router.push('/image/' + img.id)
  } else {
    previewImage.value = img
    showPreview.value = true
  }
}

function closePreview() {
  showPreview.value = false
}

function formatDate(dateStr) {
  return dateStr?.replace('T', ' ')?.substring(0, 19)
}

onMounted(loadImages)
</script>

<template>
  <div class="image-table">
    <!-- Toolbar: search + filter + batch actions (desktop) -->
    <div class="toolbar">
      <div class="toolbar-search">
        <el-input
          v-model="searchText"
          :placeholder="t('manage.searchPlaceholder')"
          class="search-input"
          clearable
          size="small"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg></el-icon>
          </template>
        </el-input>
        <el-radio-group v-model="searchType" @change="handleSearch" size="small">
          <el-radio-button value="filename">{{ t('manage.searchByFilename') }}</el-radio-button>
          <el-radio-button value="username">{{ t('manage.searchByUsername') }}</el-radio-button>
        </el-radio-group>
      </div>
      <el-select
        v-model="publishedFilter"
        :placeholder="t('manage.filterAll')"
        size="small"
        clearable
        style="width: 130px"
        @change="onFilterChange"
      >
        <el-option :value="undefined" :label="t('manage.filterAll')" />
        <el-option :value="true" :label="t('manage.filterPublished')" />
        <el-option :value="false" :label="t('manage.filterUnpublished')" />
      </el-select>
      <div class="toolbar-actions desktop-only">
        <el-button
          type="danger"
          size="small"
          :disabled="selection.length === 0"
          @click="handleBatchDelete"
        >
          {{ t('manage.deleteSelected', { count: selection.length }) }}
        </el-button>
      </div>
    </div>

    <!-- selection mode bar (mobile) -->
    <div v-if="selectMode" class="select-bar mobile-only">
      <el-button size="small" text @click="exitSelectMode">{{ t('common.cancel') }}</el-button>
      <span class="select-count">{{ selection.length }} {{ t('common.confirm') }}</span>
      <el-button size="small" text @click="toggleSelectAll">
        {{ allSelected ? t('manage.deselectAll') : t('manage.selectAll') }}
      </el-button>
    </div>

    <!-- Desktop: table -->
    <div class="desktop-table">
      <el-table
        :data="images"
        v-loading="loading"
        @selection-change="s => selection = s"
        style="margin-top: 10px"
      >
        <el-table-column type="selection" width="45" />
        <el-table-column :label="t('manage.preview')" width="80">
          <template #default="{ row }">
            <img :src="row.ossUrl" class="thumb" @click="openPreview(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="filename" :label="t('manage.filename')" min-width="160" show-overflow-tooltip />
        <el-table-column prop="uploadedBy" :label="t('token.name')" width="120" show-overflow-tooltip />
        <el-table-column :label="t('manage.published')" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.isPublished"
              :disabled="!canTogglePublish(row)"
              size="small"
              @change="handleTogglePublish(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="contentType" :label="t('manage.type')" width="100" />
        <el-table-column :label="t('manage.size')" width="80">
          <template #default="{ row }">{{ fileSizeLabel(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column :label="t('manage.created')" width="160">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column :label="t('manage.actions')" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="copyUrl(row.ossUrl)" plain>
              <el-icon><DocumentCopy /></el-icon>
            </el-button>
            <el-button size="small" type="danger" text @click="handleDelete(row)">{{ t('manage.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Mobile: card list -->
    <div class="mobile-cards">
      <div v-if="loading" class="card-loading">
        <el-skeleton :rows="3" animated />
      </div>
      <div
        v-for="img in images"
        :key="img.id"
        :class="['image-card-mobile', { selected: selectedIds.has(img.id) }]"
        @contextmenu.prevent="enterSelectMode(img)"
        @click="selectMode ? toggleSelect(img) : null"
      >
        <div v-if="selectMode" class="card-check">
          <el-checkbox :model-value="selectedIds.has(img.id)" @click.stop="toggleSelect(img)" />
        </div>
        <img :src="img.ossUrl" class="card-thumb" @click.stop="openPreview(img)" />
        <div class="card-body">
          <div class="card-filename">{{ img.filename }}</div>
          <div class="card-meta">
            <span>{{ img.contentType }}</span>
            <span>{{ fileSizeLabel(img.fileSize) }}</span>
            <span v-if="img.uploadedBy" class="card-uploader">{{ img.uploadedBy }}</span>
          </div>
          <div class="card-date">
            <span>{{ formatDate(img.createdAt) }}</span>
            <el-text :type="img.isPublished ? 'success' : 'warning'" size="small">{{ img.isPublished ? t('manage.published') : t('manage.unpublished') }}</el-text>
          </div>
          <div v-if="!selectMode" class="card-actions">
            <el-button type="primary" size="small" @click="copyUrl(img.ossUrl)" plain>
              <el-icon><DocumentCopy /></el-icon>
            </el-button>
            <el-button size="small" type="danger" plain @click="handleDelete(img)">{{ t('manage.delete') }}</el-button>
            <el-tooltip
              :content="t('manage.cannotTogglePublish')"
              placement="top"
              :disabled="canTogglePublish(img)"
            >
              <el-button
                size="small"
                :type="img.isPublished ? 'warning' : 'success'"
                plain
                :disabled="!canTogglePublish(img)"
                @click.stop="handleTogglePublish(img)"
              >
                {{ img.isPublished ? t('manage.unpublish') : t('manage.publish') }}
              </el-button>
            </el-tooltip>
          </div>
        </div>
      </div>
    </div>

    <!-- Mobile: floating action bar -->
    <transition name="float-fade">
      <div v-if="selectMode && selection.length > 0" class="float-action-bar mobile-only">
        <el-button size="small" type="danger" round @click="handleBatchDelete">
          {{ t('manage.delete') }} ({{ selection.length }})
        </el-button>
      </div>
    </transition>

    <el-pagination
      v-if="total > pageSize"
      :total="total"
      :page-size="pageSize"
      :current-page="page + 1"
      layout="prev, pager, next, total"
      @current-change="handlePageChange"
      style="margin-top: 16px; justify-content: center; display: flex; flex-wrap: wrap;"
    />

    <ImagePreview
      :image="previewImage"
      :visible="showPreview"
      @close="closePreview"
    />
  </div>
</template>

<style scoped>
.thumb {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.toolbar-search {
  display: flex;
  align-items: center;
  gap: 8px;
}
.search-input {
  width: 200px;
}

.toolbar-actions {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

/* Responsive visibility */
.desktop-table { display: block; }
.desktop-only { display: block; }
.mobile-cards { display: none; }
.mobile-only { display: none; }

@media (max-width: 767px) {
  .desktop-table { display: none; }
  .desktop-only { display: none; }
  .mobile-cards { display: flex; flex-direction: column; gap: 10px; margin-top: 10px; }
  .mobile-only { display: block; }
}

/* Select bar */
.select-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: #ecf5ff;
  border-radius: 8px;
  margin-bottom: 8px;
}
.select-count {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

/* Mobile card */
.image-card-mobile {
  display: flex;
  gap: 12px;
  background: #fff;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
  position: relative;
  transition: background 0.2s;
}
.image-card-mobile.selected {
  background: #ecf5ff;
  box-shadow: 0 0 0 2px #409eff inset;
}
.card-check {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}
.card-thumb {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
  flex-shrink: 0;
  cursor: pointer;
}
.card-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.card-filename {
  font-size: 14px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-meta {
  font-size: 12px;
  color: #909399;
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}
.card-uploader {
  color: #409eff;
  font-weight: 500;
}
.card-date {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 11px;
  color: #c0c4cc;
}
.card-actions {
  display: flex;
  gap: 8px;
  margin-top: 4px;
  align-items: center;
}
.card-actions .el-button {
  padding: 4px 8px;
  font-size: 12px;
}
.card-loading {
  padding: 12px;
  background: #fff;
  border-radius: 8px;
}

/* Floating action bar */
.float-action-bar {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 100;
  display: flex;
  gap: 8px;
  background: #fff;
  padding: 10px 16px;
  border-radius: 24px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}
.float-fade-enter-active,
.float-fade-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}
.float-fade-enter-from,
.float-fade-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(12px);
}
</style>
