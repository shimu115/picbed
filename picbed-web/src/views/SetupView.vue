<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { setupToken } from '@/api'
import { useTokenStore } from '@/stores/token'
import { ElMessage } from 'element-plus'

const router = useRouter()
const tokenStore = useTokenStore()
const { t } = useI18n()

const masterKey = ref('')
const tokenName = ref('Admin')
const tokenEmail = ref('')
const generatedToken = ref('')
const loading = ref(false)
const error = ref('')

async function handleSetup() {
  loading.value = true
  error.value = ''
  try {
    const res = await setupToken(masterKey.value, tokenName.value || 'Admin', tokenEmail.value.trim())
    generatedToken.value = res.data.data.token
    tokenStore.setToken(generatedToken.value)
    ElMessage.success(t('setup.success'))
  } catch (e) {
    error.value = e.response?.data?.msg || t('setup.setupFailed')
  } finally {
    loading.value = false
  }
}

function copyAndGo() {
  navigator.clipboard.writeText(generatedToken.value)
  ElMessage.success(t('common.copySuccess'))
  setTimeout(() => router.push('/upload'), 500)
}
</script>

<template>
  <div class="setup-page">
    <h2>{{ t('setup.title') }}</h2>
    <p class="setup-desc">{{ t('setup.desc') }}</p>

    <el-card v-if="!generatedToken" style="max-width: 500px">
      <el-form @submit.prevent="handleSetup">
        <el-form-item :label="t('setup.masterKey')">
          <el-input v-model="masterKey" type="password" :placeholder="t('setup.masterKeyPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('setup.tokenName')">
          <el-input v-model="tokenName" :placeholder="t('setup.tokenNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('token.email')">
          <el-input v-model="tokenEmail" :placeholder="t('token.emailPlaceholder')" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSetup" :loading="loading" :disabled="!masterKey">
            {{ t('setup.createToken') }}
          </el-button>
        </el-form-item>
        <p v-if="error" class="error-text">{{ error }}</p>
      </el-form>
    </el-card>

    <el-result
      v-else
      icon="success"
      :title="t('setup.success')"
      :sub-title="t('setup.successDesc')"
    >
      <template #extra>
        <el-input :model-value="generatedToken" readonly size="large" style="margin-bottom: 16px" />
        <el-button type="primary" @click="copyAndGo">{{ t('token.copyAndGo') }}</el-button>
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
