<script setup>
import ImageCard from './ImageCard.vue'

defineProps({
  images: { type: Array, required: true },
  loading: { type: Boolean, default: false }
})

defineEmits(['preview'])
</script>

<template>
  <div v-if="loading" class="loading-area">
    <el-skeleton :rows="3" animated />
  </div>
  <div v-else class="image-grid">
    <ImageCard
      v-for="img in images"
      :key="img.id"
      :image="img"
      @preview="$emit('preview', $event)"
    />
  </div>
</template>

<style scoped>
.loading-area {
  margin-top: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
}
</style>
