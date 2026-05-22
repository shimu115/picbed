<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { setupToken } from '@/api'
import { useTokenStore } from '@/stores/token'
import { ElMessage } from 'element-plus'

const router = useRouter()
const tokenStore = useTokenStore()

const masterKey = ref('')
const tokenName = ref('Admin')
const generatedToken = ref('')
const loading = ref(false)
const error = ref('')

async function handleSetup() {
  loading.value = true
  error.value = ''
  try {
    const res = await setupToken(masterKey.value, tokenName.value || 'Admin')
    generatedToken.value = res.data.data.token
    tokenStore.setToken(generatedToken.value)
    ElMessage.success('Token created successfully!')
  } catch (e) {
    error.value = e.response?.data?.message || 'Setup failed. Check your master key.'
  } finally {
    loading.value = false
  }
}

function copyAndGo() {
  navigator.clipboard.writeText(generatedToken.value)
  ElMessage.success('Token copied! Redirecting...')
  setTimeout(() => router.push('/upload'), 500)
}
</script>

<template>
  <div class="setup-page">
    <h2>Initial Setup</h2>
    <p class="setup-desc">
      Create your first auth token using the server's master setup key.
      This is a one-time operation.
    </p>

    <el-card v-if="!generatedToken" style="max-width: 500px">
      <el-form @submit.prevent="handleSetup">
        <el-form-item label="Master Setup Key">
          <el-input v-model="masterKey" type="password" placeholder="Enter master setup key" />
        </el-form-item>
        <el-form-item label="Token Name">
          <el-input v-model="tokenName" placeholder="e.g. Admin Laptop" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSetup" :loading="loading" :disabled="!masterKey">
            Create Token
          </el-button>
        </el-form-item>
        <p v-if="error" class="error-text">{{ error }}</p>
      </el-form>
    </el-card>

    <el-result
      v-else
      icon="success"
      title="Token Created"
      sub-title="Copy your token and keep it safe. It won't be shown again."
    >
      <template #extra>
        <el-input :model-value="generatedToken" readonly size="large" style="margin-bottom: 16px" />
        <el-button type="primary" @click="copyAndGo">Copy & Go to Upload</el-button>
      </template>
    </el-result>
  </div>
</template>

<style scoped>
.setup-desc {
  color: #606266;
  margin-bottom: 20px;
  max-width: 500px;
}
.error-text {
  color: #f56c6c;
  font-size: 13px;
}
</style>
