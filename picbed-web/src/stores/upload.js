import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUploadStore = defineStore('upload', () => {
  const queue = ref([])

  function addFile(id, file) {
    queue.value.push({
      id,
      file,
      progress: 0,
      status: 'pending',
      accessUrl: '',
      errorMsg: ''
    })
  }

  function updateProgress(id, progress) {
    const item = queue.value.find(i => i.id === id)
    if (item) {
      item.progress = progress
      if (item.status === 'pending') item.status = 'uploading'
    }
  }

  function markSuccess(id, accessUrl) {
    const item = queue.value.find(i => i.id === id)
    if (item) {
      item.status = 'success'
      item.progress = 100
      item.accessUrl = accessUrl
    }
  }

  function updateFile(id, file) {
    const item = queue.value.find(i => i.id === id)
    if (item) item.file = file
  }

  function markError(id, msg) {
    const item = queue.value.find(i => i.id === id)
    if (item) {
      item.status = 'error'
      item.errorMsg = msg
    }
  }

  function clearCompleted() {
    queue.value = queue.value.filter(i => i.status !== 'success' && i.status !== 'error')
  }

  function clearAll() {
    queue.value = []
  }

  return { queue, addFile, updateProgress, updateFile, markSuccess, markError, clearCompleted, clearAll }
})
