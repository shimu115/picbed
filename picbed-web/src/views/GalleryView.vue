<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPublicImages, deleteImage } from '@/api'
import { useTokenStore } from '@/stores/token'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImageGrid from '@/components/gallery/ImageGrid.vue'
import ImagePreview from '@/components/gallery/ImagePreview.vue'
import EmptyState from '@/components/common/EmptyState.vue'

const router = useRouter()
const tokenStore = useTokenStore()

const images = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(0)
const previewImage = ref(null)
const showPreview = ref(false)

async function loadImages() {
  loading.value = true
  try {
    const res = await getPublicImages(page.value, 20)
    images.value = res.data.data.content
    total.value = res.data.data.totalElements
  } finally {
    loading.value = false
  }
}

function openPreview(img) {
  previewImage.value = img
  showPreview.value = true
}

function closePreview() {
  showPreview.value = false
}

async function handleDelete(img) {
  if (!tokenStore.hasToken) {
    ElMessage.warning('Set your auth token first')
    return
  }
  try {
    await ElMessageBox.confirm(`Delete "${img.filename}"?`, 'Confirm', { type: 'warning' })
    await deleteImage(img.id)
    ElMessage.success('Deleted')
    await loadImages()
  } catch { /* cancelled */ }
}

function handleLoadMore() {
  page.value++
  loadImages()
}

onMounted(loadImages)
</script>

<template>
  <div class="gallery-page">
    <h2>Gallery</h2>
    <ImageGrid
      v-if="images.length > 0"
      :images="images"
      :loading="loading"
      @preview="openPreview"
      @delete="handleDelete"
    />
    <EmptyState
      v-else-if="!loading"
      message="No images yet. Upload your first image!"
      :show-upload="tokenStore.hasToken"
      @upload="router.push('/upload')"
    />
    <div v-if="images.length < total" class="load-more">
      <el-button @click="handleLoadMore" :loading="loading">Load More</el-button>
    </div>

    <ImagePreview
      :image="previewImage"
      :visible="showPreview"
      @close="closePreview"
    />
  </div>
</template>

<style scoped>
.load-more {
  text-align: center;
  margin-top: 24px;
}
</style>
