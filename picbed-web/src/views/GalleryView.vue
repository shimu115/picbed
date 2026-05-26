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
const searchText = ref('')
const searchType = ref('filename')

async function loadImages() {
  loading.value = true
  try {
    const res = await getPublicImages(page.value, 20, searchText.value.trim(), searchType.value)
    images.value = res.data.data.content
    total.value = res.data.data.totalElements
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 0
  loadImages()
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
    <div class="search-bar">
      <el-input
        v-model="searchText"
        :placeholder="t('gallery.searchPlaceholder')"
        class="search-input"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      >
        <template #prefix>
          <el-icon><svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg></el-icon>
        </template>
      </el-input>
      <el-radio-group v-model="searchType" @change="handleSearch" size="small">
        <el-radio-button value="filename">{{ t('gallery.searchByFilename') }}</el-radio-button>
        <el-radio-button value="username">{{ t('gallery.searchByUsername') }}</el-radio-button>
      </el-radio-group>
    </div>
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
.search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}
.search-input {
  max-width: 320px;
}
.load-more {
  text-align: center;
  margin-top: 24px;
}
@media (max-width: 767px) {
  .search-bar {
    flex-direction: column;
    align-items: stretch;
  }
  .search-input {
    max-width: none;
  }
}
</style>
