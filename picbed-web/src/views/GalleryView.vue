<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { getPublicImages } from '@/api'
import { useTokenStore } from '@/stores/token'
import ImageGrid from '@/components/gallery/ImageGrid.vue'
import ImagePreview from '@/components/gallery/ImagePreview.vue'
import EmptyState from '@/components/common/EmptyState.vue'

const router = useRouter()
const tokenStore = useTokenStore()
const { t } = useI18n()

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

function handleLoadMore() {
  page.value++
  loadImages()
}

onMounted(loadImages)
</script>

<template>
  <div class="gallery-page">
    <h2>{{ t('gallery.title') }}</h2>
    <ImageGrid
      v-if="images.length > 0"
      :images="images"
      :loading="loading"
      @preview="openPreview"
    />
    <EmptyState
      v-else-if="!loading"
      :show-upload="tokenStore.hasToken"
      @upload="router.push('/upload')"
    />
    <div v-if="images.length < total" class="load-more">
      <el-button @click="handleLoadMore" :loading="loading">{{ t('gallery.loadMore') }}</el-button>
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
